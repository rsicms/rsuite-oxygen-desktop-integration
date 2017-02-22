package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.awt.Frame;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import ro.sync.exml.plugin.lock.LockHandler;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserPluginExtension2;
import ro.sync.exml.plugin.urlstreamhandler.URLChooserToolbarExtension;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class RSuiteCheckInChooserExtension
    implements URLChooserPluginExtension2, URLChooserToolbarExtension {

	@Override
	public URL[] chooseURLs(StandalonePluginWorkspace standalonePluginWorkspace) {
		URL[] urls = null;

		java.io.PrintStream out = OxyUtils.openLogFile("errorlog_chooser.txt");

		try {
		    LockHandler lock = RSuiteCheckInURLHandlerExtension.getLock();
		    if(lock == null)
		        out.println("LOCK IS NULL");
		    else
		        out.println("LOCK IS NOT NULL");

		    out.println("URL IS:  " + Handler.getUrl());
		    out.println("UNLOCKING DOCUMENT...");
            if (Handler.getRepository().isCheckedOut(Handler.getMoId())) {
            	String url = Handler.getUrl();
            	synchronized (url.intern()) {
            		RSuiteCheckInDialog rsuiteCheckInDialog = RSuiteCheckInDialogHelper.initiateRSuiteCheckInDialog(url, out);
                    lock.unlock(new URL(url));                    
                    if (rsuiteCheckInDialog.isFileCheckedIn()) {
                    	OxyUtils.closeCurrentOxygenEditorWithoutSaving(standalonePluginWorkspace);
                    }

                    RSuiteCheckInDialogHelper.remove(url);
				}
            } else {
                Frame frame = new Frame();
                JOptionPane.showMessageDialog(frame,
                        "Document is not checked out",
                        "RSuite Notification",
                        JOptionPane.ERROR_MESSAGE);

            }

		} catch(Throwable t){
		    if (out != null) t.printStackTrace(out);

		} finally{
		    if (out != null) {
		        out.flush();
		        out.close();
		    }
		}
		return urls;
	}

	@Override
	public String getMenuName() {
		return "RSuite - Check In";
	}

	@Override
	public Icon getToolbarIcon() {
		return new ImageIcon(getClass().getResource("/images/checkin.png"));
	}

	@Override
	public String getToolbarTooltip() {
		return "Check in document into RSuite";
	}

}
