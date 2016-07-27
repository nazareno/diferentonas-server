package controllers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import models.Cidadao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

public class CidadaoControllerTest extends WithAuthentication {

    private List<Cidadao> cidadaosCobaia;
	private CidadaoController controller;
	private JPAApi jpaAPI;

    @Before
    public void preparaBancoParaTeste() {
		cidadaosCobaia = new LinkedList<>();
        cidadaosCobaia.add(new Cidadao("root", "root"));
        cidadaosCobaia.add(new Cidadao("raquel", "raquel"));
        cidadaosCobaia.add(new Cidadao("toinho", "toinho"));

        controller = app.injector().instanceOf(CidadaoController.class);
        jpaAPI = app.injector().instanceOf(JPAApi.class);
        limpaBanco();
        jpaAPI.withTransaction(() -> {
            for (Cidadao c:
                    cidadaosCobaia) {
                controller.criaCidadao(c);
            }
        });
    }

    @After	
    public void limpaBancoPosTeste() {
        limpaBanco();
    }

	private void limpaBanco() {

        jpaAPI.withTransaction(() -> {
            for (Cidadao c:
                 cidadaosCobaia) {
                controller.removeCidadaoPorLogin(c.getLogin());
            }
        });

	}

    private static List<Cidadao> jsonToList(String jsonResposta) throws IOException {
        return new ObjectMapper().readValue(jsonResposta, new TypeReference<List<Cidadao>>() {});
    }

    @Test
    public void deveBuscarPorLogin() throws IOException {
    	
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("r", 0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Cidadao> cidadaos = jsonToList(conteudoResposta);
        assertEquals(2, cidadaos.size());
    }

    @Test
    public void deveRetornarListaVaziaAoNaoEncontrar() throws IOException {
    	
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("ieyqwfasdkljhfrsd", 0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        assertTrue(Json.parse(conteudoResposta).isArray());
        List<Cidadao> cidadaos = jsonToList(conteudoResposta);
        assertEquals(0, cidadaos.size());
    }

    @Test
    public void devePromoverAFuncionario() throws IOException {
        // pegar um usuario conhecido no BD.
    	
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        List<Cidadao> cidadaos = jsonToList(contentAsString(result));
        assertEquals(1, cidadaos.size()); // para evitar surpresas

        // agora o teste
        Cidadao cidadao = cidadaos.get(0);
        assertFalse(cidadao.isFuncionario());
        assertNull(cidadao.getMinisterioDeAfiliacao());

        String umMinisterio = "Ministério que ainda vão inventar";
        Result result2 = Helpers.route(builder.uri(controllers.routes.CidadaoController.promoveAFuncionario(cidadao.getId().toString(), umMinisterio).url()).method("POST"));
        assertEquals(OK, result2.status());
        Cidadao cidadao2 = Json.fromJson(Json.parse(contentAsString(result2)), Cidadao.class);
        assertTrue(cidadao2.isFuncionario());
        assertEquals(umMinisterio, cidadao2.getMinisterioDeAfiliacao());

        // e via GET
        // pegar um usuario conhecido no BD.
        Result result3 = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        List<Cidadao> cidadaos3 = jsonToList(contentAsString(result3));

        // agora o teste
        Cidadao cidadao3 = cidadaos3.get(0);
        assertTrue(cidadao3.isFuncionario());
        assertEquals(umMinisterio, cidadao3.getMinisterioDeAfiliacao());
    }


    @Test
    public void devePermitirMudancaDeMinisterio() throws IOException {
        // pegar um usuario conhecido no BD.
    	
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        List<Cidadao> cidadaos = jsonToList(contentAsString(result));
        assertEquals(1, cidadaos.size()); // para evitar surpresas

        // agora o teste
        Cidadao cidadao = cidadaos.get(0);
        assertFalse(cidadao.isFuncionario());
        assertNull(cidadao.getMinisterioDeAfiliacao());

        String umMinisterio = "Ministério que ainda vão inventar";
        String outroMinisterio = "Novo nome do Ministério que ainda vão inventar";
        
        Helpers.route(builder.uri(controllers.routes.CidadaoController.promoveAFuncionario(cidadao.getId().toString(), umMinisterio).url()).method("POST"));
        
        Helpers.route(builder.uri(controllers.routes.CidadaoController.promoveAFuncionario(cidadao.getId().toString(), outroMinisterio).url()).method("POST"));

        // e via GET
        Result result2 = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        List<Cidadao> cidadaos2 = jsonToList(contentAsString(result2));

        // agora o teste
        Cidadao cidadao2 = cidadaos2.get(0);
        assertTrue(cidadao2.isFuncionario());
        assertEquals(outroMinisterio, cidadao2.getMinisterioDeAfiliacao());
    }

    @Test
    public void deveDespromoverFuncionario() throws IOException {
        // pegar um usuario conhecido no BD.
    	
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        assertEquals(OK, result.status());
        List<Cidadao> cidadaos = jsonToList(contentAsString(result));

        // agora o teste
        Cidadao cidadao = cidadaos.get(0);

        String umMinisterio = "Ministério que ainda vão inventar";
        Helpers.route(builder.uri(controllers.routes.CidadaoController.promoveAFuncionario(cidadao.getId().toString(), umMinisterio).url()).method("POST"));
        Result result2 = Helpers.route(builder.uri(controllers.routes.CidadaoController.removePapelDeFuncionario(cidadao.getId().toString()).url()).method("DELETE"));
        assertEquals(OK, result2.status());

        // e via GET
        // pegar um usuario conhecido no BD.
        Result result3 = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        List<Cidadao> cidadaos3 = jsonToList(contentAsString(result3));
        assertEquals(1, cidadaos3.size());

        // agora o teste
        Cidadao cidadao3 = cidadaos3.get(0);
        assertFalse(cidadao3.isFuncionario());
        assertNull(cidadao3.getMinisterioDeAfiliacao());
    }

    @Test
    public void deveRetornarApenasFuncionarios() throws IOException {
        Result result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getCidadaos("raquel", 0, 10).url()).method("GET"));
        List<Cidadao> cidadaos = jsonToList(contentAsString(result));
        Cidadao cidadao = cidadaos.get(0);

        String umMinisterio = "Ministério que ainda vão inventar";
        Helpers.route(builder.uri(controllers.routes.CidadaoController.promoveAFuncionario(cidadao.getId().toString(), umMinisterio).url()).method("POST"));

        result = Helpers.route(builder.uri(controllers.routes.CidadaoController.getFuncionarios("r", 0, 10).url()).method("GET"));
        cidadaos = jsonToList(contentAsString(result));
        Cidadao cidadao2 = cidadaos.get(0);
        assertEquals(1, cidadaos.size());
        assertEquals("raquel", cidadao2.getNome());
    }
}
