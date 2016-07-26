package models;

public enum ProvedorDeLogin {
	
	FACEBOOK("facebook", "diferentonas.secret.facebook"), GOOGLE("google", "diferentonas.secret.google");

	private String name;
	private String secretProp;

	private ProvedorDeLogin(final String name, final String secretProp) {
		this.name = name;
		this.secretProp = secretProp;
	}

	public String getName() {
		return this.name;
	}

	public String capitalize() {
		return this.name.toUpperCase();
	}
	
	public String getSecretProp(){
		return this.secretProp;
	}
}