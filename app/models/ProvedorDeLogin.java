package models;

public enum ProvedorDeLogin {
	
	FACEBOOK("facebook"), GOOGLE("google");

	private String name;

	private ProvedorDeLogin(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public String capitalize() {
		return this.name.toUpperCase();
	}
}