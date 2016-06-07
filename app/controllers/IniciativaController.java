package controllers;

import static play.libs.Json.toJson;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import models.Cidadao;
import models.CidadaoDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class IniciativaController extends Controller {

    private IniciativaDAO iniciativaDAO;
    private CidadaoDAO cidadaoDAO;

    @Inject
    public IniciativaController(IniciativaDAO iniciativaDAO, CidadaoDAO cidadaoDAO) {
        this.iniciativaDAO = iniciativaDAO;
        this.cidadaoDAO = cidadaoDAO;
    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
    	iniciativaDAO.calculaSumario(id);
        return ok(toJson(iniciativaDAO.find(id)));
    }

    @Transactional(readOnly = true)
    public CompletionStage<Result> similares(Long id, Long quantidade) {
        return CompletableFuture.supplyAsync(
                () -> iniciativaDAO.findSimilares(id, quantidade)).thenApply(
                (i) -> ok(toJson(i)));
    }

    @Transactional
    public Result adicionaInscrito(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound();
        }
        Cidadao cidadao = getCidadaoAtual();
        boolean inscreveu = cidadao.inscreverEm(iniciativa);
        return inscreveu ? ok() : status(CONFLICT,
                "Cidadão " + cidadao.getId() + " já inscrito em " + iniciativa.getId());
    }

    @Transactional
    public Result removeInscrito(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound();
        }
        Cidadao cidadao = getCidadaoAtual();
        boolean desinscreveu = cidadao.desinscreverDe(iniciativa);
        return desinscreveu ? ok() : notFound("Cidadão " + cidadao.getId() + " não está inscrito em " + iniciativa.getId());
    }

    /**
     * Só funciona dentro de uma transação. Use enquanto não houver lógica de login.
     *
     * @return o Cidadao da sessão
     */
    protected Cidadao getCidadaoAtual() {

        // não faz nada com o access-token
        // String loginDoCidadao = session("cidadao");
        //TODO podemos precisar disso depois do login...
        return cidadaoDAO.findByLogin("admin");
    }

}
