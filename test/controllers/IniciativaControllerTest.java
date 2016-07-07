package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import models.Cidadao;
import models.CidadaoDAO;
import models.Iniciativa;
import module.MainModule;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.Application;
import play.Logger;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IniciativaControllerTest extends WithApplication {

	@Before
	public void limpaBancoParaTestes() {
		CidadaoDAO cidadaoDAO= app.injector().instanceOf(CidadaoDAO.class);
		JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
		jpaAPI.withTransaction(() -> {
			Cidadao cidadao = cidadaoDAO.findByLogin("admin");
			cidadao.setIniciativasAcompanhadas(new HashSet<>());
		});
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

		assertNotNull(Json.fromJson(elements.next(), Iniciativa.class));
		assertNotNull(Json.fromJson(elements.next(), Iniciativa.class));
		assertNotNull(Json.fromJson(elements.next(), Iniciativa.class));
		assertNotNull(Json.fromJson(elements.next(), Iniciativa.class));
		assertNotNull(Json.fromJson(elements.next(), Iniciativa.class));
		assertFalse(elements.hasNext());
	}

	@Test
	public void deveriaFalharNaInscricaoNumaIniciativaInexistente() {
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(0L));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaInscreverCidadaoNaIniciativa() {
		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());
	}

	@Test
	@Ignore
	public void deveriaInscreverDoisCidadaosNaMesmaIniciativa() {
		long id = 797935L;
		Result result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());
		
		//FIXME troca usuário e resubmete
		result = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(id));
		assertEquals(Status.OK, result.status());
}

	@Test
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

	@Test
	public void deveRetornarIniciativaComInfoDeSeguidor() throws IOException {
		long idCidade = 2807402L;
		long idIniciativa = 797935L;
		// caso hajs efeitos colaterais
		Helpers.route(controllers.routes.IniciativaController.removeInscrito(idIniciativa));

		Result result1 = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(idIniciativa));
		assertEquals(contentAsString(result1), Http.Status.OK, result1.status());

		Result result2 = Helpers.route(controllers.routes.IniciativaController.getIniciativas(idCidade));
		assertEquals(OK, result2.status());
		Logger.info("%%%% " + contentAsString(result2));

		List<Iniciativa> iniciativas = new ObjectMapper()
				.readValue(contentAsString(result2), new TypeReference<List<Iniciativa>>() {});

		boolean encontrou = false;
		for (Iniciativa iniciativa :
				iniciativas) {
			if (iniciativa.getId() == idIniciativa) {
				encontrou = true;
				assertTrue(iniciativa.isSeguidaPeloRequisitante());
			} else {
				assertFalse(iniciativa.isSeguidaPeloRequisitante());
			}
		}
		if (!encontrou) {
			fail("Não encontrou a iniciativa usada na cidade.");
		}
	}

}
