package me.michaeldick.npr.model;

public class NprAuth {

	String userId;
	String auth;
	
	public NprAuth(String userId, String auth) {
		this.userId = userId;
		this.auth = auth;
	}

	public String getUserId() {
		return userId;
	}

	public String getAuth() {
		return auth;
	}
}
