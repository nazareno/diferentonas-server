package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import models.Mensagem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.libs.Json;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.util.WithAuthentication;

/**
 * Testes para {@link MensagemController}.
 * 
 * @author ricardo
 */
public class MensagemControllerTest extends WithAuthentication {
	
	private Mensagem templateMensagem;
	
	private List<String> idsDasMensagensParaDeletar; 


	@Before
	public void criaMensagensSemID(){
		
		this.idsDasMensagensParaDeletar = new ArrayList<String>();
		
		this.templateMensagem = new Mensagem();
		this.templateMensagem.setConteudo("Miga, lá vem a dezembrada!!!");
		this.templateMensagem.setAutor("Ministério da Justiça");
		this.templateMensagem.setCriadaEm(null);
		
	}
	
	@After
	public void limpaBancoAposTeste(){
		
		for (String id : idsDasMensagensParaDeletar) {
			Helpers.route(builder.uri(controllers.routes.MensagemController.delete(id).url()).method("DELETE"));
		}
		idsDasMensagensParaDeletar.clear();
	}
	
	@Test
	public void deveRetornarNenhumaMensagem() {
		Result result = Helpers.route(builder.uri(controllers.routes.MensagemController.getMensagens(0, 10).url()).method("GET"));
		assertEquals(OK, result.status());
		assertNotNull(Helpers.contentAsString(result));
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
	}

	@Test
	public void deveAdicionarUmaMensagem() {
		templateMensagem.setTitulo("Titulo OK");
		Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
		
		// insere
		assertEquals(CREATED, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertFalse("deveria retornar mensagem no JSON de resposta", node.isNull());
		
		Mensagem mensagemCriada = Json.fromJson(node, Mensagem.class);
		
		// id foi gerado?
		assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
		assertEquals(templateMensagem.getTitulo(), mensagemCriada.getTitulo());
		
		this.idsDasMensagensParaDeletar.add(mensagemCriada.getId().toString());
	}

	@Test
	public void deveFalharAoAdicionarUmaMensagemComTituloGigante() {
		StringBuilder conteudoInvalido = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			conteudoInvalido.append("ASDF ");
		}
		
		templateMensagem.setTitulo(conteudoInvalido.toString());

		Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
		assertEquals(Status.BAD_REQUEST, result.status());
	}

	@Test
	public void deveFalharAoAdicionarUmaMensagemComTituloVazio() {
		
		templateMensagem.setTitulo("");

		Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
		assertEquals(Status.BAD_REQUEST, result.status());
	}

	@Test
	public void deveFalharAoAdicionarUmaMensagemComTituloNull() {
		
		templateMensagem.setTitulo(null);

		Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
		assertEquals(Status.BAD_REQUEST, result.status());
	}

	@Test
	public void deveRetornarMensagensOrdenadasPelaMaisRecente() {
		
		
		// Insere 20 mensagens com títulos diferentes
		for (int i = 1; i < 21; i++) {
			templateMensagem.setTitulo("" + i);

			Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
			assertEquals(CREATED, result.status());
			Mensagem mensagemCriada = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
			assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
			this.idsDasMensagensParaDeletar.add(mensagemCriada.getId().toString());
			
		}
		
		Result result = Helpers.route(builder.uri(controllers.routes.MensagemController.getMensagens(0, 3).url()).method("GET"));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		for (int i = 20; i > 17; i--) {
			assertTrue(elements.hasNext());
			Mensagem mensagem = Json.fromJson(elements.next(), Mensagem.class);
			assertEquals("deveria ter retornado a mensagem #" + i, ""+i, mensagem.getTitulo());
		}
	}

	@Test
	public void deveRetornarMensagensDaPaginaCorreta() {
		
		
		// Insere 20 mensagens com títulos diferentes
		for (int i = 1; i < 21; i++) {
			templateMensagem.setTitulo("" + i);

			Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
			assertEquals(CREATED, result.status());
			Mensagem mensagemCriada = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
			assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
			this.idsDasMensagensParaDeletar.add(mensagemCriada.getId().toString());
			
		}
		
		Result result = Helpers.route(builder.uri(controllers.routes.MensagemController.getMensagens(1, 3).url()).method("GET"));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		for (int i = 17; i > 14; i--) {
			assertTrue(elements.hasNext());
			Mensagem mensagem = Json.fromJson(elements.next(), Mensagem.class);
			assertEquals("deveria ter retornado a mensagem #" + i, ""+i, mensagem.getTitulo());
		}
	}

	@Test
	public void deveRetornarListaVaziaParaPaginaInexistente() {
		
		
		// Insere 20 mensagens com títulos diferentes
		for (int i = 1; i < 21; i++) {
			templateMensagem.setTitulo("" + i);

			Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
			assertEquals(CREATED, result.status());
			Mensagem mensagemCriada = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
			assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
			this.idsDasMensagensParaDeletar.add(mensagemCriada.getId().toString());
			
		}
		
		Result result = Helpers.route(builder.uri(controllers.routes.MensagemController.getMensagens(10, 3).url()).method("GET"));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		assertFalse(elements.hasNext());
	}

	@Test
	public void deveRetornarListaComMensagensRestantes() {
		
		
		// Insere 20 mensagens com títulos diferentes
		for (int i = 1; i < 21; i++) {
			templateMensagem.setTitulo("" + i);

			Result result = route(fakeRequest(controllers.routes.MensagemController.save()).bodyJson(Json.toJson(templateMensagem)).header("X-Auth-Token", token));
			assertEquals(CREATED, result.status());
			Mensagem mensagemCriada = Json.fromJson(Json.parse(Helpers.contentAsString(result)), Mensagem.class);
			assertNotNull("mensagem deveria conter um UUID gerado pelo BD", mensagemCriada.getId());
			this.idsDasMensagensParaDeletar.add(mensagemCriada.getId().toString());
			
		}
		
		Result result = Helpers.route(builder.uri(controllers.routes.MensagemController.getMensagens(6, 3).url()).method("GET"));
		
		assertEquals(OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();

		for (int i = 2; i > 0; i--) {
			assertTrue(elements.hasNext());
			Mensagem mensagem = Json.fromJson(elements.next(), Mensagem.class);
			assertEquals("deveria ter retornado a mensagem #" + i, ""+i, mensagem.getTitulo());
		}
		
		assertFalse(elements.hasNext());
	}

}
