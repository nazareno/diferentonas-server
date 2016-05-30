package controllers;

import static play.libs.Json.toJson;
import models.Mensagem;
import models.MensagemService;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class CoachingController extends Controller {
	
	private MensagemService service;
	private FormFactory formFactory;

	@Inject
	public CoachingController(MensagemService service, FormFactory formFactory) {
		this.service = service;
		this.formFactory = formFactory;
	}
	
    @Transactional(readOnly = true)
    public Result getMensagens(Long size) {
    	return ok(toJson(service.paginate(0, size.intValue())));
    }
	
    @Transactional
    public Result save() {
    	Mensagem mensagem = service.create(formFactory.form(Mensagem.class).bindFromRequest().get());
    	return created(toJson(mensagem)); 
    }
}
