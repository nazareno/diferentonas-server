package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.inject.Inject;
import models.Mensagem;
import models.Novidade;
import module.MainModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import play.Application;
import play.Logger;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static controllers.util.ControllersTestUtils.*;
import static play.test.Helpers.contentAsString;

public class FeedControllerTest extends WithApplication {

    private Long iniciativaUsada = 805264L;
    private String conteudoExemplo = "Das que conheço, essa iniciativa é uma delas!";

    @Before
    public void limpaBancoAposTeste() {
        OpiniaoController controller = app.injector().instanceOf(OpiniaoController.class);
        JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
        jpaAPI.withTransaction(() -> {
            controller.removeOpinioes(iniciativaUsada);
        });
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
    }

    @Test
    public void deveIniciarComFeedVazio() throws IOException {
        Result result = Helpers.route(controllers.routes.FeedController.getNovidades(0, 10));
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
        enviaPOSTAddOpiniao(conteudoExemplo, iniciativaUsada);
        // a segunda opinião cria uma notificação
        enviaPOSTAddOpiniao("A novidade veio dar na praia. Na qualidade rara de sereia", iniciativaUsada);

        Result result = Helpers.route(controllers.routes.FeedController.getNovidades(0, 10));
        List<Novidade> novidades = jsonToList(contentAsString(result));

        // TODO Esse teste só é possível quando tivermos diferentes usuários:
        // assertEquals(1, novidades.size());
        assertEquals(2, novidades.size()); // Temporário

        Novidade aNovidade = novidades.get(0);
        assertEquals(Novidade.TIPO_OPINIAO, aNovidade.getTipo());
        assertEquals(iniciativaUsada, aNovidade.getOpiniao().getIniciativa().getId());
        // assertEquals(iniciativaUsada, aNovidade.getIniciativa());
    }

}
