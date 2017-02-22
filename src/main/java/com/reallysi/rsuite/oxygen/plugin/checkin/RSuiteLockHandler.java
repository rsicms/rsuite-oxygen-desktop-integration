package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.net.URL;

import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.plugin.lock.LockHandler;

public class RSuiteLockHandler implements LockHandler {

	@Override
	public void unlock(URL url) throws LockException {
		java.io.PrintStream out = OxyUtils.openLogFile("errorlog_lockhandler_unlock.txt");
  
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			// Parse url (rsuite:/http://host/user/session/moId)
			out.println("INCOMING URL IS:  " + url);
			out.println("PARSING URL TO GET PARAMETERS...");
  
			RSuiteURLParameters rsuiteURLParameters = RSuiteProtocolUtils.parseRSuiteProtocolURL(url.toString());
			
			out.println("HOST:  " + rsuiteURLParameters.getHost());
			out.println("USERNAME:  " + rsuiteURLParameters.getUserName());
			out.println("SESSION KEY:  " + rsuiteURLParameters.getSessionKey());
  			out.println("MOID:  " + rsuiteURLParameters.getMoId());
  			
  			RSuiteCheckInDialog checkin = RSuiteCheckInDialogHelper.getRSuiteCheckInDialog(url.toString(), out);
  			
  			checkin.setLocationRelativeTo(null);
            checkin.setVisible(true);
		}
		catch(Throwable t){
			if (out != null) t.printStackTrace(out);
		}
		finally{
			Thread.currentThread().setContextClassLoader(saved);
			if (out != null) {
			    out.flush();
			    out.close();
			}
		}
	}

	@Override
	public void updateLock(URL url, int timeout) throws LockException {
		java.io.PrintStream out = OxyUtils.openLogFile("errorlog_lockhandler_update.txt");
  
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			out.println(url);
			out.println("RESOURCE IS LOCKED.");
		}
		catch(Throwable t){
			if (out != null) t.printStackTrace(out);
		}
		finally{
			Thread.currentThread().setContextClassLoader(saved);
			if (out != null) {
			    out.flush();
			    out.close();
			}
		}
	}
}
