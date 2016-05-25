package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import models.Mensagem;
import module.MainModule;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.Application;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Testes do controller.
 */
public class CoachingControllerTest extends WithApplication {
	
	private CoachingController controller;

	private JPAApi jpaAPI;

	@Before
	public void setUp() {
		this.controller = app.injector().instanceOf(CoachingController.class);
		this.jpaAPI = app.injector().instanceOf(JPAApi.class);
	}
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	/**
	 * Test method for {@link controllers.CoachingController#getMensagens(Long)}.
	 */
	@Test
	public void testGetMensagensComBancoVazio() {
		jpaAPI.withTransaction(() ->{
			Result result = controller.getMensagens(10L);
			assertEquals(OK, result.status());
			JsonNode node = Json.parse(Helpers.contentAsString(result));
			assertFalse(node.elements().hasNext());
		});
	}

	/**
	 * Test method for {@link controllers.CoachingController#getMensagens(Long)}.
	 */
	@Test
	@Ignore
	public void testSaveMensagem() {
		jpaAPI.withTransaction(() ->{
			Result result = controller.save();
			assertEquals(OK, result.status());
			JsonNode node = Json.parse(Helpers.contentAsString(result));
			assertFalse(node.elements().hasNext());
		});
	}

	/**
	 * Test method for {@link controllers.CoachingController#getMensagens(Long)}.
	 */
	@Test
	@Ignore
	public void testGetMensagensComUmaMensagem() {
		jpaAPI.withTransaction(() ->{
			Result result = controller.getMensagens(1L);
			assertEquals(OK, result.status());
			JsonNode node = Json.parse(Helpers.contentAsString(result));
			assertTrue(node.elements().hasNext());
			JsonNode messageJSON = node.elements().next();
			Mensagem mensagem = Json.fromJson(messageJSON, Mensagem.class);
			assertNotNull(mensagem);
			assertNotNull(mensagem.getId());
		});
	}
}
