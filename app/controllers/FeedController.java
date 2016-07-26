package controllers;

import static play.libs.Json.toJson;

import java.util.UUID;

import javax.inject.Inject;

import models.Cidadao;
import models.CidadaoDAO;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Controller da linha do tempo dos cidadãos.
 */
@Security.Authenticated(AcessoCidadao.class)
public class FeedController extends Controller {

	@Inject
	private CidadaoDAO daoCidadao;

	@Transactional
	public Result getNovidades(int pagina, int tamanhoPagina) {
		if (pagina < 0 || tamanhoPagina <= 0 || tamanhoPagina > 500) {
			return badRequest("Página, Tamanho de página e Máximo de resultados devem ser maiores que zero. "
					+ "Tamanho de página deve ser menor ou igual a 500.");
		}
		Cidadao cidadao = daoCidadao
				.find(UUID.fromString(request().username()));
		
		return ok(toJson(daoCidadao.getNovidadesRecentes(cidadao.getId(),
				pagina, tamanhoPagina)));
	}
}
