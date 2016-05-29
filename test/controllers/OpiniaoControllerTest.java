package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Opiniao;
import module.MainModule;
import org.junit.After;
import org.junit.Before;
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

import java.io.IOException;
import java.util.Iterator;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.route;

/**
 * Testa inserção e recuperação de opiniões de usuários nas iniciativas.
 */
public class OpiniaoControllerTest extends WithApplication {

    // TODO pagination

    private OpiniaoController controller;

    private JPAApi jpaAPI;

    private Long iniciativaExemplo = 805264L;
    private String conteudoExemplo = "Essa iniciativa é absolutamente estrogonófica para a cidade.";

    @Before
    public void setUp() {
        this.controller = app.injector().instanceOf(OpiniaoController.class);
        this.jpaAPI = app.injector().instanceOf(JPAApi.class);
    }

    // TODO Essa forma seria mais limpa? Não funciona (ConcurrentModificationException)
//    @After
//    public void tearDown() {
//        jpaAPI.withTransaction(() -> {
//            Result result = controller.removeOpinioes(iniciativaExemplo);
//        });
//    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
    }

    @Test
    public void deveRetornarJsonVazioQuandoNaoHaOpinioes() {
        failSeHaOpinioes(iniciativaExemplo);
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
        // se houver algum efeito colateral de outro teste, falhe
        failSeHaOpinioes(iniciativaExemplo);
        jpaAPI.withTransaction(() -> {
            // criar opinião
            try {
                Result result2 = enviaPOSTAddOpiniao(conteudoExemplo);
                assertEquals(OK, result2.status());
            } catch (IOException e) {
                fail(e.getMessage());
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
            assertNotNull(opiniaoId[0]);
        });

        // impiedosamente apagamos ela para não deixar rastro
        removeOpiniaoDoBD(opiniaoId[0]);
    }

    @Test
    public void devePostarConteudoDaOpiniao() throws IOException {
        final String[] opiniaoId = {null};

        jpaAPI.withTransaction(() -> {
            try {
                Result result = enviaPOSTAddOpiniao(conteudoExemplo);

                assertEquals(OK, result.status());
                JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));

                String postado = respostaJson.get("conteudo").asText();
                assertEquals(conteudoExemplo, postado);

                opiniaoId[0] = respostaJson.get("id").asText();
                assertNotNull(opiniaoId[0]);
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });

        removeOpiniaoDoBD(opiniaoId[0]);
    }

    @Test
    public void deveImpedirPostsMuitoGrandes() throws IOException {
        jpaAPI.withTransaction(() -> {
            try {
                String conteudo = "";
                for (int i = 0; i < 200; i++) {
                    conteudo += "12345";
                }
                // 1001 caracteres.
                conteudo += "x";

                Result result = enviaPOSTAddOpiniao(conteudo);
                assertEquals(BAD_REQUEST, result.status());
                // é preciso ter limites
                assertEquals("{\"conteudo\":[\"Opiniões devem ter 1000 caracteres ou menos\"]}", Helpers.contentAsString(result));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    public void deveImpedirPostsVazios() throws IOException {
        jpaAPI.withTransaction(() -> {
            try {
                String conteudo = "";

                Result result = enviaPOSTAddOpiniao(conteudo);
                assertEquals(BAD_REQUEST, result.status());
                // é preciso ter limites
                assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    public void deveExigirCampos() throws IOException {
        jpaAPI.withTransaction(() -> {
            try {
                JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\"}");
                Result result = enviaPOSTAddOpiniao(json);
                assertEquals(BAD_REQUEST, result.status());
                assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });

        jpaAPI.withTransaction(() -> {
            try {
                JsonNode json = new ObjectMapper().readTree("{\"conteudo\": \"Topíssimo\"}");
                Result result = enviaPOSTAddOpiniao(json);
                assertEquals(BAD_REQUEST, result.status());
                assertEquals("{\"tipo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        });
    }

    /**
     * Falha se houver alguma opinião para a iniciativa.
     */
    private void failSeHaOpinioes(Long idIniciativa) {
        jpaAPI.withTransaction(() -> {
            Result result = controller.getOpinioes(idIniciativa);
            assertEquals(OK, result.status());
            assertTrue(temZeroElementosJson(result));
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

    private JsonNode criaJsonDaRequisicao(String conteudo) throws IOException {
        return (new ObjectMapper()).readTree("{ \"conteudo\": \"" + conteudo + "\", " +
                "\"tipo\": \"coracao\"}");
    }

    private Result enviaPOSTAddOpiniao(String conteudo) throws IOException {
        JsonNode json = criaJsonDaRequisicao(conteudo);
        return enviaPOSTAddOpiniao(json);
    }

    private Result enviaPOSTAddOpiniao(JsonNode json) {
        Logger.debug("Requisição para add opinião: " + json.toString());
        Http.RequestBuilder request = new Http.RequestBuilder().method("POST")
                .bodyJson(json)
                .uri(controllers.routes.OpiniaoController.addOpiniao(iniciativaExemplo).url());
        return route(request);
    }

}
