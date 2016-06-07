package controllers;

import static play.libs.Json.toJson;

import models.*;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

import java.util.List;

public class CidadeController extends Controller {

    private CidadeDAO dao;
    private IniciativaDAO daoIniciativa;
    private CidadaoDAO daoCidadao;

    @Inject
    public CidadeController(CidadeDAO dao, IniciativaDAO daoIniciativa, CidadaoDAO daoCidadao) {
        this.dao = dao;
        this.daoIniciativa = daoIniciativa;
        this.daoCidadao = daoCidadao;
    }

    @Transactional(readOnly = true)
    public Result getIniciativas(Long idCidade) {
        Cidade cidade = dao.findComIniciativas(idCidade);
        if (cidade == null) {
            return notFound("Cidade " + idCidade);
        }

        adicionaInfoParaView(cidade.getIniciativas());

        Logger.debug("Iniciativas para " + cidade.getNome() + ": " + cidade.getIniciativas().size());
        return ok(toJson(cidade.getIniciativas()));
    }


    protected void adicionaInfoParaView(List<Iniciativa> iniciativas) {
        Cidadao cidadao = daoCidadao.findByLogin("admin");

        for (Iniciativa iniciativa : iniciativas) {
            iniciativa.setSumario(daoIniciativa.calculaSumario(iniciativa.getId()));
            iniciativa.setSeguidaPeloRequisitante(cidadao.isInscritoEm(iniciativa));
        }
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
        if (cidade == null) {
            ObjectNode result = Json.newObject();
            result.put("error", "Not found " + id);
            return notFound(toJson(result));
        }

        Logger.debug("Acesso a " + cidade.getNome());

        return ok(toJson(cidade.getSimilares()));
    }


}
