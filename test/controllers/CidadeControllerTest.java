import controllers.CidadeController;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

import static org.junit.Assert.*;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .build();
    }

    @Test
    public void testIndex() {
        Result result = new CidadeController().index();
        assertEquals(OK, result.status());
        assertEquals("text/plain", result.contentType().get());
        assertEquals("utf-8", result.charset().get());
        assertTrue(contentAsString(result).contains("Olar"));
    }
}
