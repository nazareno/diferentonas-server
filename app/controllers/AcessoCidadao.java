package controllers;

import java.text.ParseException;

import javax.inject.Inject;

import org.joda.time.DateTime;

import play.Logger;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

public class AcessoCidadao extends Security.Authenticator {
	
	private AuthUtils authenticator;

	@Inject
	public AcessoCidadao(AuthUtils authenticator) {
		this.authenticator = authenticator;
	}
	
    @Override
    public String getUsername(Context ctx) {
    	String[] authTokenHeaderValues = ctx.request().headers().get(AuthUtils.AUTH_HEADER_KEY);
    	if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
    		String authHeader = authTokenHeaderValues[0];

			try {
				JWTClaimsSet claimSet = (JWTClaimsSet) authenticator.decodeToken(authHeader);
				if (new DateTime(claimSet.getExpirationTime()).isAfter(DateTime.now())) {
					return claimSet.getSubject();
				} 
			} catch (ParseException | JOSEException e) {
				Logger.error("Erro na validação do token: " + e.getMessage());
			}
    	}

        return null;
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return unauthorized();
    }

}