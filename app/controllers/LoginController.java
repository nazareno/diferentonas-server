package controllers;

import static play.libs.Json.toJson;

import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Cidadao;
import models.CidadaoDAO;
import models.ProvedorDeLogin;
import play.Configuration;
import play.Logger;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.JOSEException;

@Singleton
public class LoginController extends Controller {

	public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
	public static final String AUTH_TOKEN = "authToken";

	public static final String CLIENT_ID_KEY = "client_id",
			REDIRECT_URI_KEY = "redirect_uri", CLIENT_SECRET = "client_secret",
			CODE_KEY = "code", GRANT_TYPE_KEY = "grant_type",
			AUTH_CODE = "authorization_code";

	private CidadaoDAO dao;
	private WSClient ws;
	private FormFactory formFactory;
	private String facebookSecret;
	
	@Inject 
	public LoginController(CidadaoDAO dao, WSClient ws, FormFactory formFactory,
			Configuration configuration) {
		this.dao = dao;
		this.ws = ws;
		this.formFactory = formFactory;
		this.facebookSecret = configuration.getString("secret.facebook");
	}

	@Transactional(readOnly = true)
	public Result estaLogado() {

		String token = session("cidadao_token");
		if (token != null) {
			return ok("Hello " + dao.findByToken(token));
		} else {
			return unauthorized("Oops, you are not connected");
		}
	}

	@Transactional
	public CompletionStage<Result> loginFacebook() {

		DadosLogin payload = formFactory.form(DadosLogin.class)
				.bindFromRequest().get();
		
		Logger.debug(payload.toString());

		final String accessTokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token";
		final String graphApiUrl = "https://graph.facebook.com/v2.3/me";

		WSRequest requisicaoDeToken = ws.url(accessTokenUrl)
				.setQueryParameter(CLIENT_ID_KEY, payload.getClientId())
				.setQueryParameter(REDIRECT_URI_KEY, payload.getRedirectUri())
				.setQueryParameter(CLIENT_SECRET, facebookSecret)
				.setQueryParameter(CODE_KEY, payload.getCode());

		return requisicaoDeToken.get().thenComposeAsync(respostaComToken -> {

			JsonNode jsonComToken = respostaComToken.asJson();
			
			if(!jsonComToken.has("access_token")){
				return CompletableFuture.supplyAsync(() -> {
					return ok(toJson(""));
				});
			}
			
			WSRequest requisicaoDePerfil = ws.url(graphApiUrl)
					.setQueryParameter("access_token", jsonComToken.get("access_token").asText())
					.setQueryParameter("expires_in", jsonComToken.get("expires_in").asText());
			
			return requisicaoDePerfil.get().thenApply( respostaComEmail -> {
				
				JsonNode jsonComEmail = respostaComEmail.asJson();
				
				try {
					return processUser(request(), ProvedorDeLogin.FACEBOOK, jsonComEmail.get("id").asText(), jsonComEmail.get("email").asText(), jsonComEmail.get("name").asText());
				} catch (Exception e) {
					return internalServerError("NAO SEI");
				}
			});
		});
	}

	@Security.Authenticated(Secured.class)
	@Transactional
	public Result getPrivate() {
		return ok(toJson(dao.find(UUID.fromString(request().username()))));
	}
	
	
	 private Result processUser(final Request request, ProvedorDeLogin provider,
		      final String idNoProvider, final String email, String nome) throws JOSEException, ParseException {
		    
		 final String authHeader = request.getHeader(AuthUtils.AUTH_HEADER_KEY);

		 Cidadao cidadao = dao.findByProvider(provider, idNoProvider);
		 if (authHeader != null && !authHeader.isEmpty()) { // existe um header
			 if (cidadao != null) { // tá fazendo login duas vezes...
				 return status(CONFLICT, "CONFLITOOOOO");
			 }

			 String uuid = AuthUtils.getSubject(authHeader);
			 cidadao = dao.find(UUID.fromString(uuid));
			 if (cidadao == null) { // fez login antes mas deletou usuário e tá tentando usar header
				 return notFound("NAO ACHOU");
			 }
			 
			 // já logou com outro provedor e está com header ok
			 cidadao.setProviderId(provider, idNoProvider);
			 cidadao.setNome(nome);
			 cidadao = dao.saveAndUpdate(cidadao);
		 } else if (cidadao == null) { // Nova conta
				cidadao = new Cidadao(nome, email);
				cidadao.setProviderId(provider, idNoProvider);
				cidadao = dao.saveAndUpdate(cidadao);
		 } // default: perdeu o token, ou expirou...

		 return ok(toJson(AuthUtils.createToken(request.remoteAddress(), cidadao.getId())));
	 }


}
