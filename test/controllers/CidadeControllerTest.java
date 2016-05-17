package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import org.junit.Ignore;
import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.WithApplication;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().build();
	}

	@Test
	public void testIndex() {
		Result result = new CidadeController().index();
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType().get());
		assertEquals("utf-8", result.charset().get());
		assertTrue(contentAsString(result).contains("Olar"));
	}

	/**
	 * Test method for {@link controllers.CidadeController#getCidades()}.
	 * FIXME and remove {@link Ignore}. Maybe we should mock and not inject...
	 */
	@Test
	@Ignore("Not working without injecting JPAApi and I don't know why...")
	public void testGetCidadeInexistente() {
		Result result = new CidadeController().get(0L);
		assertEquals(NOT_FOUND, result.status());
	}
}
