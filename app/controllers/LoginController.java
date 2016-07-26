package controllers;

import static play.libs.Json.toJson;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Cidadao;
import models.CidadaoDAO;
import models.DadosLogin;
import models.ProvedorDeLogin;
import models.Token;
import play.Configuration;
import play.Logger;
import play.data.FormFactory;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

@Singleton
public class LoginController extends Controller {

	public static final String CLIENT_ID_KEY = "client_id",
			REDIRECT_URI_KEY = "redirect_uri", CLIENT_SECRET = "client_secret",
			CODE_KEY = "code", GRANT_TYPE_KEY = "grant_type",
			AUTH_CODE = "authorization_code";

	private CidadaoDAO dao;
	private WSClient ws;
	private FormFactory formFactory;
	private String facebookSecret;
	private String googleSecret;
	private JPAApi api;

	@Inject 
	public LoginController(CidadaoDAO dao, WSClient ws, FormFactory formFactory,
			Configuration configuration, JPAApi api) {
		this.dao = dao;
		this.ws = ws;
		this.formFactory = formFactory;
		this.api = api;
		this.facebookSecret = configuration.getString(ProvedorDeLogin.FACEBOOK.getSecretProp());
		this.googleSecret = configuration.getString(ProvedorDeLogin.GOOGLE.getSecretProp());
	}

	@Transactional
	public CompletionStage<Result> loginFacebook() {
		Request request = request();

		DadosLogin payload = formFactory.form(DadosLogin.class)
				.bindFromRequest().get();

		final String accessTokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token";
		final String graphApiUrl = "https://graph.facebook.com/v2.3/me";

		WSRequest requisicaoDeToken = ws.url(accessTokenUrl)
				.setQueryParameter(CLIENT_ID_KEY, payload.getClientId())
				.setQueryParameter(REDIRECT_URI_KEY, payload.getRedirectUri())
				.setQueryParameter(CLIENT_SECRET, facebookSecret)
				.setQueryParameter(CODE_KEY, payload.getCode());

		return requisicaoDeToken.get().thenComposeAsync(respostaComToken -> {

			JsonNode jsonComToken = respostaComToken.asJson();
			Logger.debug(jsonComToken.toString());
			if(!jsonComToken.has("access_token")){
				return CompletableFuture.supplyAsync(() -> {
					return ok(toJson(""));
				});
			}

			WSRequest requisicaoDePerfil = ws.url(graphApiUrl)
					.setQueryParameter("access_token", jsonComToken.get("access_token").asText())
					.setQueryParameter("fields", "[\"name\",\"email\"]")
					.setQueryParameter("expires_in", jsonComToken.get("expires_in").asText());

			return requisicaoDePerfil.get().thenApply( respostaComEmail -> {

				JsonNode jsonComEmail = respostaComEmail.asJson();
Logger.debug(jsonComEmail.toString());
				try {
					
					return processUser(request, ProvedorDeLogin.FACEBOOK, jsonComEmail.get("id").asText(), 
							jsonComEmail.get("email").asText(), jsonComEmail.get("name").asText(), 
							"http://graph.facebook.com/" + jsonComEmail.get("id").asText() + "/picture?type=large");
				} catch (Exception e) {
					return internalServerError(e.getMessage());
				}
			});
		});
	}

	@Transactional
	public CompletionStage<Result> loginGoogle() {
		Request request = request();

		DadosLogin payload = formFactory.form(DadosLogin.class)
				.bindFromRequest().get();

	    final String accessTokenUrl = "https://accounts.google.com/o/oauth2/token";
	    final String peopleApiUrl = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";

		WSRequest requisicaoDeToken = ws.url(accessTokenUrl).setContentType("application/x-www-form-urlencoded");
		
		StringBuilder builder = new StringBuilder()
		.append(CLIENT_ID_KEY + "=" +payload.getClientId()+"&")
		.append(REDIRECT_URI_KEY + "=" +payload.getRedirectUri()+"&")
		.append(CLIENT_SECRET + "=" +googleSecret+"&")
		.append(CODE_KEY + "=" +payload.getCode()+"&")
		.append(GRANT_TYPE_KEY + "=" +AUTH_CODE);
		
		return requisicaoDeToken.post(builder.toString()).thenComposeAsync(respostaComToken -> {

			JsonNode jsonComToken = respostaComToken.asJson();
			Logger.debug(jsonComToken.toString());
			if(!jsonComToken.has("access_token")){
				return CompletableFuture.supplyAsync(() -> {
					return ok(toJson(""));
				});
			}
			
			String accessToken = jsonComToken.get("access_token").asText();
			
			WSRequest requisicaoDePerfil = ws.url(peopleApiUrl).setContentType("text/plain")
					.setHeader(AuthUtils.AUTH_HEADER_KEY, String.format("Bearer %s", accessToken));

			return requisicaoDePerfil.get().thenApply( respostaComEmail -> {

				JsonNode jsonComEmail = respostaComEmail.asJson();
				Logger.debug(jsonComEmail.toString());
				try {
					String urlDaFoto = jsonComEmail.get("picture").asText();
					return processUser(request, ProvedorDeLogin.GOOGLE, jsonComEmail.get("sub").asText(), 
							jsonComEmail.get("email").asText(), jsonComEmail.get("name").asText(),
							urlDaFoto.substring(0, urlDaFoto.indexOf("?")));
				} catch (Exception e) {
					return internalServerError(e.getMessage());
				}
			});
		});
	}

	private Result processUser(final Request request, ProvedorDeLogin provider,
			final String idNoProvider, final String email, String nome, String urlDaFoto){

		return api.withTransaction(() -> {
			try{

				final String authHeader = request.getHeader(AuthUtils.AUTH_HEADER_KEY);

				Cidadao cidadao = dao.findByProvider(provider, idNoProvider);
				if (authHeader != null && !authHeader.isEmpty()) { // existe um header
					if (cidadao != null) { // tá fazendo login duas vezes...
						return status(CONFLICT, "Cidadão já está logado!");
					}

					String uuid = AuthUtils.getSubject(authHeader);
					Cidadao cidadaoDoHeader = dao.find(UUID.fromString(uuid));
					if (cidadaoDoHeader == null) { // fez login antes mas deletou usuário e tá tentando usar header
						return notFound("Cidadão não encontrado no banco de dados. Limpe seu cache e tente novamente.");
					} else if(!cidadao.equals(cidadaoDoHeader)){
						return status(CONFLICT, "Login inválido. Limpe seu cache e tente novamente.");
					}
					
				} else {
					cidadao = dao.findByLogin(email);
					if (cidadao == null) { // Nova conta
						cidadao = new Cidadao(nome, email);
					}
				}
				cidadao.setProviderId(provider, idNoProvider);
				cidadao.setNome(nome);
				cidadao.setUrlDaFoto(urlDaFoto);
				cidadao = dao.saveAndUpdate(cidadao);
				Token token = AuthUtils.createToken(request.remoteAddress(), cidadao);
				Logger.debug("authorization: [token " + token.getToken() + "]");
				return ok(toJson(token));
			}catch(Exception e){
				return internalServerError(e.getMessage());

			}
		});

	}


}
