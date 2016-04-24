package controllers;

import static play.libs.Json.toJson;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import models.Cidade;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class CidadeController extends Controller {

    @Transactional(readOnly = true)
    public Result getConvenios(long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        if(cidade == null) {
            return notFound();
        } else {
            return ok(toJson(cidade.getConvenios()));
        }
    }

    @Transactional(readOnly = true)
    public Result getCidade(long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        if(cidade == null) {
            ok(toJson(Json.newObject()));
        }

        return ok(toJson(cidade));
    }

    @Transactional(readOnly = true)
    public Result getCidades() {
        List<Cidade> cidades = JPA.em().createQuery("FROM Cidade", Cidade.class).getResultList();
        return ok(toJson(cidades));
    }

    public Result index() {
        return ok("Olar.");
    }

    @Transactional(readOnly = true)
    public Result getSimilares(Long id) {
        Cidade cidade = JPA.em().find(Cidade.class, id);

        return ok(toJson(cidade == null? Collections.emptyList() : cidade.getSimilares()));
    }


}
