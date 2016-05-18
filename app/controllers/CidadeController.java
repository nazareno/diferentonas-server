package controllers;

import static play.libs.Json.toJson;
import models.Cidade;
import models.CidadeService;
import play.Logger;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CidadeController extends Controller {
	
    @Transactional(readOnly = true)
    public Result getIniciativas(Long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        if(cidade == null) {
            return notFound();
        } else {
            Logger.debug("Iniciativas para " + cidade.getNome() + ": " + cidade.getIniciativas().size());
            return ok(toJson(cidade.getIniciativas()));
        }
    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
    	Cidade cidade = CidadeService.find(id);

        if(cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        return ok(toJson(cidade));
    }

    @Transactional(readOnly = true)
    public Result getCidades() {
    	
        return ok(toJson(CidadeService.all()));
    }

    public Result index() {
        return ok("Olar.");
    }

    @Transactional(readOnly = true)
    public Result getSimilares(Long id) {
    	
        Cidade cidade = CidadeService.find(id);
        if(cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        return ok(toJson(cidade.getSimilares()));
    }
    

}
