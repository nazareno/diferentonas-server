package controllers;

import static play.libs.Json.toJson;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import models.*;
import org.hibernate.Hibernate;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Controller da linha do tempo dos cidadãos.
 */
public class FeedController extends Controller {

    @Inject
    private OpiniaoDAO daoOpiniao;
    @Inject
    private CidadaoDAO daoCidadao;


    @Transactional
    public Result getNovidades(int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        Cidadao cidadao = daoCidadao.findByLogin("admin");
        List<Opiniao> opinioesMaisNovas = daoOpiniao.findRecentes(cidadao.getId(), pagina, tamanhoPagina);
        List<Novidade> notificacoes = opinioesMaisNovas.stream()
                .map(opiniao -> new Novidade(opiniao))
                .collect(Collectors.toList());
        return ok(toJson(notificacoes));
    }

}
