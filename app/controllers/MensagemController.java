package controllers;

import static play.libs.Json.toJson;

import java.util.UUID;

import models.Cidadao;
import models.CidadaoDAO;
import models.Mensagem;
import models.MensagemDAO;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.google.inject.Inject;

@Security.Authenticated(AcessoCidadao.class)
public class MensagemController extends Controller {

    private MensagemDAO daoMensagem;
    private FormFactory formFactory;
	private CidadaoDAO daoCidadao;

    @Inject
    public MensagemController(MensagemDAO daoMensagem, CidadaoDAO daoCidadao, FormFactory formFactory) {
        this.daoMensagem = daoMensagem;
		this.daoCidadao = daoCidadao;
        this.formFactory = formFactory;
    }

    @Transactional(readOnly = true)
    public Result getMensagens(Integer pagina, Integer tamanhoPagina) {
        return ok(toJson(daoMensagem.paginate(pagina, tamanhoPagina)));
    }

    @Transactional(readOnly = true)
    public Result getMensagensNaoLidas(UUID ultimaLida) {
        return ok(toJson(daoMensagem.findMaisRecentesQue(ultimaLida)));
    }

    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result save() {
    	
    	Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		if (!cidadao.isFuncionario()) {
			return unauthorized("Cidadão não autorizado");
		}
    	
        Form<Mensagem> form = formFactory.form(Mensagem.class).bindFromRequest();
        if (form.hasErrors()) {
            String recebido = Controller.request().body().asJson().toString();
            if (recebido.length() > 30) {
                recebido = recebido.substring(0, 30) + "...";
            }
            Logger.debug("Submissão com erros: " + recebido + "; Erros: " + form.errorsAsJson());
            return badRequest(form.errorsAsJson());
        }
        
        Mensagem mensagem = daoMensagem.create(form.get());
        mensagem.setAutor(cidadao.getMinisterioDeAfiliacao());
        
        return created(toJson(mensagem));
    }

    @Transactional
    public Result delete(String id) {
    	
    	Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		if (!cidadao.isFuncionario()) {
			return unauthorized("Cidadão não autorizado");
		}

		Mensagem mensagem = daoMensagem.find(UUID.fromString(id));
        if (mensagem != null) {
            daoMensagem.delete(mensagem);
            return ok(toJson("Deleted: " + id));
        } else {
            return notFound(toJson("id : " + id));
        }
    }
}
