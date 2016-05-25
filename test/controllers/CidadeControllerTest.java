package controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import module.MainModule;


import org.junit.Before;
import org.junit.Test;


import play.Application;
import play.db.jpa.JPAApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

/**
 * Testes do controller.
 */
public class CidadeControllerTest extends WithApplication {
	
	private CidadeController controller;

	private JPAApi jpaAPI;

	@Before
	public void setUp() {
		this.controller = app.injector().instanceOf(CidadeController.class);
		this.jpaAPI = app.injector().instanceOf(JPAApi.class);
	}
	
	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().bindings(new MainModule())
		.build();
	}

	@Test
	public void testIndex() {
		Result result = controller.index();
		assertEquals(OK, result.status());
		assertEquals("text/plain", result.contentType().get());
		assertEquals("utf-8", result.charset().get());
		assertTrue(contentAsString(result).contains("Olar"));
	}

	/**
	 * Test method for {@link controllers.CidadeController#getCidades()}.
	 */
	@Test
	public void testGetCidadeInexistente() {
		jpaAPI.withTransaction(() ->{
			Result result = controller.get(0L);
			assertEquals(NOT_FOUND, result.status());
		});
	}

	/**
	 * Test method for {@link controllers.CidadeController#getCidades()}.
	 */
	@Test
	public void testGetCidadeExistente() {
		jpaAPI.withTransaction(() ->{
			Result result = controller.get(2513406L);
			assertEquals(OK, result.status());
			System.out.println(" >>>>>>> ");
			System.out.println(Helpers.contentAsString(result));
			System.out.println(" >>>>>>> ");
		});
	}
}
