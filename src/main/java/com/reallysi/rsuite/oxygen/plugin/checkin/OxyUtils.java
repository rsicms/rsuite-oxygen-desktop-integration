package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

/**
 * General utilities for oxygen-plugin classes.
 */
public class OxyUtils {
	
	private static Object tmpDirMutex = new Object();
	private static File tmpdir = null;
	private static boolean isDebug = false;
	static {
	    String s = System.getenv("RSUITE_OXYGEN_DEBUG");
	    if (s != null && s.equals("true")) {
	        isDebug = true;
	    }
	}

	/**
	 * Open a named file for logging.
	 * <p>Logging is only enabled if the RSUITE_OXYGEN_DEBUG envvar is
	 * set to "true".  If not set, a "sink" PrintStream is returned
	 * so plugin code does not have to check if logging is enabled.
	 * </p>
	 * <p>If logging is enabled and since we may be running in an applet
	 * context, we attempt to create the log file in the default
	 * temporary directory.
	 * </p>
	 * <p>This method throws no exceptions.
	 * </p>
	 * @param  name     Basename of file.
	 * @return PrintStream for log/debugging messages.
	 */
	public static PrintStream openLogFile(
	        String name
	) {
	    if (!isDebug) return nullPS;
	    try {
	        if (tmpdir == null) {
	            synchronized (tmpDirMutex) {
	                if (tmpdir == null) {
	                    tmpdir = File.createTempFile("rsioxy", null);
	                    tmpdir.delete();
	                    if (!tmpdir.mkdir()) {
	                        return nullPS;
	                    }
//	                    FileUtils.forceDeleteOnExit(tmpdir);
	                }
	            }
	        }
	        File f = new File(tmpdir, name);
	        // System.out.println(" + [DEBUG] openLogFile(): Created temp log \"" + f.getAbsolutePath() + "\"");
	        return new PrintStream(new FileOutputStream(f));
	    } catch (Throwable t) {
	        //t.printStackTrace();  // unsure if this will be seen
	        return nullPS;
	    }
	}

	public static void closeCurrentOxygenEditorWithoutSaving (StandalonePluginWorkspace standalonePluginWorkspace) {
		closeCurrentOxygenEditor(standalonePluginWorkspace, false);
	}

	public static void closeCurrentOxygenEditor (StandalonePluginWorkspace standalonePluginWorkspace, boolean askForSave) {
		WSEditor editorAccess = standalonePluginWorkspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        editorAccess.close(askForSave);
	}
	
	/**
	 * Dummy PrintStream if cannot open local log files.
	 */
	private static NullPrintStream nullPS = new NullPrintStream(); 
	private static class NullPrintStream extends PrintStream {
	    NullPrintStream() { super(new NullOutputStream()); }
	}
	
	/**
	 * Dummy OutputStream that functions as a sink.
	 */
	private static class NullOutputStream extends OutputStream {
	    public void close() throws IOException { }
	    public void flush() throws IOException { }
	    public void write(byte[] b) throws IOException { }
	    public void write(byte[] b, int off, int len) throws IOException { }
	    public void write(int b) throws IOException { }
	}
}
