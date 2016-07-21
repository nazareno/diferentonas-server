package controllers;

import java.io.Serializable;

public class Token implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5586848722138610622L;
	private String token;

	public Token(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
