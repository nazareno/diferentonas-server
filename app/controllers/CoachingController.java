package controllers;

import static play.libs.Json.toJson;

import java.util.Collections;

import models.CidadeService;
import models.Mensagem;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class CoachingController extends Controller {
	
	private CidadeService service;

	@Inject
	public CoachingController(CidadeService service) {
		this.service = service;
	}
	
    @Transactional(readOnly = true)
    public Result getMensagens(Long size) {
    	return ok(toJson(Collections.EMPTY_LIST));
    }
	
    @Transactional
    public Result save() {
    	Mensagem mensagem = Json.fromJson(Controller.request().body().asJson(), Mensagem.class);
    	assert mensagem != null: "Caguei tudo";
    	assert mensagem.getId() != null: "Caguei o UUID tudo";
    	return ok("Não caguei!");
    }
}
