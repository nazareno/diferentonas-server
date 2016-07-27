package controllers;

import static play.libs.Json.toJson;
import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;
import static play.mvc.Results.status;

import java.util.List;
import java.util.UUID;

import models.Cidadao;
import models.CidadaoDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import models.Opiniao;
import models.OpiniaoDAO;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

/**
 * Controller para ações relacionadas às opiniões dos usuários em iniciativas.
 */
@Security.Authenticated(AcessoCidadao.class)
public class OpiniaoController {

    @Inject
    private CidadaoDAO cidadaoDAO;
    @Inject
    private IniciativaDAO iniciativaDAO;
    @Inject
    private OpiniaoDAO opiniaoDAO;
    @Inject
    private FormFactory formFactory;

    @Transactional(readOnly = true)
    public Result getOpinioes(Long idIniciativa, int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound("Iniciativa não encontrada");
        }

        List<Opiniao> opinioes = opiniaoDAO.findByIniciativa(idIniciativa, pagina, tamanhoPagina);
        for (Opiniao opiniao : opinioes) {
			opiniao.setApoiada(getUsuarioLogado());
		}
        return ok(toJson(opinioes));
    }

    @Transactional
    public Result addOpiniao(Long idIniciativa) {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest("Esperava receber json");
        }

        // Usando anotações de validação do play
        Form<Opiniao> form = formFactory.form(Opiniao.class);
        Form<Opiniao> comDados = form.bind(json);
        if (comDados.hasErrors()) {
            Logger.debug("Submissão com erros: " + json.toString() + "; Erros: " + comDados.errorsAsJson());
            return badRequest(comDados.errorsAsJson());
        }
        Opiniao opiniao = comDados.get();

        opiniao.setAutor(getUsuarioLogado());

        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound("Iniciativa não encontrada");
        }

        Logger.debug("Add opinião na iniciativa " + iniciativa.getId());

        iniciativa.addOpiniao(opiniao);
        iniciativaDAO.flush(); // para que a opinião seja retornada com id
        
        return ok(toJson(opiniao));
    }
    
    @Transactional
    public Result addJoinha(Long idIniciativa, String idOpiniao){
    	
    	Cidadao apoiador = getUsuarioLogado();
    	
    	try{
    		UUID id = UUID.fromString(idOpiniao);
    		Opiniao opiniao = opiniaoDAO.find(id);
    		if(opiniao.addApoiador(apoiador)){
    			return ok();
    		}else{
    			return status(play.mvc.Http.Status.CONFLICT); 
    		}
    	}catch(IllegalArgumentException e){
    		return notFound(idOpiniao);
    	}
    }

    @Transactional
    public Result removeJoinha(Long idIniciativa, String idOpiniao){
    	
    	Cidadao apoiador = getUsuarioLogado();
    	
    	try{
    		UUID id = UUID.fromString(idOpiniao);
    		Opiniao opiniao = opiniaoDAO.find(id);
    		if(opiniao.removeApoiador(apoiador)){
    			return ok();
    		}else{
    			return badRequest("O cidadão já não apoia essa opinião"); 
    		}
    	}catch(IllegalArgumentException e){
    		return notFound(idOpiniao);
    	}
    }

    private Cidadao getUsuarioLogado() {
    	
    	return cidadaoDAO.find(UUID.fromString(request().username()));
    }
}
