package controllers;

import static play.libs.Json.toJson;

import java.util.List;

import javax.inject.Inject;

import models.Cidadao;
import models.CidadaoDAO;
import models.Novidade;
import models.NovidadeDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * Controller da linha do tempo dos cidadãos.
 */
public class FeedController extends Controller {

    @Inject
    private NovidadeDAO dao;
    @Inject
    private CidadaoDAO daoCidadao;


    @Transactional
    public Result getNovidades(int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        Cidadao cidadao = daoCidadao.findByLogin("admin");
        List<Novidade> notificacoes = dao.find(cidadao.getId(), pagina, tamanhoPagina);
        return ok(toJson(notificacoes));
    }

}
