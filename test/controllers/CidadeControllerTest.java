package controllers;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Cidade;
import models.Iniciativa;
import models.Novidade;
import module.MainModule;

import org.junit.Test;

import play.Application;
import play.Logger;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
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
    public void testGetCidadeInexistente() {
        Result result = Helpers.route(controllers.routes.CidadeController.get(0L));
        assertEquals(NOT_FOUND, result.status());
    }

    @Test
    public void testGetCidadeExistente() {
        Result result = Helpers.route(controllers.routes.CidadeController.get(2513406L));
        assertEquals(OK, result.status());

        JsonNode node = Json.parse(Helpers.contentAsString(result));
        assertFalse("deveria retornar cidade no JSON de resposta", node.isNull());

        Cidade cidade = Json.fromJson(node, Cidade.class);

        assertEquals(2513406L, cidade.getId().longValue());
        assertEquals("Santa Luzia", cidade.getNome());
    }

    @Test
    public void deveRetornarIniciativaComInfoDeSeguidor() throws IOException {
        long idCidade = 2807402L;
        long idIniciativa = 797935L;
        // caso hajs efeitos colaterais
        Helpers.route(controllers.routes.IniciativaController.removeInscrito(idIniciativa));

        Result result1 = Helpers.route(controllers.routes.IniciativaController.adicionaInscrito(idIniciativa));
        assertEquals(contentAsString(result1), Http.Status.OK, result1.status());

        Result result2 = Helpers.route(controllers.routes.CidadeController.getIniciativas(idCidade));
        assertEquals(OK, result2.status());

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
