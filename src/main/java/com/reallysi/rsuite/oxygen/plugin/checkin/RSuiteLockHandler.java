package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.io.PrintStream;
import java.net.URL;

import ro.sync.exml.plugin.lock.LockException;
import ro.sync.exml.plugin.lock.LockHandler;

public class RSuiteLockHandler implements LockHandler {

	@Override
	public void unlock(URL url) throws LockException {  
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			// Parse url (rsuite:/http://host/user/session/moId)
			log.println("INCOMING URL IS:  " + url);
			log.println("PARSING URL TO GET PARAMETERS...");
  
			RSuiteURLParameters rsuiteURLParameters = RSuiteProtocolUtils.parseRSuiteProtocolURL(url.toString());
			
			log.println("HOST:  " + rsuiteURLParameters.getHost());
			log.println("USERNAME:  " + rsuiteURLParameters.getUserName());
			log.println("SESSION KEY:  " + rsuiteURLParameters.getSessionKey());
			log.println("MOID:  " + rsuiteURLParameters.getMoId());
  			
  			RSuiteCheckInDialog checkin = RSuiteCheckInDialogHelper.getRSuiteCheckInDialog(rsuiteURLParameters.toString(), log);
  			if (checkin != null) {
	  			checkin.setLocationRelativeTo(null);
	                        checkin.setVisible(true);
				RSuiteCheckInDialogHelper.remove(rsuiteURLParameters.toString());
  			}
		}
		catch(Throwable t){
			if (log != null) t.printStackTrace(log);
		}
		finally{
			Thread.currentThread().setContextClassLoader(saved);
		}
	}
	PrintStream log = OxyUtils.openLogFile("errorlog_lockhandler.txt");
	
	@Override
	public void updateLock(URL url, int timeout) throws LockException {
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			log.println(url);
			log.println("RESOURCE IS LOCKED.");
		}
		catch(Throwable t){
			if (log != null) t.printStackTrace(log);
		}
		finally{
			Thread.currentThread().setContextClassLoader(saved);
		}
	}
}
