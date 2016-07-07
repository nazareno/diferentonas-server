package controllers;

import static play.libs.Json.toJson;
import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

import java.util.Iterator;
import java.util.List;

import models.Cidadao;
import models.CidadaoDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import models.Opiniao;
import models.OpiniaoDAO;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.jpa.Transactional;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

/**
 * Controller para ações relacionadas às opiniões dos usuários em iniciativas.
 */
public class OpiniaoController {

    @Inject
    private CidadaoDAO cidadaoDAO;
    @Inject
    private IniciativaDAO iniciativaDAO;
    @Inject
    private OpiniaoDAO opiniaoDAO;
    @Inject
    private FormFactory formFactory;

    @Transactional(readOnly = true)
    public Result getOpinioes(Long idIniciativa, int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound("Iniciativa não encontrada");
        }

        List<Opiniao> opinioes = opiniaoDAO.findByIniciativa(idIniciativa, pagina, tamanhoPagina);
        return ok(toJson(opinioes));
    }

    @Transactional
    public Result addOpiniao(Long idIniciativa) {
        JsonNode json = request().body().asJson();
        if (json == null) {
            return badRequest("Esperava receber json");
        }

        // Usando anotações de validação do play
        Form<Opiniao> form = formFactory.form(Opiniao.class);
        Form<Opiniao> comDados = form.bind(json);
        if (comDados.hasErrors()) {
            Logger.debug("Submissão com erros: " + json.toString() + "; Erros: " + comDados.errorsAsJson());
            return badRequest(comDados.errorsAsJson());
        }
        Opiniao opiniao = comDados.get();

        opiniao.setAutor(getUsuarioLogado());

        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound("Iniciativa não encontrada");
        }

        Logger.debug("Add opinião na iniciativa " + iniciativa.getId());

        iniciativa.addOpiniao(opiniao);
        iniciativaDAO.flush(); // para que a opinião seja retornada com id
        
        return ok(toJson(opiniao));
    }

    private Cidadao getUsuarioLogado() {
        return cidadaoDAO.findByLogin("admin");
    }

    /**
     * Usado para testes. Hoje não precisa de rota.
     */
    @Transactional
    public Result removeOpiniao(String idOpiniao) {
        Logger.debug("Removendo opinião " + idOpiniao);

        Opiniao paraRemover = opiniaoDAO.find(idOpiniao);
        if (paraRemover == null) {
            return notFound("Opinião não encontrada");
        }

        Iniciativa iniciativa = paraRemover.getIniciativa();
        iniciativa.removeOpiniao(paraRemover);
        removeOpiniaoDoBD(paraRemover);

        return ok(toJson(paraRemover));
    }

    private void removeOpiniaoDoBD(Opiniao paraRemover) {
        opiniaoDAO.delete(paraRemover);
    }

    /**
     * Usado para testes. Hoje não precisa e não tem rota.
     */
    @Transactional
    public Result removeOpinioes(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        Logger.debug(""+iniciativa);
        List<Opiniao> opinioes = iniciativa.getOpinioes();
        Logger.debug(""+opinioes);
		Iterator<Opiniao> it = opinioes.iterator();
        while (it.hasNext()){
            Opiniao o = it.next();
            removeOpiniaoDoBD(o);
            it.remove();
        }
        return ok();
    }
}
