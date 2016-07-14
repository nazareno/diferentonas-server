package controllers;

import static controllers.util.ControllersTestUtils.enviaPOSTAddOpiniao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.List;

import models.Novidade;
import models.TipoDaNovidade;

import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

public class FeedControllerTest extends WithAuthentication {

    private Long iniciativaUsada = 805265L;
    private Long cidadeDaIniciativaUsada = 2922706L;
    private String conteudoExemplo = "Das que conheço, essa iniciativa é uma delas!";

    @Before
    public void limpaBancoAposTeste() {
        OpiniaoController controller = app.injector().instanceOf(OpiniaoController.class);
        JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
        jpaAPI.withTransaction(() -> {
            controller.removeOpinioes(iniciativaUsada);
        });
    }

    @Test
    public void deveIniciarComFeedVazio() throws IOException {
        Result result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = jsonToList(conteudoResposta);
        assertTrue(novidades.isEmpty());
    }

    private static List<Novidade> jsonToList(String jsonResposta) throws IOException {
        return new ObjectMapper().readValue(jsonResposta, new TypeReference<List<Novidade>>() {});
    }

    @Test
    public void deveNotificarOpinioesEmIniciativaOndeComentei() throws IOException {
        // vira interessado
        enviaPOSTAddOpiniao(conteudoExemplo, iniciativaUsada, token);
        // a segunda opinião cria uma notificação
        enviaPOSTAddOpiniao("A novidade veio dar na praia. Na qualidade rara de sereia", iniciativaUsada, token);

        Result result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(result));

        // TODO Esse teste só é possível quando tivermos diferentes usuários:
        // assertEquals(1, novidades.size());
        assertEquals(2, novidades.size()); // Temporário

        Novidade aNovidade = novidades.get(0);
        assertEquals(TipoDaNovidade.NOVA_OPINIAO, aNovidade.getTipo());
        assertEquals(iniciativaUsada, aNovidade.getIniciativa().getId());
        assertEquals(cidadeDaIniciativaUsada, aNovidade.getCidade().getId());
    }

}
