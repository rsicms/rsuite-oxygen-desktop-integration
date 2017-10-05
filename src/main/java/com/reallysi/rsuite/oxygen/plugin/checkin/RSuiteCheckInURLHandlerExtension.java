package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.net.URLStreamHandler;

import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension;

/**
 * This class handles Rsuite URLS when opening Oxygen.
 * rsuite:/host/user/session/moId
 */
public class RSuiteCheckInURLHandlerExtension implements URLStreamHandlerWithLockPluginExtension {

	private static final String RSUITE = "rsuite";
	private static LockHandler lock = null;
	
	public static LockHandler getLock(){
		if(lock == null){
			lock = new com.reallysi.rsuite.oxygen.plugin.checkin.RSuiteLockHandler();
		}
		return lock;
	}
	
	/**
	 * Gets the Handler for our custom RSuite protocol
	 */
	@Override
	public URLStreamHandler getURLStreamHandler(String protocol){
		if(protocol.equals(RSUITE)){
			URLStreamHandler handler = new com.reallysi.rsuite.oxygen.plugin.checkin.Handler();
			return handler;
		}
		return null;
	}
	
	/**
	 * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#getLockHandler()
	 */
	@Override
	public LockHandler getLockHandler(){
		return getLock();
	}
	
	/**
	 * @see ro.sync.exml.plugin.urlstreamhandler.URLStreamHandlerWithLockPluginExtension#isSupported(java.lang.String)
	 */
	@Override
	public boolean isLockingSupported(String protocol) {
		if(protocol.equals(RSUITE))
			return true;
		return false;
	}

}