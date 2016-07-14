package controllers;

import static play.libs.Json.toJson;

import java.util.List;

import javax.inject.Inject;

import models.Cidadao;
import models.CidadaoDAO;
import models.Novidade;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Controller da linha do tempo dos cidadãos.
 */
@Security.Authenticated(Secured.class)
public class FeedController extends Controller {

    @Inject
    private CidadaoDAO daoCidadao;


    @Transactional
    public Result getNovidades(int pagina, int tamanhoPagina) {
        if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
            return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. " +
                    "Tamannho de página deve ser menor ou igual a 500.");
        }

        Cidadao cidadao = daoCidadao.findByLogin("admin");
//        List<Novidade> novidades = daoCidadao.getNovidadesRecentes(UUID.fromString("65fe8be9-81ea-4f2c-b30c-8c3336107793"), pagina, tamanhoPagina);
        List<Novidade> novidades = daoCidadao.getNovidadesRecentes(cidadao.getId(), pagina, tamanhoPagina);
		return ok(toJson(novidades));
    }
}
