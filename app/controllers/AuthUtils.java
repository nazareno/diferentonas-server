package controllers;

import java.text.ParseException;

import models.Cidadao;
import models.Token;

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
	
	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private static final String TOKEN_SECRET = System.getProperty("play.crypto.secret", "Zy0F9i7Sicgo1KFt6EBW4@d9zsu`5aYA;Yd2c5Rdp[uQcnSLZR;ZL[VDQ2c[@zrI");
	private static final String ADMIN_MAIL = System.getProperty("diferentonas.admin.email","admin@mail.com");
	public static final String AUTH_HEADER_KEY = "Authorization";
	
	public static String getSubject(String authHeader) throws ParseException, JOSEException {
		return decodeToken(authHeader).getSubject();
	}
	
	public static JWTClaimsSet decodeToken(String authHeader) throws ParseException, JOSEException {
		SignedJWT signedJWT = SignedJWT.parse(getSerializedToken(authHeader));
		if (signedJWT.verify(new MACVerifier(TOKEN_SECRET))) {
			return signedJWT.getJWTClaimsSet();
		} else {
			throw new JOSEException("Signature verification failed");
		}
	}
	
	public static Token createToken(String host, Cidadao cidadao) throws JOSEException {
		JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
				.subject(cidadao.getId().toString()).issuer(host)
				.issueTime(DateTime.now().toDate())
				.expirationTime(DateTime.now().plusDays(14).toDate())
				.claim("admin", ADMIN_MAIL.equals(cidadao.getLogin()))
				.claim("funcionario", cidadao.isFuncionario());
		
		JWSSigner signer = new MACSigner(TOKEN_SECRET);
		SignedJWT jwt = new SignedJWT(JWT_HEADER, builder.build());
		jwt.sign(signer);
		
		return new Token(jwt.serialize());
	}
	
	public static String getSerializedToken(String authHeader) {
		return authHeader.split(" ")[1];
	}
}
