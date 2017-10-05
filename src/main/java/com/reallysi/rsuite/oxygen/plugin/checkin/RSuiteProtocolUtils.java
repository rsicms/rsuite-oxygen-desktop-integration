package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.net.URL;


public class RSuiteProtocolUtils {
	public static RSuiteURLParameters parseRSuiteProtocolURL (URL url) {
		return parseRSuiteProtocolURL(url.toString());
	}
	public static RSuiteURLParameters parseRSuiteProtocolURL (String url) {
		
		RSuiteURLParameters rsuiteURLParameters = new RSuiteURLParameters();

		String[] params = url.split("/");
		String protocol = params[1];
		String host	= params[3];
		String userName	= params[4];
		String sessionKey = params[5];
		String moId = params[6];
		if (moId.indexOf(".xml") != -1) {
			String[] mo	= moId.split(".xml");
			moId = mo[0];
		}
		
		

		rsuiteURLParameters.setProtocol(protocol);
		rsuiteURLParameters.setHost(host);
		rsuiteURLParameters.setUserName(userName);
		rsuiteURLParameters.setSessionKey(sessionKey);
		rsuiteURLParameters.setMoId(moId);

		return rsuiteURLParameters;
	}
}
