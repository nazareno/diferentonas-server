package controllers;

import com.google.inject.Inject;
import models.*;
import play.Logger;
import play.db.jpa.Transactional;
import play.mvc.Result;

import static play.libs.Json.toJson;
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
        Opiniao opiniao = new Opiniao();
        iniciativa.addOpiniao(opiniao);
        //JPA.em().flush(); // TODO necessário?
        return ok(toJson(opiniao));
    }

    /**
     * Usado para testes. Hoje não precisa de rota.
     */
    @Transactional
    public Result removeOpiniao(Long idIniciativa, String idOpiniao) {
        Logger.debug("Removendo opinião " + idOpiniao);
        Iniciativa iniciativa = iniciativaService.find(idIniciativa);
        if(iniciativa == null){
            return notFound("Iniciativa não encontrada");
        }

        Opiniao paraRemover = opiniaoDAO.find(idOpiniao);
        if(paraRemover == null){
            return notFound("Opinião não encontrada");
        }

        // TODO melhor recuperar a iniciativa a partir da opinião; evita que a iniciativa esteja errada
        iniciativa.removeOpiniao(paraRemover);
        opiniaoDAO.delete(paraRemover);

        return ok(toJson(paraRemover));
    }
}
