package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import module.MainModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;
import java.util.Iterator;

import static controllers.util.ControllersTestUtils.*;
import static org.junit.Assert.*;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.route;

/**
 * Testa inserção e recuperação de opiniões de usuários nas iniciativas.
 */
public class OpiniaoControllerTest extends WithApplication {

    private OpiniaoController controller;

    private JPAApi jpaAPI;

    private Long iniciativaExemplo = 805264L;
    private String conteudoExemplo = "Essa iniciativa é absolutamente estrogonófica para a cidade.";

    @Before
    public void setUp() {
        this.controller = app.injector().instanceOf(OpiniaoController.class);
        this.jpaAPI = app.injector().instanceOf(JPAApi.class);
    }

    // Efeito colateral: não restarão opiniões em iniciativaExemplo no BD após esses testes.
    @After
    public void tearDown() {
        jpaAPI.withTransaction(() -> {
            controller.removeOpinioes(iniciativaExemplo);
        });
    }

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
            Result result = controller.getOpinioes(inexistente, 0, 100);
            assertEquals(NOT_FOUND, result.status());
            assertEquals("Iniciativa não encontrada", Helpers.contentAsString(result));
        });
    }

    @Test
    public void devePostarOpiniao() throws IOException {
        // se houver algum efeito colateral de outro teste, falhe
        failSeHaOpinioes(iniciativaExemplo);

        // criar opinião
        Result result2 = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaExemplo);
        assertEquals(OK, result2.status());

        // agora deve haver uma
        Result result = Helpers.route(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 100));
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
        Iterator<JsonNode> elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        JsonNode node = elementosIt.next();
        assertFalse(elementosIt.hasNext()); // ha apenas um

        String opiniaoId = node.get("id").asText();
        assertNotNull(opiniaoId);
    }

    @Test
    public void devePostarConteudoDaOpiniao() throws IOException {
        Result result = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaExemplo);

        assertEquals(OK, result.status());
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));

        String postado = respostaJson.get("conteudo").asText();
        assertEquals(conteudoExemplo, postado);

        String opiniaoId = respostaJson.get("id").asText();
        assertNotNull(opiniaoId);
    }

    @Test
    public void devePaginarOpinioes() throws IOException {
        // 3 opiniões
        String opiniao1 = "Excelente!",
                opiniao2 = "Top!!!",
                opiniao3 = "Não concordo com tudo";
        enviaPOSTAddOpiniao(opiniao1, iniciativaExemplo);
        enviaPOSTAddOpiniao(opiniao2, iniciativaExemplo);
        enviaPOSTAddOpiniao(opiniao3, iniciativaExemplo);

        Result result = Helpers.route(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 2));
        // Deve haver 2 opiniões
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
        Iterator<JsonNode> elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        elementosIt.next();
        assertTrue(elementosIt.hasNext()); // há 2 elementos
        elementosIt.next();
        assertFalse(elementosIt.hasNext()); // só há 2 elementos

        // pag 2
        result = Helpers.route(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 1, 2));
        // Deve haver 1 opiniões
        respostaJson = Json.parse(Helpers.contentAsString(result));
        elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        elementosIt.next();
        assertFalse(elementosIt.hasNext()); // só há 2 elementos


        // pag 3
        result = Helpers.route(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 3, 2));
        // Deve haver 0 opiniões
        respostaJson = Json.parse(Helpers.contentAsString(result));
        elementosIt = respostaJson.elements();
        assertFalse(elementosIt.hasNext()); // há 0 elementos

        jpaAPI.withTransaction(() -> {
            controller.removeOpinioes(iniciativaExemplo);
        });
    }


    @Test
    public void deveImpedirPostsMuitoGrandes() throws IOException {
        String conteudo = "";
        for (int i = 0; i < 200; i++) {
            conteudo += "12345";
        }
        // 1001 caracteres.
        conteudo += "x";

        Result result = enviaPOSTAddOpiniao(conteudo, iniciativaExemplo);
        assertEquals(BAD_REQUEST, result.status());
        // é preciso ter limites
        assertEquals("{\"conteudo\":[\"Opiniões devem ter 1000 caracteres ou menos\"]}", Helpers.contentAsString(result));
    }

    @Test
    public void deveImpedirPostsVazios() throws IOException {
        String conteudo = "";

        Result result = enviaPOSTAddOpiniao(conteudo, iniciativaExemplo);
        assertEquals(BAD_REQUEST, result.status());
        // é preciso ter limites
        assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));
    }

    @Test
    public void deveExigirCampos() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\"}");
        Result result = enviaPOSTAddOpiniao(json, iniciativaExemplo);
        assertEquals(BAD_REQUEST, result.status());
        assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));

        JsonNode json2 = new ObjectMapper().readTree("{\"conteudo\": \"Topíssimo\"}");
        Result result2 = enviaPOSTAddOpiniao(json2, iniciativaExemplo);
        assertEquals(BAD_REQUEST, result2.status());
        assertEquals("{\"tipo\":[\"Campo necessário\"]}", Helpers.contentAsString(result2));
    }

    @Test
    public void deveRetornarPrimeiroMaisRecentes() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        JsonNode json2 = new ObjectMapper().readTree("{\"tipo\": \"coracao\", \"conteudo\": \"Eu quero que você se top top top\"}");
        enviaPOSTAddOpiniao(json, iniciativaExemplo);
        enviaPOSTAddOpiniao(json2, iniciativaExemplo);

        Result result = Helpers.route(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 2));
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
        Iterator<JsonNode> elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        assertEquals("coracao", elementosIt.next().get("tipo").asText());
        assertTrue(elementosIt.hasNext()); // há 2 elementos
        assertEquals("bomba", elementosIt.next().get("tipo").asText());
    }

    /**
     * Falha se houver alguma opinião para a iniciativa.
     */
    private void failSeHaOpinioes(Long idIniciativa) {
        jpaAPI.withTransaction(() -> {
            Result result = controller.getOpinioes(idIniciativa, 0, 100);
            assertEquals(OK, result.status());
            assertTrue(temZeroElementosJson(result));
        });
    }

    private boolean temZeroElementosJson(Result result) {
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        return !node.elements().hasNext();
    }
}
