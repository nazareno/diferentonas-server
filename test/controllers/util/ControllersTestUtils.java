package controllers.util;

import static play.test.Helpers.route;

import java.io.IOException;

import play.Logger;
import play.mvc.Http;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ControllersTestUtils {

    public static JsonNode criaJsonParaPostOpiniao(String conteudo) throws IOException {
        return (new ObjectMapper()).readTree("{ \"conteudo\": \"" + conteudo + "\", " +
                "\"tipo\": \"coracao\"}");
    }

    public static Result enviaPOSTAddOpiniao(String conteudo, long iniciativa, String token) throws IOException {
        JsonNode json = criaJsonParaPostOpiniao(conteudo);
        return enviaPOSTAddOpiniao(json, iniciativa, token);
    }

    public static Result enviaPOSTAddOpiniao(JsonNode json, long iniciativaExemplo, String token) {
        Logger.debug("Requisição para add opinião: " + json.toString());
        Http.RequestBuilder request = new Http.RequestBuilder().method("POST")
                .bodyJson(json)
                .header("authorization", "token " + token)
                .uri(controllers.routes.OpiniaoController.addOpiniao(iniciativaExemplo).url());
        return route(request);
    }

}
