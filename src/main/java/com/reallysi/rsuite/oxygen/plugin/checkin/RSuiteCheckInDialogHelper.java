package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.reallysi.rsuite.client.api.impl.RsuiteRepositoryImpl;

public class RSuiteCheckInDialogHelper {
	
	private static Map<String, RSuiteCheckInDialog> rsuiteCheckInDialogs = new HashMap<String, RSuiteCheckInDialog>();
	
	public static RSuiteCheckInDialog initiateRSuiteCheckInDialog (String url, PrintStream out) throws Exception {
		return createNewRSuiteCheckInDialog(url, out, true);
	}
	
	public static RSuiteCheckInDialog getRSuiteCheckInDialog (String url, PrintStream out) throws Exception {
		if (rsuiteCheckInDialogs.containsKey(url)) {
			return rsuiteCheckInDialogs.get(url);
		}

		return createNewRSuiteCheckInDialog(url, out, false);
	}


	private static RSuiteCheckInDialog createNewRSuiteCheckInDialog(String url, PrintStream out, boolean storeDialog) throws Exception {		
		RSuiteCheckInDialog rsuiteCheckInDialog = null;
		
		RSuiteURLParameters rsuiteURLParameters = RSuiteProtocolUtils.parseRSuiteProtocolURL(url);
		
		// see if we already have a repository initialized
			RsuiteRepositoryImpl repository = Handler.getRepository();

			// SESSION WAS PASSED IN, INITIALIZE REPOSITORY
			if(repository == null){
				out.println("SESSION PASSED IN BY URL, INITIALIZE REPOSITORY");
				repository = new RsuiteRepositoryImpl(rsuiteURLParameters.getUserName(), "", rsuiteURLParameters.getHost());
				out.println("REPOSITORY INITIALIZED.");

				// NO NEED TO LOGIN SINCE WE HAVE SESSION KEY ALREADY
				out.println("SETTING THE REPOSITORY SESSION KEY...");
				repository.sessionKey = rsuiteURLParameters.getSessionKey();
				out.println("SESSION KEY SET.  NO NEED FOR LOGIN");
			}	
			
		// PERFORM CHECK IN
        if (repository.isCheckedOut(rsuiteURLParameters.getMoId())) {
            out.println("SHOWING CHECK IN DIALOG...");
            rsuiteCheckInDialog = new RSuiteCheckInDialog(repository, rsuiteURLParameters.getMoId());
            
            if (storeDialog) {
            	rsuiteCheckInDialogs.put(url, rsuiteCheckInDialog);
            }
        }
		
		return rsuiteCheckInDialog;
	}

	public static void remove (String url) {
		rsuiteCheckInDialogs.remove(url);
	}
}
