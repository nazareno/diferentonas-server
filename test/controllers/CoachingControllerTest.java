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

import java.util.Iterator;

import models.Mensagem;
import module.MainModule;

import org.junit.After;
import org.junit.Before;
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
	
	private Mensagem primeiraMensagem;
	private Mensagem segundaMensagem;

	@Before
	public void setUp(){
		this.primeiraMensagem = new Mensagem();
		this.primeiraMensagem.setTitulo("Primeira mensagem");
		this.primeiraMensagem.setConteudo("Miga, lá vem a dezembrada!!!");
		this.primeiraMensagem.setAutor("Ministério da Justiça");
		
		this.segundaMensagem = new Mensagem();
		this.segundaMensagem.setTitulo("Segunda Mensagem");
		this.segundaMensagem.setConteudo("Continue de olho na sua cidade... ");
		this.segundaMensagem.setAutor("Ministério da Justiça");
	}
	
	@After
	public void tearDown(){
//		if(primeiraMensagem.getId() != null){
//			Helpers.route(controllers.routes.CoachingController.delete(primeiraMensagem.getId().toString()));
//		}
//		if(segundaMensagem.getId() != null){
//			Helpers.route(controllers.routes.CoachingController.delete(segundaMensagem.getId().toString()));
//		}
	}
	
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
		Result result = route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(primeiraMensagem)));
		
		assertEquals(CREATED, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertFalse("deveria retornar mensagem no JSON de resposta", node.isNull());
		
		Mensagem mensagemCriada = Json.fromJson(node, Mensagem.class);
		
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
		assertEquals(primeiraMensagem.getTitulo(), mensagemCriada.getTitulo());
		this.primeiraMensagem = mensagemCriada;
	}

	@Test
	public void testGetEmOrdem() {
		Result result = null;
		
		result = route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(primeiraMensagem)));
		assertEquals(CREATED, result.status());
		this.primeiraMensagem = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", this.primeiraMensagem.getId());

		result = route(fakeRequest(controllers.routes.CoachingController.save()).bodyJson(Json.toJson(segundaMensagem)));
		assertEquals(CREATED, result.status());
		this.segundaMensagem = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", this.segundaMensagem.getId());
		
		
		result = Helpers.route(controllers.routes.CoachingController.getMensagens(10L));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		Iterator<JsonNode> elements = node.elements();
		assertTrue(elements.hasNext());
		Mensagem segunda = Json.fromJson(elements.next(), Mensagem.class);
		assertTrue(elements.hasNext());
		Mensagem primeira = Json.fromJson(elements.next(), Mensagem.class);
		
		assertEquals(this.segundaMensagem, segunda);
		assertEquals(this.primeiraMensagem, primeira);
	}

}
