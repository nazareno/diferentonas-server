package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import models.Cidadao;
import models.CidadaoDAO;
import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.IniciativaDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Configuration;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

public class IniciativaControllerTest extends WithAuthentication {

	private static final Long INICIATIVA_TESTE = -1L;
	private static final Long CIDADE_TESTE = -1L;

	@Before
	public void limpaBancoParaTestes() {
		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
		Configuration configuration = app.injector().instanceOf(Configuration.class);
		JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
		jpaAPI.withTransaction(() -> {
			String adminEmail = configuration.getString(Cidadao.ADMIN_EMAIL);
			Cidadao cidadao = cidadaoDAO.findByLogin(adminEmail);
			cidadao.setIniciativasAcompanhadas(new HashSet<>());
		});
		
		
		Cidade cidadeTeste = new Cidade(CIDADE_TESTE, "Pasárgada", "PB", 1f, 1f, 1f, 1f, 1000L, 1f, 1f, 0f);
		Iniciativa iniciativaTeste = new Iniciativa(INICIATIVA_TESTE, 2015, "iniciativa para melhorar condições da cidade", "programa do governo federal", "area de atuação", "fonte", "concedente", "status", false, 1000f, 1000f, new Date(), new Date(), new Date());
		cidadeTeste.addIniciativa(iniciativaTeste, new Date(), false);
		
		CidadeDAO cidadeDAO = app.injector().instanceOf(CidadeDAO.class);
		jpaAPI.withTransaction(() ->{
			if(cidadeDAO.find(-1L) == null){
				cidadeDAO.save(cidadeTeste);
			}
		});
	}

	@After
	public void limpaBancoDepoisDeTestes() {
		JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
		CidadeDAO cidadeDAO = app.injector().instanceOf(CidadeDAO.class);
		IniciativaDAO iniciativaDAO = app.injector().instanceOf(IniciativaDAO.class);
		jpaAPI.withTransaction(() ->{
			if(cidadeDAO.find(-1L) != null){
				cidadeDAO.remove(cidadeDAO.find(-1L));
			}
			if(iniciativaDAO.find(-1L) != null){
				iniciativaDAO.remove(iniciativaDAO.find(-1L));
			}
		});
	}

	@Test
	public void deveriaRetornarIniciativasSimilares() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.similares(INICIATIVA_TESTE, 5L).url()).method("GET"));
		assertEquals(Status.OK, result.status());
		JsonNode node = Json.parse(Helpers.contentAsString(result));
		assertTrue(node.isArray());
		
		Iterator<JsonNode> elements = node.elements();
		
		// Não existe iniciativa similar a essa
		assertFalse(elements.hasNext());
	}

	@Test
	public void deveriaFalharNaInscricaoNumaIniciativaInexistente() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(0L).url()).method("POST"));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaReportarCidadaoJaInscritoNumaIniciativa() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(INICIATIVA_TESTE).url()).method("POST"));
		assertEquals(Status.OK, result.status());

		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(INICIATIVA_TESTE).url()).method("POST"));
		assertEquals(Status.CONFLICT, result.status());
		
		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(INICIATIVA_TESTE).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());
	}

	@Test
	public void deveriaFalharNaRemocaoDaInscricaoNumaIniciativaInexistente() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(0L).url()).method("DELETE"));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaFalharAoRemoverCidadaoNaoInscritoNaIniciativa() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(INICIATIVA_TESTE).url()).method("DELETE"));
		assertEquals(Status.NOT_FOUND, result.status());
	}

	@Test
	public void deveriaRemoverCidadaoJaInscritoNumaIniciativa() {
		Result result = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(INICIATIVA_TESTE).url()).method("POST"));
		assertEquals(Status.OK, result.status());
		
		// não deveria estar aqui, mas não há como limpar banco de dados após todos os testes ainda.
		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(INICIATIVA_TESTE).url()).method("POST"));
		assertEquals(Status.CONFLICT, result.status());


		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(INICIATIVA_TESTE).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());
	}

	@Test
	public void deveRetornarIniciativaComInfoDeSeguidor() throws IOException {
		// caso hajs efeitos colaterais
		Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(INICIATIVA_TESTE).url()).method("DELETE"));

		Result result1 = Helpers.route(builder.uri(controllers.routes.IniciativaController.adicionaInscrito(INICIATIVA_TESTE).url()).method("POST"));
		assertEquals(contentAsString(result1), Http.Status.OK, result1.status());

		Result result2 = Helpers.route(builder.uri(controllers.routes.IniciativaController.getIniciativas(CIDADE_TESTE).url()).method("GET"));
		assertEquals(OK, result2.status());

		List<Iniciativa> iniciativas = new ObjectMapper()
				.readValue(contentAsString(result2), new TypeReference<List<Iniciativa>>() {});

		boolean encontrou = false;
		for (Iniciativa iniciativa :
				iniciativas) {
			if (INICIATIVA_TESTE.equals(iniciativa.getId())) {
				encontrou = true;
				assertTrue(iniciativa.isSeguidaPeloRequisitante());
			} else {
				assertFalse(iniciativa.isSeguidaPeloRequisitante());
			}
		}
		if (!encontrou) {
			fail("Não encontrou a iniciativa usada na cidade.");
		}
		
		Result result3 = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(INICIATIVA_TESTE).url()).method("DELETE"));
		assertEquals(Status.OK, result3.status());

	}

}
