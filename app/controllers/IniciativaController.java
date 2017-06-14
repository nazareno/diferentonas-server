package controllers;

import static play.libs.Json.toJson;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import models.Cidadao;
import models.CidadaoDAO;
import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.IniciativaDAO;

import org.hibernate.Hibernate;

import play.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

public class IniciativaController extends Controller {

    private IniciativaDAO iniciativaDAO;
    private CidadaoDAO cidadaoDAO;
    private CidadeDAO cidadeDAO;

    @Inject
    public IniciativaController(IniciativaDAO iniciativaDAO, CidadaoDAO cidadaoDAO, CidadeDAO cidadeDAO) {
        this.iniciativaDAO = iniciativaDAO;
        this.cidadaoDAO = cidadaoDAO;
        this.cidadeDAO = cidadeDAO;
    }

    @Transactional(readOnly = true)
    public Result get(Long id) {
        Iniciativa iniciativa = iniciativaDAO.find(id);
        if(iniciativa == null){
            return(notFound("Iniciativa " + id))
        }
        return ok(toJson(iniciativa));
    }

    @Transactional(readOnly = true)
    public Result getIniciativas(Long idCidade) {
        Cidade cidade = cidadeDAO.findComIniciativas(idCidade);
        if (cidade == null) {
            return notFound("Cidade " + idCidade);
        }

        Logger.debug("Iniciativas para " + cidade.getNome() + ": " + cidade.getIniciativas().size());
        return ok(toJson(iniciativaDAO.adicionaSumarios(cidade.getIniciativas(), getCidadaoLogado())));
    }

    @Transactional(readOnly = true)
    public CompletionStage<Result> similares(Long id, Long quantidade) {
		return CompletableFuture.supplyAsync(
				() -> (iniciativaDAO.findSimilares(id, quantidade, null)))
				.thenApply((iniciativas) -> {
					ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
					for (Iniciativa iniciativa : iniciativas) {
						ObjectNode node = (ObjectNode)Json.toJson(iniciativa);
						node.set("cidade", Json.toJson(iniciativa.getCidade()));
						arrayNode.add(node);
					}
					return ok(arrayNode);
				});
    }

    @Transactional
    public Result adicionaInscrito(Long idIniciativa) {
        Iniciativa iniciativa = iniciativaDAO.find(idIniciativa);
        if (iniciativa == null) {
            return notFound();
        }
        Cidadao cidadao = getCidadaoLogado();
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
        Cidadao cidadao = getCidadaoLogado();
        boolean desinscreveu = cidadao.desinscreverDe(iniciativa);
        return desinscreveu ? ok() : notFound("Cidadão " + cidadao.getId() + " não está inscrito em " + iniciativa.getId());
    }

	private Cidadao getCidadaoLogado() {
		return null;
	}

}
