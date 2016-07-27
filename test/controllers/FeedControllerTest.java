package controllers;

import static controllers.util.ControllersTestUtils.enviaPOSTAddOpiniao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Iniciativa;
import models.IniciativaDAO;
import models.Novidade;
import models.Opiniao;
import models.TipoDaNovidade;

import org.junit.After;
import org.junit.Test;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Http.Status;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

public class FeedControllerTest extends WithAuthentication {

    private Long iniciativaUsada = 805265L;
    private Long cidadeDaIniciativaUsada = 2922706L;
    private String conteudoExemplo = "Das que conheço, essa iniciativa é uma delas!";
    private List<UUID> uuidDeOpinioesPraRemover = new ArrayList<UUID>();

    @After
    public void limpaBancoAposTeste() {
        IniciativaDAO daoIniciativa = app.injector().instanceOf(IniciativaDAO.class);
        JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
        jpaAPI.withTransaction(() -> {
            Iniciativa i = daoIniciativa.find(iniciativaUsada);
            for (UUID uuid : uuidDeOpinioesPraRemover) {
            	Opiniao paraRemover = new Opiniao();
            	paraRemover.setId(uuid);
            	i.removeOpiniao(paraRemover);
            }
            daoIniciativa.save(i);
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
        Result resultado = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaUsada, token);
		uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());

        // a segunda opinião cria uma notificação
		resultado = enviaPOSTAddOpiniao("A novidade veio dar na praia. Na qualidade rara de sereia", iniciativaUsada, token);
		uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId());

        Result result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(result));

        // TODO Esse teste só é possível quando tivermos diferentes usuários:
        // assertEquals(1, novidades.size());
        assertFalse(novidades.isEmpty());
        assertEquals(uuidDeOpinioesPraRemover.get(0), novidades.get(1).getOpiniao().getId());
        assertEquals(uuidDeOpinioesPraRemover.get(1), novidades.get(0).getOpiniao().getId());

        Novidade aNovidade = novidades.get(0);
        assertEquals(TipoDaNovidade.NOVA_OPINIAO, aNovidade.getTipo());
        assertEquals(iniciativaUsada, aNovidade.getIniciativa().getId());
        assertEquals(cidadeDaIniciativaUsada, aNovidade.getCidade().getId());
        
		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(iniciativaUsada).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());

    }

}
