package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.List;

import module.MainModule;

import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
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

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
    }

    @Test
    public void deveListarDuasAtualizacoes() throws JsonParseException, JsonMappingException, IOException {
        Result result = Helpers.route(controllers.routes.AtualizacaoController.getAtualizacoes());
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<String> novidades = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<String>>() {});
        assertFalse("devia ter duas atualizações", novidades.isEmpty());
        assertEquals("20160627", novidades.get(0));
        assertEquals("20160513", novidades.get(1));
    }
}
