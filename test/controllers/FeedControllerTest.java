package controllers;

import static controllers.util.ControllersTestUtils.enviaPOSTAddOpiniao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import models.Cidadao;
import models.CidadaoDAO;
import models.Cidade;
import models.CidadeDAO;
import models.Iniciativa;
import models.IniciativaDAO;
import models.Novidade;
import models.Opiniao;
import models.OpiniaoDAO;
import models.TipoDaNovidade;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.Configuration;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Http.Status;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

public class FeedControllerTest extends WithAuthentication {

	private static final Long INICIATIVA_TESTE = -1L;
	private static final Long CIDADE_TESTE = -1L;

	private Long iniciativaUsada = INICIATIVA_TESTE;
    private Long cidadeDaIniciativaUsada = CIDADE_TESTE;
    private String conteudoExemplo = "Das que conheço, essa iniciativa é uma delas!";
    private List<UUID> uuidDeOpinioesPraRemover = new ArrayList<UUID>();

	@Before
	public void limpaBancoParaTestes() {
		JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
		Cidade cidadeTeste = new Cidade(CIDADE_TESTE, "Pasárgada", "PB", 1f, 1f, 1f, 1f, 1000L, 1f, 1f, 0f);
		Iniciativa iniciativaTeste = new Iniciativa(INICIATIVA_TESTE, 2015, "iniciativa para melhorar condições da cidade", "programa do governo federal", "area de atuação", "fonte", "concedente", "status", false, 1000f, 1000f, new Date(), new Date(), new Date());
		cidadeTeste.addIniciativa(iniciativaTeste, new Date(), false);
		
		CidadeDAO cidadeDAO = app.injector().instanceOf(CidadeDAO.class);
		jpaAPI.withTransaction(() ->{
			if(cidadeDAO.find(-1L) == null){
				cidadeDAO.save(cidadeTeste);
			}
		});
	}

    @After
    public void limpaBancoAposTeste() {
        IniciativaDAO daoIniciativa = app.injector().instanceOf(IniciativaDAO.class);
        OpiniaoDAO daoOpiniao = app.injector().instanceOf(OpiniaoDAO.class);
        JPAApi jpaAPI = app.injector().instanceOf(JPAApi.class);
        jpaAPI.withTransaction(() -> {
            Iniciativa i = daoIniciativa.find(iniciativaUsada);
            for (UUID uuid : uuidDeOpinioesPraRemover) {
            	Opiniao paraRemover = new Opiniao();
            	paraRemover.setId(uuid);
            	i.removeOpiniao(paraRemover);
            	daoOpiniao.delete(daoOpiniao.find(uuid));
            }
            daoIniciativa.save(i);
        });
        
        desautenticaAdmin();
        
		CidadeDAO cidadeDAO = app.injector().instanceOf(CidadeDAO.class);
		IniciativaDAO iniciativaDAO = app.injector().instanceOf(IniciativaDAO.class);
		jpaAPI.withTransaction(() ->{
			if(cidadeDAO.find(-1L) != null){
				cidadeDAO.remove(cidadeDAO.find(-1L));
			}
			if(iniciativaDAO.find(-1L) != null){
				iniciativaDAO.remove(iniciativaDAO.find(-1L));
			}
		});

    }

