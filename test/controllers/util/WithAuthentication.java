package controllers.util;

import models.Cidadao;
import models.CidadaoDAO;

import org.junit.After;
import org.junit.Before;

import play.Configuration;
import play.Logger;
import play.db.jpa.JPAApi;
import play.mvc.Http.RequestBuilder;
import play.test.WithApplication;

import com.nimbusds.jose.JOSEException;

import controllers.AuthUtils;

public class WithAuthentication extends WithApplication {
	
	protected String token;
	protected RequestBuilder builder;
	protected Cidadao admin;


    @Before
    public void autenticaAdmin() throws JOSEException{
    	
    	admin = app.injector().instanceOf(JPAApi.class).withTransaction(()->{
    		Configuration configuration = app.injector().instanceOf(Configuration.class);
    		String adminEmail = configuration.getString(Cidadao.ADMIN_EMAIL);
    		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
    		return cidadaoDAO.findByLogin(adminEmail);
    	});
    	
		token = AuthUtils.createToken("localhost", admin).getToken();
    	builder = new RequestBuilder().header("authorization", "token " + token);
    }

    @After
    public void desautenticaAdmin() {
    	app.injector().instanceOf(JPAApi.class).withTransaction(()->{
    		Configuration configuration = app.injector().instanceOf(Configuration.class);
    		String adminEmail = configuration.getString(Cidadao.ADMIN_EMAIL);
    		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
    		Cidadao admin = cidadaoDAO.findByLogin(adminEmail);
    		cidadaoDAO.saveAndUpdate(admin);
    	});
    }
}
