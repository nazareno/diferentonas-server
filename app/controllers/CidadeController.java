package controllers;

import static play.libs.Json.toJson;
import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

public class CidadeController extends Controller {
	
	private CidadeDAO dao;
	private IniciativaDAO daoIniciativa;

	@Inject
	public CidadeController(CidadeDAO dao, IniciativaDAO daoIniciativa) {
		this.dao = dao;
		this.daoIniciativa = daoIniciativa;
	}
	
    @Transactional(readOnly = true)
    public Result getIniciativas(Long id) {
    	Cidade cidade = dao.findComIniciativas(id);
    	
    	for (Iniciativa iniciativa : cidade.getIniciativas()) {
			iniciativa.setSumario(daoIniciativa.calculaSumario(iniciativa.getId()));
		}

        if(cidade == null) {
            return notFound();
        }

        Logger.debug("Iniciativas para " + cidade.getNome() + ": " + cidade.getIniciativas().size());
        return ok(toJson(cidade.getIniciativas()));
    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
    	Cidade cidade = dao.find(id);

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
    	
        return ok(toJson(dao.all()));
    }

    public Result index() {
        return ok("Olar.");
    }

    @Transactional(readOnly = true)
    public Result getSimilares(Long id) {
    	
        Cidade cidade = dao.find(id);
        if(cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        return ok(toJson(cidade.getSimilares()));
    }
    

}
