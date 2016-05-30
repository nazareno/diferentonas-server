package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import models.Mensagem;
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
public class CoachingControllerTest extends WithApplication {
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	@Test
	public void testGetMensagensComBancoVazio() {
		Result result = Helpers.route(controllers.routes.CoachingController.getMensagens(10L));
		assertEquals(OK, result.status());
		assertNotNull(Helpers.contentAsString(result));
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
	}

	@Test
	public void testSaveMensagem() {
		Mensagem mensagem = new Mensagem("Fica de olho na sua cidade...", "Miga, lá vem a dezembrada!!!", "Ministério da Justiça");
		
		Result result = route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(mensagem)));
		
		assertEquals(CREATED, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertFalse("deveria retornar mensagem no JSON de resposta", node.isNull());
		
		Mensagem mensagemCriada = Json.fromJson(node, Mensagem.class);
		
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
		assertEquals(mensagem.getTitulo(), mensagemCriada.getTitulo());
	}

	@Test
	public void testGetUltimaMensagem() {
		Json.parse(Helpers.contentAsString(Helpers.route(controllers.routes.CoachingController.getMensagens(10L))));
		
		Mensagem mensagemAntiga = new Mensagem("Fique de olho na sua cidade... " + System.currentTimeMillis(), "Primeira mensagem", "Ministério da Justiça");
		assertEquals(CREATED, route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(mensagemAntiga))).status());

		Result result = Helpers.route(controllers.routes.CoachingController.getMensagens(1L));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		assertTrue(node.elements().hasNext());
		
		assertEquals(mensagemAntiga, Json.fromJson(node.elements().next(), Mensagem.class));
		
		Mensagem mensagemNova = new Mensagem("Continue de olho na sua cidade... " + System.currentTimeMillis(), "Segunda mensagem", "Ministério da Justiça");
		assertEquals(CREATED, route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(mensagemNova))).status());
		
		result = Helpers.route(controllers.routes.CoachingController.getMensagens(1L));
		
		assertEquals(OK, result.status());
		node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		assertTrue(node.elements().hasNext());
		Mensagem mensagem = Json.fromJson(node.elements().next(), Mensagem.class);
		
		assertNotEquals(mensagemAntiga.getId(), mensagem.getId());
		assertEquals(mensagemNova.getId(), mensagem.getId());
	}
}
