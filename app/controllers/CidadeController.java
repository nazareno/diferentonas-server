package controllers;

import static play.libs.Json.toJson;

import java.util.Collections;

import models.Cidade;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

public class CidadeController extends Controller {

    public Result index() {
        return ok("Olar.");
    }

    @Transactional(readOnly = true)
    public Result getSimilares(Long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        return ok(toJson(cidade == null? Collections.emptyList() : cidade.getSimilares()));
    }


}
