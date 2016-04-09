import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.CidadeController;
import models.Cidade;
import org.junit.*;

import play.Application;
import play.db.jpa.JPA;
import play.db.jpa.JPAApi;
import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import static play.mvc.Results.status;
import static play.test.Helpers.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest extends WithApplication {

    private final JPAApi jpaApi;

    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
    }

    @After
    public void tearDown(){
    }

    @Inject
    public ApplicationTest(JPAApi api) {
        super();
        this.jpaApi = api;
    }

    @Test
    public void indexExiste() {
        Result result =  new CidadeController().index();
        assertEquals(result.status(), OK);
    }


    @Test
    public void deveIniciarSemCidades() throws Exception {
        jpaApi.withTransaction(() -> {
            List<Cidade> cidades = JPA.em().createQuery("FROM " + Cidade.class.getName()).getResultList();
            assertTrue(cidades.isEmpty());
        });
    }

}
