package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

import java.util.Iterator;

import models.Iniciativa;
import module.MainModule;

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
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		// mais similar é a própria iniciativa
		assertTrue(elements.hasNext());

		assertEquals(790448, Json.fromJson(elements.next(), Iniciativa.class).getId());
		assertEquals(796377, Json.fromJson(elements.next(), Iniciativa.class).getId());
		assertEquals(797931, Json.fromJson(elements.next(), Iniciativa.class).getId());
		assertEquals(798784, Json.fromJson(elements.next(), Iniciativa.class).getId());
		assertEquals(804599, Json.fromJson(elements.next(), Iniciativa.class).getId());
		assertFalse(elements.hasNext());
	}

}
