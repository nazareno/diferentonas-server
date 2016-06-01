package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import module.MainModule;
import org.junit.After;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

public class FeedControllerTest extends WithApplication {

    @After
    public void limpaBancoAposTeste() {

    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().bindings(new MainModule())
                .build();
    }

    @Test
    public void deveIniciarComFeedVazio(){
        Result result = Helpers.route(controllers.routes.FeedController.getNovidades(0, 10));
        assertEquals(OK, result.status());
        assertNotNull(Helpers.contentAsString(result));
        JsonNode node = Json.parse(Helpers.contentAsString(result));
        assertTrue(node.isArray());
    }

}
