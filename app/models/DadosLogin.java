package models;

public class DadosLogin {

	private String clientId;

	private String redirectUri;

	private String code;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "DadosLogin [clientId=" + clientId + ", redirectUri="
				+ redirectUri + ", code=" + code + "]";
	}

}
