package com.reallysi.rsuite.oxygen.plugin.checkin;

public class RSuiteProtocolUtils {

	public static RSuiteURLParameters parseRSuiteProtocolURL (String url) {
		RSuiteURLParameters rsuiteURLParameters = new RSuiteURLParameters();

		String[] params = url.split("/");
		String host	= params[1] + "//" + params[3];
		String userName	= params[4];
		String sessionKey = params[5];
		String[] mo	= params[6].split(".xml");
		String moId	= mo[0];

		rsuiteURLParameters.setHost(host);
		rsuiteURLParameters.setUserName(userName);
		rsuiteURLParameters.setSessionKey(sessionKey);
		rsuiteURLParameters.setMoId(moId);

		return rsuiteURLParameters;
	}
	
}
