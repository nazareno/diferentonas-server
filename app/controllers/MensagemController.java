package controllers;

import static play.libs.Json.toJson;

import java.util.UUID;

import models.Mensagem;
import models.MensagemDAO;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class MensagemController extends Controller {
	
	private MensagemDAO dao;
	private FormFactory formFactory;

	@Inject
	public MensagemController(MensagemDAO dao, FormFactory formFactory) {
		this.dao = dao;
		this.formFactory = formFactory;
	}
	
    @Transactional(readOnly = true)
    public Result getMensagens(Long quantidade) {
    	return ok(toJson(dao.paginate(0, quantidade.intValue())));
    }
	
    @Transactional(readOnly = true)
    public Result getMensagensNaoLidas(UUID ultimaLida) {
    	return ok(toJson(dao.findMaisRecentesQue(ultimaLida)));
    }
	
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result save() {
    	Form<Mensagem> form = formFactory.form(Mensagem.class).bindFromRequest();
    	if(form.hasErrors()){
            Logger.debug("Submissão com erros: " + Controller.request().body().asJson().toString() + "; Erros: " + form.errorsAsJson());
    		return badRequest(form.errorsAsJson());
    	}
		Mensagem mensagem = dao.create(form.get());
    	return created(toJson(mensagem)); 
    }

    @Transactional
    public Result delete(String id) {
    	Mensagem mensagem = dao.find(UUID.fromString(id));
    	if(mensagem != null){
    		dao.delete(mensagem);
    		return ok(toJson("Deleted: " + id)); 
    	}else{
    		return notFound(toJson("id : " + id ));
    	}
    }
}
