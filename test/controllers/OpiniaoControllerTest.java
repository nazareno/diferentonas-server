package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import module.MainModule;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;

/**
 * Testa inserção e recuperação de opiniões de usuários nas iniciativas.
 */
public class OpiniaoControllerTest extends WithApplication {

    // TODO pagination
    // TODO max size
    // TODO conteudo
    // TODO tipos de opinião

    private OpiniaoController controller;

    private JPAApi jpaAPI;

    private Long iniciativaExemplo = 805264L;

    @Before
    public void setUp() {
        this.controller = app.injector().instanceOf(OpiniaoController.class);
        this.jpaAPI = app.injector().instanceOf(JPAApi.class);
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
    }

    @Test
    public void deveRetornarJsonVazioQuandoNaoHaOpinioes() {
        jpaAPI.withTransaction(() ->{
            Result result = controller.getOpinioes(iniciativaExemplo);
            assertEquals(OK, result.status());
            assertTrue(temZeroElementosJson(result));
        });
    }

    @Test
    public void deveRetornar404EmCidadeInexistente() {
        jpaAPI.withTransaction(() ->{
            long inexistente = -1L;
            Result result = controller.getOpinioes(inexistente);
            assertEquals(NOT_FOUND, result.status());
            assertEquals("Iniciativa não encontrada", Helpers.contentAsString(result));
        });
    }

    @Test
    public void devePostarOpiniao() {
        jpaAPI.withTransaction(() ->{
            // confirmar que não há opinião
            Result result = controller.getOpinioes(iniciativaExemplo);
            assertTrue(temZeroElementosJson(result));

            // criar opinião
            Result result2 = controller.addOpiniao(iniciativaExemplo);
            assertEquals(OK, result.status());
        });

        final String[] opiniaoId = {null};

        // agora deve haver uma
        jpaAPI.withTransaction(() ->{
            Result result = controller.getOpinioes(iniciativaExemplo);
            JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
            Iterator<JsonNode> elementosIt = respostaJson.elements();
            assertTrue(elementosIt.hasNext()); // há elemento
            JsonNode node = elementosIt.next();
            assertFalse(elementosIt.hasNext()); // ha apenas um

            opiniaoId[0] = node.get("id").asText();
        });

        // impiedosamente apagamos ela sem deixar rastro
        jpaAPI.withTransaction(() ->{
            Result result = controller.removeOpiniao(opiniaoId[0]);
            assertEquals(OK, result.status());

            Result result2 = controller.getOpinioes(iniciativaExemplo);
            assertTrue(temZeroElementosJson(result2));
        });
    }

    private boolean temZeroElementosJson(Result result) {
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        return ! node.elements().hasNext();
    }
}
