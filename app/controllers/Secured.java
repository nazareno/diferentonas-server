package controllers;

import javax.inject.Inject;

import models.CidadaoDAO;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

public class Secured extends Security.Authenticator {
	
	@Inject CidadaoDAO dao;

    @Override
    public String getUsername(Context ctx) {
        String[] authTokenHeaderValues = ctx.request().headers().get(LoginController.AUTH_TOKEN_HEADER);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
        	return authTokenHeaderValues[0];
        }

        return "TOKEN";// FIXME delete me to enable access control
//        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return unauthorized();
    }

}