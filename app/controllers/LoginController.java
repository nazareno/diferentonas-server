package controllers;


import static akka.pattern.Patterns.ask;
import static play.libs.Json.toJson;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.Cidadao;
import models.CidadaoDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import actors.AtualizadorActorProtocol;
import akka.actor.ActorRef;

@Singleton
public class LoginController extends Controller {

	@Inject
	private CidadaoDAO dao;

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
	public Result login() {
		
		String token = session("auth");
	    if(token != null) {
	    	if(dao.findByToken(token) == null){
	    		Cidadao cidadao = new Cidadao("novo");
	    		cidadao.setToken(token);
	    		dao.saveAndUpdate(cidadao);
	    		session("cidadao_token", token);
	    		return ok("Hello " + dao.findByToken(token));
	    	}
	        return unauthorized("Oops, you are not connected");
	    } else {
	        return unauthorized("Oops, you are not connected");
	    }
	}

}
