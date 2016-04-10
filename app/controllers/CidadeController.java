package controllers;

import static play.libs.Json.*;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Cidade;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class CidadeController extends Controller {

    private JPAApi jpaAPI;

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
        List<Cidade> cidades = (List<Cidade>) JPA.em().createQuery("FROM Cidade").getResultList();
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
