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

    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
    }

    @Test
    public void indexExiste() {
        Result result =  new CidadeController().index();
        assertEquals(result.status(), OK);
    }


    @Test
    public void deveIniciarSemCidades() throws Exception {
        List<Cidade> cidades = JPA.em().createQuery("FROM " + Cidade.class.getName()).getResultList();
        assertTrue(cidades.isEmpty());
    }

    @Test
    public void deveSalvarDisciplinaNoBD() throws Exception {
        Cidade c1 = new Cidade("Santa Luzia");
        JPA.em().persist(c1);

        List<Cidade> cidades = JPA.em().createQuery("FROM " + Cidade.class.getName()).getResultList();
        assertEquals(cidades.size(), 1);
        assertEquals(cidades.get(0).getNome(), "Santa Luzia");
    }
}
