package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Iniciativa;
import models.IniciativaService;
import models.Opiniao;
import models.OpiniaoDAO;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Result;

import static play.libs.Json.toJson;
import static play.mvc.Controller.request;
import static play.mvc.Results.*;

/**
* Controller para ações relacionadas às opiniões dos usuários em iniciativas.
 */
public class OpiniaoController {

    private IniciativaService iniciativaService;
    private OpiniaoDAO opiniaoDAO;
    private FormFactory formFactory;

    @Inject
    public OpiniaoController(IniciativaService service, OpiniaoDAO opiniaoDAO, FormFactory formFactory) {
        this.iniciativaService = service;
        this.opiniaoDAO = opiniaoDAO;
        this.formFactory = formFactory;
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
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Esperava receber json");
        }

        // Usando anotações de validação do play
        Form<Opiniao> form = formFactory.form(Opiniao.class);
        Form<Opiniao> comDados = form.bind(json);
        if(comDados.hasErrors()){
            Logger.debug("Submissão com erros: " + json.toString() + "; Erros: " + comDados.errorsAsJson());
            return badRequest(comDados.errorsAsJson());
        }
        Opiniao opiniao = comDados.get();

        Iniciativa iniciativa = iniciativaService.find(idIniciativa);
        if(iniciativa == null){
            return notFound("Iniciativa não encontrada");
        }

        Logger.debug("Add opinião na iniciativa " + iniciativa.getId());

        iniciativa.addOpiniao(opiniao);
        opiniao.setIniciativa(iniciativa);
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

        removeOpiniaoDoBD(paraRemover);

        return ok(toJson(paraRemover));
    }

    private void removeOpiniaoDoBD(Opiniao paraRemover) {
        Iniciativa iniciativa = paraRemover.getIniciativa();
        iniciativa.removeOpiniao(paraRemover);
        opiniaoDAO.delete(paraRemover);
    }

    /**
     * Usado para testes. Hoje não precisa e não tem rota.
     */
    @Transactional
    public Result removeOpinioes(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaService.find(idIniciativa);
        for (Opiniao o: iniciativa.getOpinioes()) {
            removeOpiniaoDoBD(o);
        }
        return ok();
    }
}
