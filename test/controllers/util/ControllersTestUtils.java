package controllers.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Logger;
import play.mvc.Http;
import play.mvc.Result;

import java.io.IOException;

import static play.test.Helpers.route;

public class ControllersTestUtils {

    public static JsonNode criaJsonParaPostOpiniao(String conteudo) throws IOException {
        return (new ObjectMapper()).readTree("{ \"conteudo\": \"" + conteudo + "\", " +
                "\"tipo\": \"coracao\"}");
    }

    public static Result enviaPOSTAddOpiniao(String conteudo, long iniciativa) throws IOException {
        JsonNode json = criaJsonParaPostOpiniao(conteudo);
        return enviaPOSTAddOpiniao(json, iniciativa);
    }

    public static Result enviaPOSTAddOpiniao(JsonNode json, long iniciativaExemplo) {
        Logger.debug("Requisição para add opinião: " + json.toString());
        Http.RequestBuilder request = new Http.RequestBuilder().method("POST")
                .bodyJson(json)
                .uri(controllers.routes.OpiniaoController.addOpiniao(iniciativaExemplo).url());
        return route(request);
    }

}
