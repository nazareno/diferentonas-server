package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

import models.Cidade;
import models.CidadeDAO;
import models.Novidade;
import models.Score;
import models.TipoDaNovidade;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {
	
    private JPAApi jpaAPI;
    private CidadeDAO dao;

    private String conteudoExemplo = "Essa iniciativa é absolutamente estrogonófica para a cidade.";


    @Before
    public void setUp() {
        this.dao = app.injector().instanceOf(CidadeDAO.class);
        this.jpaAPI = app.injector().instanceOf(JPAApi.class);
    }

    @Test
    public void testIndex() {
        Result result = Helpers.route(controllers.routes.CidadeController.index());
        assertEquals(OK, result.status());
        assertEquals("text/plain", result.contentType().get());
        assertEquals("utf-8", result.charset().get());
        assertTrue(contentAsString(result).contains("Olar"));
    }

    @Test
    public void deveRetornarNotFoundParaCidadeInexistente() {
        Result result = Helpers.route(controllers.routes.CidadeController.get(0L));
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void deveRetornarCidadeExistente() {
        Result result = Helpers.route(controllers.routes.CidadeController.get(2513406L));
        assertEquals(OK, result.status());

        JsonNode node = Json.parse(Helpers.contentAsString(result));
        assertFalse("deveria retornar cidade no JSON de resposta", node.isNull());

        Cidade cidade = Json.fromJson(node, Cidade.class);

        assertEquals(2513406L, cidade.getId().longValue());
        assertEquals("Santa Luzia", cidade.getNome());
    }


    @Test
    public void deveRetornarNenhumaNovidade() throws JsonParseException, JsonMappingException, IOException {
        Result result = Helpers.route(controllers.routes.CidadeController.getNovidades(2513406L, 1, 10));
        assertEquals(OK, result.status());

        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Novidade>>() {});
        
        assertTrue(novidades.isEmpty());
    }

    @Test
    public void deveRetornarNovidadesDeNovoScore() throws JsonParseException, JsonMappingException, IOException {
    	
    	Score novoScore = jpaAPI.withTransaction( (em) -> {
    		Cidade cidade = dao.find(2513406L);
    		Score score = new Score("teste", 0f, 0f, 0f, 0f);
    		cidade.atualizaScore(score);
    		em.persist(score);
    		em.persist(cidade);
    		em.flush();
    		em.refresh(score);
    		em.refresh(cidade);
    		return score;
    	});
    	
        Result result = Helpers.route(controllers.routes.CidadeController.getNovidades(2513406L, 0, 10));
        assertEquals(OK, result.status());

        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Novidade>>() {});
        
        assertFalse(novidades.isEmpty());
        Novidade novidade = novidades.get(0);
		assertEquals(novoScore, novidade.getScore());
        assertEquals(TipoDaNovidade.NOVO_SCORE, novidade.getTipo());
        
    	jpaAPI.withTransaction( () -> {
    		jpaAPI.em().remove(jpaAPI.em().merge(novidade));
    		jpaAPI.em().remove(jpaAPI.em().merge(novoScore));
    	});
    }

    @Test
    public void deveRetornarNovidadesDeNovoScoreEScoreAtualizado() throws JsonParseException, JsonMappingException, IOException {
    	
    	Score novoScore = jpaAPI.withTransaction( (em) -> {
    		Cidade cidade = dao.find(2513406L);
    		Score score = new Score("teste", 0f, 0f, 0f, 0f);
    		cidade.atualizaScore(score);
    		em.persist(score);
    		em.persist(cidade);
    		em.flush();
    		em.refresh(score);
    		em.refresh(cidade);
    		return score;
    	});
    	
    	float novoValor = 1f;
    	
    	jpaAPI.withTransaction( () -> {
    		Cidade cidade = dao.find(2513406L);
			Score score = new Score("teste", novoValor, 0f, 0f, 0f);
    		cidade.atualizaScore(score);
    		EntityManager em = jpaAPI.em();
			em .persist(cidade);
    		em.flush();
    		em.refresh(cidade);
    	});
    	
        Result result = Helpers.route(controllers.routes.CidadeController.getNovidades(2513406L, 0, 10));
        assertEquals(OK, result.status());

        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Novidade>>() {});
        
        assertFalse(novidades.isEmpty());
        Novidade novidadeDeNovoScore = novidades.get(1);
		assertEquals(novoScore, novidadeDeNovoScore.getScore());
        assertEquals(TipoDaNovidade.NOVO_SCORE, novidadeDeNovoScore.getTipo());
        
        Novidade novidadeDeScoreAtualizado = novidades.get(0);
		assertEquals(novoScore, novidadeDeScoreAtualizado.getScore());
		assertEquals(novoValor, novidadeDeScoreAtualizado.getScore().getValorScore(), 0.0000001);
        assertEquals(TipoDaNovidade.ATUALIZACAO_DE_SCORE, novidadeDeScoreAtualizado.getTipo());
        
    	jpaAPI.withTransaction( () -> {
    		jpaAPI.em().remove(jpaAPI.em().merge(novidadeDeScoreAtualizado));
    		jpaAPI.em().remove(jpaAPI.em().merge(novidadeDeNovoScore));
    		jpaAPI.em().remove(jpaAPI.em().merge(novoScore));
    	});
    }

    @Test
    public void deveRetornarSomenteNovidadesDeNovoScoreParaMaiorQueDois() throws JsonParseException, JsonMappingException, IOException {
    	
    	Score novoScore = jpaAPI.withTransaction( (em) -> {
    		Cidade cidade = dao.find(2513406L);
    		Score score = new Score("teste", 2f, 0f, 0f, 0f);
    		cidade.atualizaScore(score);
    		em.persist(score);
    		em.persist(cidade);
    		em.flush();
    		em.refresh(score);
    		em.refresh(cidade);
    		return score;
    	});
    	
    	float novoValor = 3f;
    	
    	jpaAPI.withTransaction( () -> {
    		Cidade cidade = dao.find(2513406L);
			Score score = new Score("teste", novoValor, 0f, 0f, 0f);
    		cidade.atualizaScore(score);
    		EntityManager em = jpaAPI.em();
			em .persist(cidade);
    		em.flush();
    		em.refresh(cidade);
    	});
    	
        Result result = Helpers.route(controllers.routes.CidadeController.getNovidades(2513406L, 0, 10));
        assertEquals(OK, result.status());

        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Novidade>>() {});
        
        assertFalse(novidades.isEmpty());
        Novidade novidadeDeNovoScore = novidades.get(0);
		assertEquals(novoScore, novidadeDeNovoScore.getScore());
        assertEquals(TipoDaNovidade.NOVO_SCORE, novidadeDeNovoScore.getTipo());
        
    	jpaAPI.withTransaction( () -> {
    		jpaAPI.em().remove(jpaAPI.em().merge(novidadeDeNovoScore));
    		jpaAPI.em().remove(jpaAPI.em().merge(novoScore));
    	});
    }
}
