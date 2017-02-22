package com.reallysi.rsuite.oxygen.plugin.checkin;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

public class RSuiteCheckInPlugin extends Plugin {
	
	private static RSuiteCheckInPlugin instance = null;

	public RSuiteCheckInPlugin(PluginDescriptor descriptor) {
		super(descriptor);

		if (instance != null) {
			throw new IllegalStateException("Already instantiated!");
		}
		instance = this;
	}
  
	public static RSuiteCheckInPlugin getInstance() {
		return instance;
	}
}