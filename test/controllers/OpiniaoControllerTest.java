package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.*;
import module.MainModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Application;
import play.Logger;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.invokeWithContext;
import static play.test.Helpers.route;

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
    private String conteudoExemplo = "Minha opinião sobre essa iniciativa é que ela é " +
            "absolutamente estrogonófica para a cidade.";

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
        jpaAPI.withTransaction(() -> {
            Result result = controller.getOpinioes(iniciativaExemplo);
            assertEquals(OK, result.status());
            assertTrue(temZeroElementosJson(result));
        });
    }

    @Test
    public void deveRetornar404EmCidadeInexistente() {
        jpaAPI.withTransaction(() -> {
            long inexistente = -1L;
            Result result = controller.getOpinioes(inexistente);
            assertEquals(NOT_FOUND, result.status());
            assertEquals("Iniciativa não encontrada", Helpers.contentAsString(result));
        });
    }

    @Test
    public void devePostarOpiniao() throws IOException {
        jpaAPI.withTransaction(() -> {
            // confirmar que não há opinião
            Result result = controller.getOpinioes(iniciativaExemplo);
            assertTrue(temZeroElementosJson(result));

            // criar opinião
            try {
                Result result2 = requisicaoAddOpiniao(conteudoExemplo);
                assertEquals(OK, result2.status());
            } catch (IOException e) {
                fail();
            }
        });

        final String[] opiniaoId = {null};

        // agora deve haver uma
        jpaAPI.withTransaction(() -> {
            Result result = controller.getOpinioes(iniciativaExemplo);
            JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
            Iterator<JsonNode> elementosIt = respostaJson.elements();
            assertTrue(elementosIt.hasNext()); // há elemento
            JsonNode node = elementosIt.next();
            assertFalse(elementosIt.hasNext()); // ha apenas um

            opiniaoId[0] = node.get("id").asText();
        });
        // impiedosamente apagamos ela sem deixar rastro
        removeOpiniaoDoBD(opiniaoId[0]);
    }

    @Test
    public void devePostarConteudoDaOpiniao() throws IOException {
        final String[] opiniaoId = {null};

        jpaAPI.withTransaction(() -> {
            try {
                Result result = requisicaoAddOpiniao(conteudoExemplo);

                assertEquals(OK, result.status());
                JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
                Logger.debug("Resposta: " + Helpers.contentAsString(result));

                String postado = respostaJson.get("conteudo").asText();
                assertEquals(conteudoExemplo, postado);

                opiniaoId[0] = respostaJson.get("id").asText();
            } catch (IOException e) {
                fail();
            }
        });

        removeOpiniaoDoBD(opiniaoId[0]);
    }

    private JsonNode criaRequisicao(String conteudo) throws IOException {
        return (new ObjectMapper()).readTree("{ \"conteudo\": \"" + conteudo + "\" }");
    }

    @Test
    @Ignore
    public void deveImpedirPostsMuitoGrandes() throws IOException {
        jpaAPI.withTransaction(() -> {
            try {
                String conteudo = "";
                for (int i = 0; i < 200; i++) {
                    conteudo += "12345";
                }
                conteudo += "x";
                Result result = null;
                result = requisicaoAddOpiniao(conteudo);
                assertEquals(BAD_REQUEST, result.status());
                assertEquals("Conteúdo não pode ultrapassar 1000 caracteres", Helpers.contentAsString(result));
            } catch (IOException e) {
                fail();
            }
        });
    }

    private boolean temZeroElementosJson(Result result) {
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        return !node.elements().hasNext();
    }

    private void removeOpiniaoDoBD(String idOpiniao) {
        jpaAPI.withTransaction(() -> {
            Result result = controller.removeOpiniao(idOpiniao);
            assertEquals(OK, result.status());

            Result result2 = controller.getOpinioes(iniciativaExemplo);
            assertTrue(temZeroElementosJson(result2));
        });
    }

    private Result requisicaoAddOpiniao(String conteudo) throws IOException {
        Http.RequestBuilder request = new Http.RequestBuilder().method("POST")
                .bodyJson(criaRequisicao(conteudo))
                .uri(controllers.routes.OpiniaoController.addOpiniao(iniciativaExemplo).url());
        return route(request);
    }

}
