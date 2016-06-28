package controllers;

import static play.libs.Json.toJson;
import models.Cidade;
import models.CidadeDAO;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

public class CidadeController extends Controller {

    private CidadeDAO dao;

    @Inject
    public CidadeController(CidadeDAO dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
        Cidade cidade = dao.find(id);

        if (cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        ObjectNode node = (ObjectNode) toJson(cidade);
        node.set("scores", Json.toJson(cidade.getScores()));
		return ok(node);
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
        if (cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        return ok(toJson(cidade.getSimilares()));
    }


}
