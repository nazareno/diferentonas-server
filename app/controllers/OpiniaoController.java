package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.sun.corba.se.impl.naming.cosnaming.InterOperableNamingImpl;
import models.*;
import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import static play.libs.Json.toJson;
import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

/**
* Controller para ações relacionadas às opiniões dos usuários em iniciativas.
 */
public class OpiniaoController {

    private IniciativaService iniciativaService;
    private OpiniaoDAO opiniaoDAO;

    @Inject
    public OpiniaoController(IniciativaService service, OpiniaoDAO opiniaoDAO) {
        this.iniciativaService = service;
        this.opiniaoDAO = opiniaoDAO;
    }

    @Transactional(readOnly = true)
    public Result getOpinioes(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaService.find(idIniciativa);
        if(iniciativa == null){
            return notFound("Iniciativa não encontrada");
        }
        return ok(toJson(iniciativa.getOpinioes()));
    }

    @Transactional
    public Result addOpiniao(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaService.find(idIniciativa);
        if(iniciativa == null){
            return notFound("Iniciativa não encontrada");
        }

        JsonNode json = request().body().asJson();

        if(json == null) {
            return badRequest("Esperava receber json");
        }

        String conteudo = json.findPath("conteudo").textValue();
        if(conteudo == null){
            return badRequest("Falta parâmetro [conteudo]");
        }

        Logger.debug("Add opinião: " + conteudo.substring(0, 30) + "...");
        // Opiniao opiniao = Json.fromJson(json, Opiniao.class);
        Opiniao opiniao = new Opiniao();
        opiniao.setConteudo(conteudo);
        iniciativa.addOpiniao(opiniao);
        iniciativaService.flush(); // para que a opinião seja retornada com id
        return ok(toJson(opiniao));
    }

    /**
     * Usado para testes. Hoje não precisa de rota.
     */
    @Transactional
    public Result removeOpiniao(String idOpiniao) {
        Logger.debug("Removendo opinião " + idOpiniao);

        Opiniao paraRemover = opiniaoDAO.find(idOpiniao);
        if(paraRemover == null){
            return notFound("Opinião não encontrada");
        }

        Iniciativa iniciativa = paraRemover.getIniciativa();
        iniciativa.removeOpiniao(paraRemover);

        opiniaoDAO.delete(paraRemover);

        return ok(toJson(paraRemover));
    }
}
