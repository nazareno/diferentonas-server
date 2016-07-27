package controllers;

import static controllers.util.ControllersTestUtils.enviaPOSTAddOpiniao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.CONFLICT;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import models.Iniciativa;
import models.IniciativaDAO;
import models.Novidade;
import models.Opiniao;
import models.OpiniaoDAO;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

/**
 * Testa inserção e recuperação de opiniões de usuários nas iniciativas.
 */
public class OpiniaoControllerTest extends WithAuthentication {

    private OpiniaoController controller;

    private JPAApi jpaAPI;

    private Long iniciativaExemplo = 818977L;
    private String conteudoExemplo = "Essa iniciativa é absolutamente estrogonófica para a cidade.";
    private List<UUID> uuidDeOpinioesPraRemover = new ArrayList<UUID>();
	private OpiniaoDAO daoOpiniao;

    @Before
    public void setUp() {
        this.controller = app.injector().instanceOf(OpiniaoController.class);
        this.jpaAPI = app.injector().instanceOf(JPAApi.class);
        this.daoOpiniao = app.injector().instanceOf(OpiniaoDAO.class);
    }

    // Efeito colateral: não restarão opiniões em iniciativaExemplo no BD após esses testes.
    @After
    public void tearDown() {
        IniciativaDAO daoIniciativa = app.injector().instanceOf(IniciativaDAO.class);
        JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
        jpaAPI.withTransaction(() -> {
            Iniciativa i = daoIniciativa.find(iniciativaExemplo);
            for (UUID uuid : uuidDeOpinioesPraRemover) {
            	Opiniao paraRemover = new Opiniao();
            	paraRemover.setId(uuid);
            	i.removeOpiniao(paraRemover);
            }
            daoIniciativa.save(i);
        });
        uuidDeOpinioesPraRemover.clear();
    }

    @Test
    @Ignore
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
//        failSeHaOpinioes(iniciativaExemplo);

        // criar opinião
        Result result2 = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(result2)), Opiniao.class).getId());
        assertEquals(OK, result2.status());

        // agora deve haver uma
        Result result = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 100).url()).method("GET"));
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
        Iterator<JsonNode> elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        JsonNode node = elementosIt.next();
//        assertFalse(elementosIt.hasNext()); // ha apenas um

        String opiniaoId = node.get("id").asText();
        assertNotNull(opiniaoId);
    }

    @Test
    public void devePostarConteudoDaOpiniao() throws IOException {
        Result result = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(result)), Opiniao.class).getId());

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
        Result resultado = enviaPOSTAddOpiniao(opiniao1, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());
        resultado = enviaPOSTAddOpiniao(opiniao2, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());
        resultado = enviaPOSTAddOpiniao(opiniao3, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());

        Result result = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 2).url()).method("GET"));
        // Deve haver 2 opiniões
        JsonNode respostaJson = Json.parse(Helpers.contentAsString(result));
        Iterator<JsonNode> elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        elementosIt.next();
        assertTrue(elementosIt.hasNext()); // há 2 elementos
        elementosIt.next();
        assertFalse(elementosIt.hasNext()); // só há 2 elementos

        // pag 2
        result = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 1, 2).url()).method("GET"));
        // Deve haver 1 opiniões
        respostaJson = Json.parse(Helpers.contentAsString(result));
        elementosIt = respostaJson.elements();
        assertTrue(elementosIt.hasNext()); // há elemento
        elementosIt.next();
//        assertFalse(elementosIt.hasNext()); // só há 2 elementos


//        // pag 3
//        result = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 3, 2).url()).method("GET"));
//        // Deve haver 0 opiniões
//        respostaJson = Json.parse(Helpers.contentAsString(result));
//        elementosIt = respostaJson.elements();
//        assertFalse(elementosIt.hasNext()); // há 0 elementos

