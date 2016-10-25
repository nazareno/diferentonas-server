package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import models.Atualizacao;
import models.AtualizacaoDAO;
import models.Novidade;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.Helpers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import controllers.util.WithAuthentication;

/**
 * Testes do controller.
 */
public class AtualizacaoControllerTest extends WithAuthentication {
	
	private AtualizacaoDAO atualizacaoDAO;
	private File novaAtualizacao;
	private String data;

    @Before
    public void setUp() throws IOException {
        this.atualizacaoDAO = app.injector().instanceOf(AtualizacaoDAO.class);
        GregorianCalendar calendar = new GregorianCalendar(2016, 5, 28);
        data = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
        novaAtualizacao = new File(atualizacaoDAO.getFolder() + "/iniciativas-" + data + ".csv");
        novaAtualizacao.createNewFile();
    }

    @After
    public void desautenticaAdmin() {
        novaAtualizacao.delete();
    }

    @Test
    public void deveListarAtualizacaoInicial() throws JsonParseException, JsonMappingException, IOException {
    	
    	RequestBuilder request = builder
        .method("GET")
        .uri(controllers.routes.AtualizacaoController.getStatus().url());
    	
        Result result = Helpers.route(request);
        assertEquals(OK, result.status());
        String conteudoResposta = contentAsString(result);
        assertNotNull(conteudoResposta);
        System.err.println(conteudoResposta);
        List<Atualizacao> atualizacoes = new ObjectMapper().readValue(conteudoResposta, new TypeReference<List<Atualizacao>>() {});
        assertTrue(atualizacoes.isEmpty());
    }
}
