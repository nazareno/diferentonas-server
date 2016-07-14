package controllers;


import static play.libs.Json.toJson;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.inject.Singleton;

import models.Cidadao;
import models.CidadaoDAO;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Singleton
public class LoginController extends Controller {
	
	  public final static String AUTH_TOKEN_HEADER = "X-AUTH-TOKEN";
	    public static final String AUTH_TOKEN = "authToken";


	@Inject
	private CidadaoDAO dao;
	@Inject 
	WSClient ws;
	
	@Inject
	JPAApi api;
	 
	@Transactional(readOnly=true)
	public Result estaLogado() {
		
		String token = session("cidadao_token");
	    if(token != null) {
	        return ok("Hello " + dao.findByToken(token));
	    } else {
	        return unauthorized("Oops, you are not connected");
	    }
	}

	@Transactional
	public CompletionStage<Result> login(String auth_token, String auth_app) {
		
//		WSRequest debugRequest = ws.url("http://graph.facebook.com/debug_token")
//				.setRequestTimeout(1000)
//				.setQueryParameter("input_token", auth_token)
//				.setQueryParameter("access_token", "1168526739834367|054daccc32c5cb6a941e644d6ef23b82");

		WSRequest emailRequest = ws.url("https://graph.facebook.com/me")
				.setQueryParameter("access_token", auth_token)
				.setQueryParameter("fields", "name,email");
			
		return emailRequest.get().thenApply( emailResponse -> {
			Cidadao cidadao = api.withTransaction(() -> {
				JsonNode jsonEmail = emailResponse.asJson();
				String email = jsonEmail.get("email").asText();
				Cidadao cidadaoAutorizado = dao.findByLogin(email);
				if(cidadaoAutorizado == null){
					cidadaoAutorizado = new Cidadao(email);
				}
				cidadaoAutorizado.setToken(UUID.randomUUID().toString());
				dao.saveAndUpdate(cidadaoAutorizado);
				return cidadaoAutorizado;
			});
			
			ObjectNode accessKey = Json.newObject();
			accessKey.set("access_token", Json.toJson(cidadao.getToken()));
			return ok(accessKey);
		});
	}
	
	@Security.Authenticated(Secured.class)
	@Transactional
	public Result getPrivate(){
		return ok(toJson(dao.findByToken(request().getHeader(AUTH_TOKEN_HEADER))));
	}

}
