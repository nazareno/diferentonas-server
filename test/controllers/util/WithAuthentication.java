package controllers.util;

import models.Cidadao;
import models.CidadaoDAO;

import org.junit.After;
import org.junit.Before;

import play.Configuration;
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
    		if(cidadaoDAO.findByLogin(adminEmail) == null){
    			Cidadao cidadao = new Cidadao("Governo Federal", adminEmail);
                cidadao.setFuncionario(true);
                cidadao.setMinisterioDeAfiliacao("Governo Federal");
				return cidadaoDAO.saveAndUpdate(cidadao);
    		}
    		return cidadaoDAO.findByLogin(adminEmail);
    	});
    	
    	AuthUtils authenticator = app.injector().instanceOf(AuthUtils.class);
		token = authenticator.createToken("localhost", admin).getToken();
    	builder = new RequestBuilder().header("authorization", "token " + token);
    }

    @After
    public void desautenticaAdmin() {
    	app.injector().instanceOf(JPAApi.class).withTransaction(()->{
    		Configuration configuration = app.injector().instanceOf(Configuration.class);
    		String adminEmail = configuration.getString(Cidadao.ADMIN_EMAIL);
    		CidadaoDAO cidadaoDAO = app.injector().instanceOf(CidadaoDAO.class);
    		Cidadao admin = cidadaoDAO.findByLogin(adminEmail);
    		if(admin != null){
    			cidadaoDAO.remove(admin);
    		}
    	});
    }
}
