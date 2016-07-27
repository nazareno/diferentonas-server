package controllers;

import static play.libs.Json.toJson;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import models.Cidadao;
import models.CidadaoDAO;
import models.Novidade;
import models.TipoDaNovidade;
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
		
		List<Novidade> recentes = daoCidadao.getNovidadesRecentes(cidadao.getId(),
				pagina, tamanhoPagina);
		
		for (Novidade novidade : recentes) {
			if(TipoDaNovidade.NOVA_OPINIAO.equals(novidade.getTipo())){
				novidade.getOpiniao().setApoiada(cidadao);
			}
		}
		
		return ok(toJson(recentes));
	}
}