//        jpaAPI.withTransaction(() -> {
//            controller.removeOpinioes(iniciativaExemplo);
//        });
    }


    @Test
    public void deveImpedirPostsMuitoGrandes() throws IOException {
        String conteudo = "";
        for (int i = 0; i < 200; i++) {
            conteudo += "12345";
        }
        // 1001 caracteres.
        conteudo += "x";

        Result result = enviaPOSTAddOpiniao(conteudo, iniciativaExemplo, token);
        assertEquals(BAD_REQUEST, result.status());
        // é preciso ter limites
        assertEquals("{\"conteudo\":[\"Opiniões devem ter 1000 caracteres ou menos\"]}", Helpers.contentAsString(result));
    }

    @Test
    public void deveImpedirPostsVazios() throws IOException {
        String conteudo = "";

        Result result = enviaPOSTAddOpiniao(conteudo, iniciativaExemplo, token);
        assertEquals(BAD_REQUEST, result.status());
        // é preciso ter limites
        assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));
    }

    @Test
    public void deveExigirCampos() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\"}");
        Result result = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        assertEquals(BAD_REQUEST, result.status());
        assertEquals("{\"conteudo\":[\"Campo necessário\"]}", Helpers.contentAsString(result));

        JsonNode json2 = new ObjectMapper().readTree("{\"conteudo\": \"Topíssimo\"}");
        Result result2 = enviaPOSTAddOpiniao(json2, iniciativaExemplo, token);
        assertEquals(BAD_REQUEST, result2.status());
        assertEquals("{\"tipo\":[\"Campo necessário\"]}", Helpers.contentAsString(result2));
    }

    @Test
    public void deveRetornarPrimeiroMaisRecentes() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        JsonNode json2 = new ObjectMapper().readTree("{\"tipo\": \"coracao\", \"conteudo\": \"Eu quero que você se top top top\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());
        resultado = enviaPOSTAddOpiniao(json2, iniciativaExemplo, token);
        uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());

        Result result = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 2).url()).method("GET"));
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
    
    @Test
    public void deveAdicionarApoiador() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("POST"));
        
		assertTrue(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));

    }

    @Test
    public void deveDarErroAoAdicionarApoiadorNovamente() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("POST"));
        assertEquals(OK, resultado.status());
        
		assertTrue(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("POST"));
        assertEquals(CONFLICT, resultado.status());

		assertTrue(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
    }

    @Test
    public void deveRemoverApoiador() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("POST"));
        
		assertTrue(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.removeJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("DELETE"));
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
    }

    @Test
    public void deveDarErroAoRemoverNaoApoiador() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));

		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.removeJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("DELETE"));
        assertEquals(BAD_REQUEST, resultado.status());
    }

    @Test
    public void deveDarErroAoRemoverNaoApoiadorDeIniciativaInexistente() throws IOException {
		
        Result resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.removeJoinha(0L, "").url()).method("DELETE"));
        assertEquals(NOT_FOUND, resultado.status());
    }


    @Test
    public void deveRetornarMarcadorDeJoinhaDadoEmOpinioes() throws IOException {
        JsonNode json = new ObjectMapper().readTree("{\"tipo\": \"bomba\", \"conteudo\": \"Topíssimo\"}");
        Result resultado = enviaPOSTAddOpiniao(json, iniciativaExemplo, token);
        UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);
        
		assertFalse(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 100).url()).method("GET"));
        List<Opiniao> opinioes = new ObjectMapper().readValue(contentAsString(resultado), new TypeReference<List<Opiniao>>() {});
        for (Opiniao opiniao : opinioes) {
			assertFalse(opiniao.isApoiada());
		}
        
		
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaExemplo, opiniaoUUID.toString()).url()).method("POST"));
        
		assertTrue(jpaAPI.withTransaction(() -> daoOpiniao.find(opiniaoUUID).isApoiada(admin)));
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.getOpinioes(iniciativaExemplo, 0, 100).url()).method("GET"));
        opinioes = new ObjectMapper().readValue(contentAsString(resultado), new TypeReference<List<Opiniao>>() {});
        for (Opiniao opiniao : opinioes) {
			if(opiniao.getId().equals(opiniaoUUID)){
				assertTrue(opiniao.isApoiada());
			}else{
				assertFalse(opiniao.isApoiada());
			}
		}
    }

}
