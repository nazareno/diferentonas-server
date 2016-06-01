package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import play.mvc.Http.Status;

import java.util.Iterator;

import models.Iniciativa;
import module.MainModule;

import org.junit.Ignore;
import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;

public class IniciativaControllerTest extends WithApplication {

	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	@Test
	public void deveriaRetornarIniciativasSimilares() {

		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.similares(id, 5L));
		assertEquals(Status.OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		// mais similar é a própria iniciativa
		assertTrue(elements.hasNext());

		assertEquals(790448L, Json.fromJson(elements.next(), Iniciativa.class).getId().longValue());
		assertEquals(796377L, Json.fromJson(elements.next(), Iniciativa.class).getId().longValue());
		assertEquals(797931L, Json.fromJson(elements.next(), Iniciativa.class).getId().longValue());
		assertEquals(798784L, Json.fromJson(elements.next(), Iniciativa.class).getId().longValue());
		assertEquals(804599L, Json.fromJson(elements.next(), Iniciativa.class).getId().longValue());
		assertFalse(elements.hasNext());
	}

	@Test
	public void deveriaFalharNaInscricaoNumaIniciativaInexistente() {

		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(0L));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	@Ignore("Executar só quando clear do BD de teste estiver implementado")
	public void deveriaInscreverCidadaoNaIniciativa() {

		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());
	}

	@Test
	@Ignore("Executar só quando clear do BD de teste estiver implementado")
	public void deveriaReportarCidadaoJaInscritoNumaIniciativa() {

		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());

		result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.CONFLICT, result.status());
	}

	@Test
	public void deveriaFalharNaRemocaoDaInscricaoNumaIniciativaInexistente() {

		Result result = Helpers.route(controllers.routes.IniciativaController.removeInscrito(0L));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaFalharAoRemoverCidadaoNaoInscritoNaIniciativa() {

		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.removeInscrito(id));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaRemoverCidadaoJaInscritoNumaIniciativa() {

		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());
		
		// não deveria estar aqui, mas não há como limpar banco de dados após todos os testes ainda.
		result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.CONFLICT, result.status());


		result = Helpers.route(controllers.routes.IniciativaController.removeInscrito(id));
		assertEquals(Status.OK, result.status());
	}

}
