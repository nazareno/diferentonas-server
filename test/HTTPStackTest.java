import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import models.Cidade;

import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.WithServer;

public class HTTPStackTest extends WithServer {

    private int timeout = 5000;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .build();
    }

    /**
     * Test method for {@link controllers.CidadeController#index()}.
     */
    @Test
    public void testIndex() {
        String url = "/";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(OK, response.getStatus());
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link controllers.CidadeController#get(Long)}.
     */
    @Test
    public void testCidadeInexistente() {
        String url = "/cidade/0";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(NOT_FOUND, response.getStatus());
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for {@link controllers.CidadeController#get(Long)}.
     */
    @Test
    public void testCidadeExistenteEmas() throws Exception {
        String url = "/cidade/2505907";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(OK, response.getStatus());
            Cidade cidade = Json.fromJson(response.asJson(), Cidade.class);
            assertEquals(2505907, cidade.getId().longValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test method for {@link controllers.CidadeController#get(Long)}.
     */
    @Test
    public void testCidadesSimilaresAUmaInexistente() throws Exception {
        String url = "/cidade/0/similares";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(NOT_FOUND, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test method for {@link controllers.CidadeController#get(Long)}.
     */
    @Test
    public void testCidadesSimilaresAEmas() throws Exception {
        String url = "/cidade/2505907/similares";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(OK, response.getStatus());
            assertTrue(response.asJson().elements().hasNext());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    

    @Test
    public void test404() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/alou";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(NOT_FOUND, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
