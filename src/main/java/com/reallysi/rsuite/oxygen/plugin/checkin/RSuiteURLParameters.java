package com.reallysi.rsuite.oxygen.plugin.checkin;

public class RSuiteURLParameters {

	private String protocol;
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	private String host;
	private String userName;
	private String sessionKey;
	private String moId;

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}
	public String getMoId() {
		return moId;
	}
	public void setMoId(String moId) {
		this.moId = moId;
	}

	public String toString() {
		return "rsuite:/" + protocol + "//" + host + "/" + userName + "/" + sessionKey + "/" + moId; 
	}
	
}
