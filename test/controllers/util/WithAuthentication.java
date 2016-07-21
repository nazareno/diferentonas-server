package controllers.util;

import java.util.UUID;

import models.Cidadao;
import models.CidadaoDAO;

import org.junit.After;
import org.junit.Before;

import play.db.jpa.JPAApi;
import play.mvc.Http.RequestBuilder;
import play.test.WithApplication;

public class WithAuthentication extends WithApplication {
	
	protected String token;
	protected RequestBuilder builder;


    @Before
    public void autenticaAdmin() {
    	
    	app.injector().instanceOf(JPAApi.class).withTransaction(()->{
    		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
    		Cidadao admin = cidadaoDAO.findByLogin("admin@mail.com");
    		token = UUID.randomUUID().toString();
			admin.setToken(token);
    		cidadaoDAO.saveAndUpdate(admin);
    	});
    	builder = new RequestBuilder().header("X-Auth-Token", token);
    }

    @After
    public void desautenticaAdmin() {
    	app.injector().instanceOf(JPAApi.class).withTransaction(()->{
    		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
    		Cidadao admin = cidadaoDAO.findByLogin("admin@mail.com");
    		admin.setToken("");
    		cidadaoDAO.saveAndUpdate(admin);
    	});
    }
}
