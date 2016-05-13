import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithServer;
import play.libs.ws.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;

import static play.test.Helpers.*;

public class HTTPStackTest extends WithServer {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .build();
    }

    @Test
    public void testIndex() throws Exception {
        int timeout = 5000;
        String url = "http://localhost:" + this.testServer.port() + "/";

        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url).get();
            WSResponse response = stage.toCompletableFuture().get();
            assertEquals(OK, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test404() throws Exception {
        int timeout = 5000;
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
