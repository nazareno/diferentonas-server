package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.List;

import models.Atualizacao;
import models.Novidade;

import org.junit.Ignore;
import org.junit.Test;

import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testes do controller.
 */
public class AtualizacaoControllerTest extends WithApplication {

    @Test
    public void deveListarDuasAtualizacoes() throws JsonParseException, JsonMappingException, IOException {
        Result result = Helpers.route(controllers.routes.AtualizacaoController.getAtualizacoes());
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        Atualizacao atualizacao = Json.fromJson(Json.parse(conteudoResposta), Atualizacao.class);
        assertEquals(Atualizacao.Status.DESATUALIZADO, atualizacao.getStatus());
        assertEquals("", atualizacao.getUltima());
        assertEquals("20160706", atualizacao.getProxima());
    }

    @Test
    @Ignore
    public void deveAtualizarScores() throws JsonParseException, JsonMappingException, IOException {
        Result result = Helpers.route(controllers.routes.AtualizacaoController.aplica());
        assertEquals(OK, result.status());
        
        result = Helpers.route(controllers.routes.CidadeController.getNovidades(2801108L, 0, 0));
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Novidade>>() {});
        assertFalse("devia ter novidades", novidades.isEmpty());
        
    }
}
