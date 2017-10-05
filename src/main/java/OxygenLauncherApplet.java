import java.io.File;

/**
 * Applet to launch Oxygen editor.
 * <p>The applet checks the <tt>RSUITE_OXYGEN_APP</tt> envvar
 * to obtain the pathname location of the oxygen executable.
 * If the envvar is not set, then oxygen must be in the search
 * path for it to be executed.
 * </p>
 * <p>The following parameters are supported by this applet:
 * </p>
 * <dl>
 * <dt><tt>host</tt></dt>
 * <dd>RSuite host URL</dd>
 * <dt><tt>user</tt></dt>
 * <dd>RSuite user name</dd>
 * <dt><tt>sessionKey</tt></dt>
 * <dd>Current RSuite session key</dd>
 * <dt><tt>moId</tt></dt>
 * <dd>Managed object ID of XML document to load</dd>
 * </dl>
 */
public class OxygenLauncherApplet extends java.applet.Applet {
	
	public static final long serialVersionUID = 1;
	
	public void init(String host, String user, String sessionKey, String moId) {
		String oxygenPath = findO2(System.getProperty("oxygen"), System.getenv("RSUITE_OXYGEN_APP"));
		if (oxygenPath == null || oxygenPath.trim().equals("")) {
		    try {
		        oxygenPath = System.getenv("RSUITE_OXYGEN_APP");
		    } catch (Exception e) {
		        // Maybe its in the search path.
		        oxygenPath = "oxygen";
		    }
		}
		try {
			
			String doc = "rsuite:/" + host + "/" + user + "/" + sessionKey + "/" + moId + ".xml";
            String[] cmd = {oxygenPath, doc };
            Process p = Runtime.getRuntime().exec(cmd);
    		p.waitFor();
    		System.out.println("Exit code: " + p.exitValue());
            
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static boolean empty(String s) {
		return s == null || "".equals(s.trim());
	}
	
	public String findO2(String prop, String env) {
		if (prop != null && !prop.isEmpty() && new File(prop).exists()) {
			return prop;
		}
		if (env != null && !env.isEmpty() && new File(env).exists()) {
			return env;
		}
		return "oxygen";
	}

	public static void main (String [] args){
		OxygenLauncherApplet obj = new OxygenLauncherApplet();
		
		if (args.length < 4)
			throw new RuntimeException("Init parameters must be provided");
		else
			obj.init(args[0], args[1], args[2], args[3]);
	}
	
	
    /**
     * Check if running under a Windows OS.
     * @return  <tt>true</tt> if running under Windows, else <tt>false</tt>.
     */
    public static boolean isWindows()
    {
        String osname   = System.getProperty("os.name");
        boolean windows = (osname.indexOf("Windows") >= 0);
        return windows;
    }
}