    @Test
    public void deveIniciarComFeedVazio() throws IOException {
        Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(iniciativaUsada).url()).method("DELETE"));

        Result result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        assertEquals("getNovidades não completou com sucesso", OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull("conteúdo da resposta não deveria ser null", conteudoResposta);
        assertTrue("resposta não é um array de novidades", Json.parse(conteudoResposta).isArray());
        List<Novidade> novidades = jsonToList(conteudoResposta);
        assertTrue("array de novidades não está vazio", novidades.isEmpty());
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

        Result result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 100).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(result));
        
        System.out.println(novidades.size());

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

    @Test
    public void deveNotificarCidadeQueSegui() throws IOException {
    	
		Result result = Helpers.route(builder.uri(controllers.routes.CidadeController.adicionaInscrito(cidadeDaIniciativaUsada).url()).method("POST"));
		assertEquals(Status.OK, result.status());

        result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 100).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(result));
        
        for (Novidade novidade : novidades) {
            assertNotEquals(TipoDaNovidade.NOVA_OPINIAO, novidade.getTipo());
            assertEquals(cidadeDaIniciativaUsada, novidade.getCidade().getId());
		}
        
		result = Helpers.route(builder.uri(controllers.routes.CidadeController.removeInscrito(cidadeDaIniciativaUsada).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());
    }

    @Test
    public void deveNotificarCidadeQueSeguiSemNotificarOpinioes() throws IOException {
    	
    	// Inscreve em cidade
		Result result = Helpers.route(builder.uri(controllers.routes.CidadeController.adicionaInscrito(cidadeDaIniciativaUsada).url()).method("POST"));
		assertEquals(Status.OK, result.status());

		// Adiciona Opinião
        result = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaUsada, token);
		uuidDeOpinioesPraRemover.add(Json.fromJson(Json.parse(Helpers.contentAsString(result)), Opiniao.class).getId());

		// Desinscreve da Iniciativa
		result = Helpers.route(builder.uri(controllers.routes.IniciativaController.removeInscrito(iniciativaUsada).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());
		
		result = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 100).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(result));
        
        for (Novidade novidade : novidades) {
            assertNotEquals(TipoDaNovidade.NOVA_OPINIAO, novidade.getTipo());
            assertEquals(cidadeDaIniciativaUsada, novidade.getCidade().getId());
		}
        
		result = Helpers.route(builder.uri(controllers.routes.CidadeController.removeInscrito(cidadeDaIniciativaUsada).url()).method("DELETE"));
		assertEquals(Status.OK, result.status());
    }


    @Test
    public void deveNotificarSeApoioAOpiniaoNaNovidade() throws IOException {
        // vira interessado
        Result resultado = enviaPOSTAddOpiniao(conteudoExemplo, iniciativaUsada, token);
		UUID opiniaoUUID = Json.fromJson(Json.parse(Helpers.contentAsString(resultado)), Opiniao.class).getId();
		uuidDeOpinioesPraRemover.add(opiniaoUUID);

        resultado = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        List<Novidade> novidades = jsonToList(contentAsString(resultado));
        assertFalse(novidades.isEmpty());
        assertEquals(opiniaoUUID, novidades.get(0).getOpiniao().getId());
        assertFalse(novidades.get(0).getOpiniao().isApoiada());
        assertEquals(0, novidades.get(0).getOpiniao().getNumeroDeApoiadores());
        
        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.addJoinha(iniciativaUsada, opiniaoUUID.toString()).url()).method("POST"));
        assertEquals(Status.OK, resultado.status());

        resultado = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        novidades = jsonToList(contentAsString(resultado));
        assertFalse(novidades.isEmpty());
        assertEquals(opiniaoUUID, novidades.get(0).getOpiniao().getId());
        assertTrue(novidades.get(0).getOpiniao().isApoiada());
        assertEquals(1, novidades.get(0).getOpiniao().getNumeroDeApoiadores());

        resultado = Helpers.route(builder.uri(controllers.routes.OpiniaoController.removeJoinha(iniciativaUsada, opiniaoUUID.toString()).url()).method("DELETE"));
        assertEquals(Status.OK, resultado.status());

        resultado = Helpers.route(builder.uri(controllers.routes.FeedController.getNovidades(0, 10).url()).method("GET"));
        novidades = jsonToList(contentAsString(resultado));
        assertFalse(novidades.isEmpty());
        assertEquals(opiniaoUUID, novidades.get(0).getOpiniao().getId());
        assertFalse(novidades.get(0).getOpiniao().isApoiada());
        assertEquals(0, novidades.get(0).getOpiniao().getNumeroDeApoiadores());
    }

}
