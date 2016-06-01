package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
 * Testes para {@link MensagemController}.
 * 
 * @author ricardo
 */
public class MensagemControllerTest extends WithApplication {
	
	private Mensagem primeiraMensagem;
	private Mensagem segundaMensagem;

	@Before
	public void criaMensagensSemID(){
		this.primeiraMensagem = new Mensagem();
		this.primeiraMensagem.setTitulo("Primeira mensagem");
		this.primeiraMensagem.setConteudo("Miga, lá vem a dezembrada!!!");
		this.primeiraMensagem.setAutor("Ministério da Justiça");
		this.primeiraMensagem.setCriadaEm(null);
		
		this.segundaMensagem = new Mensagem();
		this.segundaMensagem.setTitulo("Segunda Mensagem");
		this.segundaMensagem.setConteudo("Continue de olho na sua cidade... ");
		this.segundaMensagem.setAutor("Ministério da Justiça");
		this.segundaMensagem.setCriadaEm(null);
	}
	
	@After
	public void limpaBancoAposTeste(){
		if(primeiraMensagem.getId() != null){
			Helpers.route(controllers.routes.MensagemController.delete(primeiraMensagem.getId().toString()));
		}
		if(segundaMensagem.getId() != null){
			Helpers.route(controllers.routes.MensagemController.delete(segundaMensagem.getId().toString()));
		}
	}
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	@Test
	public void deveRetornarNenhumaMensagem() {
		Result result = Helpers.route(controllers.routes.MensagemController.getMensagens(10L));
		assertEquals(OK, result.status());
		assertNotNull(Helpers.contentAsString(result));
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
	}

	@Test
	public void deveAdicionarUmaMensagem() {
		Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(primeiraMensagem)));
		
		// insere
		assertEquals(CREATED, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertFalse("deveria retornar mensagem no JSON de resposta", node.isNull());
		
		Mensagem mensagemCriada = Json.fromJson(node, Mensagem.class);
		
		// id foi gerado?
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
		assertEquals(primeiraMensagem.getTitulo(), mensagemCriada.getTitulo());
		this.primeiraMensagem = mensagemCriada;
	}

	@Test
	public void deveRetornarMensagensOrdenadasPelaMaisRecente() {
		Result result = null;
		
		// insere primeira
		result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(primeiraMensagem)));
		assertEquals(CREATED, result.status());
		this.primeiraMensagem = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", this.primeiraMensagem.getId());

		// insere segunda
		result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(segundaMensagem)));
		assertEquals(CREATED, result.status());
		this.segundaMensagem = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", this.segundaMensagem.getId());
		
		// recupera mensagens
		result = Helpers.route(controllers.routes.MensagemController.getMensagens(10L));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		// mais recente == segunda
		assertTrue(elements.hasNext());
		Mensagem segunda = Json.fromJson(elements.next(), Mensagem.class);
		assertEquals(this.segundaMensagem, segunda);
		
		// mais antiga == primeira
		assertTrue(elements.hasNext());
		Mensagem primeira = Json.fromJson(elements.next(), Mensagem.class);
		assertEquals(this.primeiraMensagem, primeira);
	}

}
