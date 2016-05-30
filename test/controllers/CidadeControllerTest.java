package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import models.Cidade;
import module.MainModule;

import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	@Test
	public void testIndex() {
		Result result = Helpers.route(controllers.routes.CidadeController.index());
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType().get());
		assertEquals("utf-8", result.charset().get());
		assertTrue(contentAsString(result).contains("Olar"));
	}

	/**
	 * Test method for {@link controllers.CidadeController#getCidades()}.
	 */
	@Test
	public void testGetCidadeInexistente() {
		Result result = Helpers.route(controllers.routes.CidadeController.get(0L));
		assertEquals(NOT_FOUND, result.status());
	}

	/**
	 * Test method for {@link controllers.CidadeController#getCidades()}.
	 */
	@Test
	public void testGetCidadeExistente() {
		Result result = Helpers.route(controllers.routes.CidadeController.get(2513406L));
		assertEquals(OK, result.status());

		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertFalse("deveria retornar cidade no JSON de resposta", node.isNull());
		
		Cidade cidade = Json.fromJson(node, Cidade.class);
		
		assertEquals(2513406L, cidade.getId().longValue());
		assertEquals("Santa Luzia", cidade.getNome());
	}
}
