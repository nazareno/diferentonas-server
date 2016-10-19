package controllers;

import java.text.ParseException;

import javax.inject.Inject;

import models.Cidadao;
import models.Token;
import play.Configuration;

import org.joda.time.DateTime;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public final class AuthUtils {
	
	public static final String AUTH_HEADER_KEY = "Authorization";
	
	private final JWSHeader jwtHeader = new JWSHeader(JWSAlgorithm.HS256);
	private final String tokenSecret;
	
	@Inject
	public AuthUtils(Configuration configuration) {
		this.tokenSecret = configuration.getString("play.crypto.secret", "Zy0F9i7Sicgo1KFt6EBW4@d9zsu`5aYA;Yd2c5Rdp[uQcnSLZR;ZL[VDQ2c[@zrI");
	}
	
	public String getSubject(String authHeader) throws ParseException, JOSEException {
		return decodeToken(authHeader).getSubject();
	}
	
	public JWTClaimsSet decodeToken(String authHeader) throws ParseException, JOSEException {
		SignedJWT signedJWT = SignedJWT.parse(getSerializedToken(authHeader));
		if (signedJWT.verify(new MACVerifier(tokenSecret))) {
			return signedJWT.getJWTClaimsSet();
		} else {
			throw new JOSEException("Signature verification failed");
		}
	}
	
	public Token createToken(String host, Cidadao cidadao) throws JOSEException {
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
				.subject(cidadao.getId().toString()).issuer(host)
				.issueTime(DateTime.now().toDate())
				.expirationTime(DateTime.now().plusDays(14).toDate());
		JWSSigner signer = new MACSigner(tokenSecret);
		SignedJWT jwt = new SignedJWT(jwtHeader, builder.build());
		jwt.sign(signer);
		
		return new Token(jwt.serialize());
	}
	
	public String getSerializedToken(String authHeader) {
		return authHeader.split(" ")[1];
	}
}
