package controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Mensagem;
import models.Novidade;
import module.MainModule;
import org.junit.After;
import org.junit.Test;
import play.Application;
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

    @After
    public void limpaBancoAposTeste() {

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
        assertEquals(1, novidades.size());

        Novidade aNovidade = novidades.get(0);
        assertEquals(Novidade.TIPO_OPINIAO, aNovidade.getTipo());
        assertEquals(iniciativaUsada, aNovidade.getIniciativa().getId());
        // assertEquals(iniciativaUsada, aNovidade.getIniciativa());
    }

}
