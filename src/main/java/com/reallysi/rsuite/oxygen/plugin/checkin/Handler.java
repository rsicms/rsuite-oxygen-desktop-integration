package com.reallysi.rsuite.oxygen.plugin.checkin;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.reallysi.rsuite.client.api.SchemaInfo;
import com.reallysi.rsuite.client.api.impl.RsuiteRepositoryImpl;

/**
 * Handler for the RSuite protocol
 */
public class Handler extends URLStreamHandler {
	
	// RSuite Connection info
	private static RsuiteRepositoryImpl repository = null;
	private static String username = null;
	private static String password = null;
	private static String host = null;
	private static String sessionKey = null;
	private static String moId = null;
	private static String docURL = null;
	private static String dtd = null;
	private static String dtdUrl = null;
	private static String xsdSchemaDeclaration = null;
	private static String xsdNamespaceDeclaration = null; 
	private static String encoding = null;
	
	private static URLConnection _connection = null;

	private static enum XMLStructureDefinitionType {
		DTD, XSD
	}

	public static RsuiteRepositoryImpl getRepository(){
		return repository;
	}
	public static String getMoId(){
		return moId;
	}
	public static String getUrl(){
		return docURL;
	}
	
	private class RSuiteConnection extends URLConnection{
		
		private String noNamespaceDeclaration = "xsi:nonamespaceschemalocation";
		
		private String namespaceDeclaration = "xsi:schemalocation";
		
		protected RSuiteConnection(URL url){
			super(url);
			setDoOutput(true);
		}
		
		private String parseUrl(URL url){
			// System.out.println(" + [DEBUG] parseUrl(): url=\"" + url + "\"");
			String[] params = url.toString().split("/");
			host			= params[1] + "//" + params[3];
			username		= params[4];
			sessionKey		= params[5];
			// get rid of extension
			String[] mo		= params[6].split(".xml");
			moId			= mo[0];
			return "rsuite:/" + host + "/" + username + "/" + sessionKey + "/" + moId;
		}

		/**
		 * Handles the connection to RSuite
		 */
		public void connect() throws IOException {
  		  	java.io.PrintStream out = openLogFile("errorlog_handler_connect.txt");
  		  
  		  	ClassLoader saved = Thread.currentThread().getContextClassLoader();
			try {
				  // Parse url (rsuite:/http://host/user/session/moId)
				  out.println("INCOMING URL IS:  " + url);
				  out.println("PARSING URL TO GET PARAMETERS...");
				  
				  // url initially comes through with extension to avoid dialog window
				  docURL = parseUrl(url);
				  // set the connection so we have it later
				  _connection = this;

				  out.println("HOST:  " + host);
				  out.println("USERNAME:  " + username);
				  out.println("SESSION KEY:  " + sessionKey);
				  out.println("MOID:  " + moId);
				  
				  Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				  
				  // CONNECT TO RSUITE IF SESSION NOT PRESENT				  
				  out.println("CONNECTION TO RSUITE...");
				  if(sessionKey == null){
					  out.println("SESSION DOES NOT EXIST, PROMPT FOR LOGIN");
					  RSuiteLoginDialog login = new RSuiteLoginDialog();
					  login.setLocationRelativeTo(null);
					  login.setVisible(true);
				  }
				  
				  // SESSION WAS PASSED IN, INITIALIZE REPOSITORY
				  if(repository == null){
					  out.println("SESSION PASSED IN BY URL, INITIALIZE REPOSITORY");
					  repository = new RsuiteRepositoryImpl(username, "", host);
					  out.println("REPOSITORY INITIALIZED.");
					  
					  // NO NEED TO LOGIN SINCE WE HAVE SESSION KEY ALREADY
					  out.println("SETTING THE REPOSITORY SESSION KEY...");
					  repository.sessionKey = sessionKey;
					  out.println("SESSION KEY SET.  NO NEED FOR LOGIN");
				  }				  
			}
			catch (Throwable t) {
			    if (out != null) {
			        t.printStackTrace(out);
			    }
			}
			finally{
				Thread.currentThread().setContextClassLoader(saved);
				if (out != null) {
				    out.flush();
	                out.close();
				}
			}
		}
		
		/**
		 * Open the rsuite protocol url in Oxygen
		 */
		public InputStream getInputStream() throws IOException {
  		  	java.io.PrintStream out = openLogFile("errorlog_handler_inputstream.txt");
  		  	
  		  	InputStream stream = null;
  		  
  		  	ClassLoader saved = Thread.currentThread().getContextClassLoader();
			try {
				  Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				  String newXml = null;
				  
				  // CONNECT TO RSUITE
				  out.println("GETTING DOCUMENT WITH MO ID:  " + moId + "...");
				  String document = repository.getAsString(moId);
				  out.println("DOCUMENT FOUND.");
				  out.print(document);
				  
				  // FIND ENCODING
				  encoding = guessXMLEncoding(document);

				  XMLStructureDefinitionType xmlStructureDefinitionType = getXMLStructureDefinitionType(document);
				  
				  switch (xmlStructureDefinitionType) {
				  	case DTD:
				  		newXml = replaceDtdDefinition(document, out);
					break;
					case XSD:
						newXml = replaceXsdDefinition(document, out);	
					break;
				  }

				  // RETURN DOCUMENT
				  out.println("CREATING INPUT STREAM FROM NEW DOCUMENT...");
				  stream = new ByteArrayInputStream(newXml.getBytes(encoding));
				  out.println("STREAM CREATED SUCCESSFULLY.");
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
			out.println("RETURNING STREAM TO OXYGEN...");
			return stream;
		}

		private String replaceDtdDefinition(String document, java.io.PrintStream out) throws Exception {
			String newXml = null;
			// GET DTD NAME
			  int docTypeIndex = document.toLowerCase().indexOf("doctype");
			  int dtdIndex = document.toLowerCase().indexOf(".dtd");
			  if (docTypeIndex >= 0 && dtdIndex >= 0) {
			      String[] parts = document.substring(
			              docTypeIndex, dtdIndex + 4).split("\"");
			      for(String part : parts){
			          String tempPart = part;
			          if(part.toLowerCase().contains(".dtd")){
			              dtd = tempPart;
			              out.println("DTD IS:  " + dtd);
			              break;
			          }
			      }

			      // REPLACE DTD WITH PATH IN RSUITE
			      dtdUrl = getShemaUrl(dtd, ".dtd", out);
			      out.println("REPLACING DTD...");
			      newXml = document.replaceFirst(dtd, dtdUrl);
			 
			  } else {
			      out.println("NO DOCTYPE FOUND!");
			      newXml = document;
			  }

			return newXml;
		}

		private String replaceXsdDefinition(String document, java.io.PrintStream out) throws Exception {
			// GET XSD NAME
			String newXml = null;
			xsdSchemaDeclaration = getXsdSchemaDeclaration(document);
			
			if (xsdSchemaDeclaration != null) {
				String xsd = getXsd(xsdSchemaDeclaration);
				out.println("XSD IS:  " + xsd);
				
				String xsdUrl = getShemaUrl(xsd, ".xsd", out);
				xsdNamespaceDeclaration = "xsi:schemaLocation=\"" + xsdUrl + "\"";
				newXml = document.replaceFirst(xsdSchemaDeclaration, xsdNamespaceDeclaration);
			} else {
				out.println("NO SCHEMA FOUND!");
			    newXml = document;
			}

			return newXml;
		}

		private String getXsd(String schemaDeclaration) {
			String xsd = null;
			if (constainsNamaspace(schemaDeclaration)) {
				String xsdNamespace[] = extractXsd(schemaDeclaration, namespaceDeclaration).split("\\/");
				xsd = xsdNamespace[xsdNamespace.length-1].replaceAll("^\\s+|\\s+$", "");
			} else {
				xsd = extractXsd(schemaDeclaration, noNamespaceDeclaration);
			}
			
			return xsd;
		}

		private boolean constainsNamaspace(String schemaDeclaration) {
			String schemaDeclarationType = getSchemaDeclarationType(schemaDeclaration);
			if (schemaDeclarationType.equals(namespaceDeclaration)) {
				return true;
			}
	
			return false;
		}

		private String getSchemaDeclarationType(String schemaDeclaration) {
			String schemaDeclarationType = null;
			if (schemaDeclaration.toLowerCase().indexOf(noNamespaceDeclaration) != -1) {
				schemaDeclarationType = noNamespaceDeclaration;
			} else if (schemaDeclaration.toLowerCase().indexOf(namespaceDeclaration) != -1) {
				schemaDeclarationType = namespaceDeclaration;
			}

			return schemaDeclarationType;
		}

		private String getShemaUrl(String schemaName, String schemaFileExt,  java.io.PrintStream out) throws Exception {
			String schemaUrl = null;
			SchemaInfo[] infos = repository.getSchemaInfos();
		      for(SchemaInfo i : infos){
		          if(i.getFileName().equalsIgnoreCase(schemaName)){
		        	  schemaUrl = repository.getRepositoryUri() +
		                  "/rsuite/schemas/" + i.getSchemaId() + schemaFileExt;
		              out.println("SCHEMA URL IS:  " + schemaUrl);
		              break;
		          }
		      }

			return schemaUrl;
		}

		private String getXsdSchemaDeclaration(String document) {
			String xsdSchemaDeclaration = null;
			if (constainsNamaspace(document)) {
				xsdSchemaDeclaration = "xsi:schemaLocation=\"" + extractXsd(document, namespaceDeclaration) + "\"";
			} else {
				xsdSchemaDeclaration = "xsi:noNamespaceSchemaLocation=\"" + extractXsd(document, noNamespaceDeclaration) + "\"";
			}

			return xsdSchemaDeclaration;
		}

		private String extractXsd(String document, String schemaDeclaration) {
			String xsdReference = null;
			int xsdStartingPoint = document.toLowerCase().indexOf(schemaDeclaration) + schemaDeclaration.length()+2;
			int xsdEndingPoint =  document.toLowerCase().indexOf(".xsd") + ".xsd".length();
			xsdReference = document.substring(xsdStartingPoint, xsdEndingPoint);

			return xsdReference;
		}

		private XMLStructureDefinitionType getXMLStructureDefinitionType(String document) {
			XMLStructureDefinitionType xmlStructureDefinitionType = null;

			if (document.toLowerCase().indexOf("doctype") != -1) {
				xmlStructureDefinitionType = XMLStructureDefinitionType.DTD;
			} else {
				xmlStructureDefinitionType = XMLStructureDefinitionType.XSD;
			}

			return xmlStructureDefinitionType;
		}

		public OutputStream getOutputStream() throws IOException {
  		  	java.io.PrintStream out = openLogFile("errorlog_handler_outputstream.txt");
  		  	
  		  	// need an output stream that is responsible for saving to Rsuite
  		  	OutputStream stream = new RSuiteOutputStream();
  		  
  		  	ClassLoader saved = Thread.currentThread().getContextClassLoader();
			try {
				  Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				  
				  out.println("CREATED RSUITE OUTPUT STREAM.");				  
				  out.println("RETURNING RSUITE OUTPUT STREAM TO OXYGEN...");
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
			
			return stream;
		}
	}
	
	/**
	 * Creates and opens the connection
	 * 
	 * @param u The URL
	 * @return the connection
	 */
	protected URLConnection openConnection(URL u) throws IOException{
		URLConnection connection = new RSuiteConnection(u);
		return connection;
	}
	
	/**
	 * The output stream returned to Oxygen
	 * Oxygen will be calling one of the write methods when saving?
	 * The write method of this stream is responsible for updating content
	 */
	
	private class RSuiteOutputStream extends OutputStream {
		
		private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		public synchronized void write(int b) throws IOException{
			buffer.write((byte)b);
		}
		
		public synchronized void write(byte[] data, int offset, int length){
			buffer.write(data, offset, length);
		}
		
		public synchronized void write(byte[] data){
			try {
				buffer.write(data);
			} 
			catch (IOException e) {
				//e.printStackTrace();
			}
		}
		
		public void close() throws IOException{
  		  	java.io.PrintStream out = openLogFile("errorlog_handler_rsuiteoutputstream.txt");		
			
  		  	ClassLoader saved = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				
				out.println("BUFFER POSITION:  " + buffer.size());				
				out.println("CREATING STRING FROM BUFFER...");
				
				byte[] byteArray = buffer.toByteArray();
				String updatedContent = new String(byteArray, encoding);
				
				out.println("RE-WRITING THE SCHEMA DECLARATION...");
				updatedContent = rewriteSchemaDeclaration(updatedContent);
				out.println("DTD PATH COMPLETE.");
				
				out.println("BYTE ARRAY SIZE:  " + byteArray.length);
				out.println("STRING CREATED.");
				out.print(updatedContent);

				out.println("PERFORMING THE UPDATE TO RSUITE...");	
				repository.update(moId, updatedContent);	
				out.println("UPDATE COMPLETE.");

				// Need to refresh document here
				// We set the request header Location value to the url
				// This causes Oxygen to reload the url after the save
				out.println("SETTING REQUEST HEADER OXYGEN-ACTION...");
				_connection.setRequestProperty("Location", docURL);
				out.println("LOCATION HEADER VARIABLE SET TO:" + docURL);
			} 
			catch (Throwable t) {
				if (out != null) t.printStackTrace(out);
				Frame frame = new Frame();
				JOptionPane.showMessageDialog(frame,
				        "An Error has occurred: "+t.getLocalizedMessage(),
				        "RSuite Error", JOptionPane.ERROR_MESSAGE);
			}
			finally{
				Thread.currentThread().setContextClassLoader(saved);
				if (out != null) {
				    out.flush();
	                out.close();
				}
			}
		}

		private String rewriteSchemaDeclaration(String updatedContent) {
			if (updatedContent.toLowerCase().indexOf("doctype") != -1) {
				updatedContent = updatedContent.replaceAll("(?i)" + dtdUrl, dtd);
			} else {
				updatedContent = updatedContent.replaceAll(xsdNamespaceDeclaration, xsdSchemaDeclaration); 
			}
			return updatedContent;
		}
	}
	
	/**
	 * Try to determine encoding from a given xml document
	 * @param document xml as string
	 * @return encoding
	 */
	protected static String guessXMLEncoding(String document){
		java.io.PrintStream out = openLogFile("errorlog_handler_encoding.txt")	;	
		String encoding = "UTF-8";
		
		ClassLoader saved = Thread.currentThread().getContextClassLoader();
		try {
			out.println("GUESSING ENCODING...");
			String parts[] = document.split("\"");
			for(int i = 0; i < parts.length; i++){
				if(parts[i].equalsIgnoreCase(" encoding=")){
					encoding = parts[i + 1];
					out.println("ENCODING FOUND TO BE:  " + encoding);
					break;
				}
			}
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
		return encoding;
	}
	
	/**
	 * Get the Oxygen Main frame
	 */
	protected static Frame getOxygenFrame() {
		   Frame oxygenFrame = null;
		   Frame[] allFrames = Frame.getFrames();
		   for (int i = 0; i < allFrames.length; i++) {                
		     if ( allFrames[i].getClass().getName().equals("ro.sync.exml.MainFrame")) {
		       oxygenFrame = allFrames[i];
		       break;
		     }
		   }
		   return oxygenFrame;
		}
	
	/**
	 * RSuite Login Prompt 
	 * launched if session is not passed in as parameter
	 */
	public static class RSuiteLoginDialog extends JDialog {
		
		private static final long serialVersionUID = 1L;

		public RSuiteLoginDialog(){
			super();
			setModal(true);
			setTitle("RSuite Login");
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			GridBagLayout layout = new GridBagLayout();
			getContentPane().setLayout(layout);
			  
			// Set up components
			final JTextField loginTextField = new JTextField((username == null) ? "" : username, 20);
			final JPasswordField passwordField = new JPasswordField("", 20);
			JButton loginButton = new JButton("Login");
			JButton cancelButton = new JButton("Cancel");
			JLabel loginLabel = new JLabel("Username:  ");
			JLabel passwordLabel = new JLabel("Password:  ");
			  
			// Set up constraints
			GridBagConstraints c = new GridBagConstraints();
			c.insets.top = 5;
			c.insets.bottom = 5;
			JPanel pane = new JPanel(layout);
			pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
			c.anchor = GridBagConstraints.EAST;
			layout.setConstraints(loginLabel, c);
			pane.add(loginLabel);
			
			layout.setConstraints(loginTextField, c);
			pane.add(loginTextField);
			
			c.gridy = 1;
			layout.setConstraints(passwordLabel, c);
			pane.add(passwordLabel);
			
			layout.setConstraints(passwordField, c);
			pane.add(passwordField);
			
			c.gridy = 2;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.CENTER;
			JPanel panel = new JPanel();
			panel.add(loginButton);
			panel.add(cancelButton);
			layout.setConstraints(panel, c);
			pane.add(panel);
			
			// add to pane
			getContentPane().add(pane);
			  
			// adjust size
			pack();
			  
			// Add action listener for the login button
			loginButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String user = loginTextField.getText();
					username = user;
					String pass = new String(passwordField.getPassword());
					password = pass;
			
					java.io.PrintStream out = openLogFile("errorlog_handler_logindialog.txt");
					ClassLoader saved = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
						
						out.println("USER:  " + username);
						out.println("PASS:  " + password);
						  
						out.println("INITIALIZING REPOSITORY...");
						repository = new RsuiteRepositoryImpl(username, password, host);
						out.println("REPOSITORY INITIALIZED.");
						  
						out.println("ATTEMPTING TO LOGIN...");
						repository.login();
						out.println("LOGIN SUCCESSFULL.");
						  
						out.println("SESSION KEY IS:  " +  repository.getSessionKey());
						sessionKey = repository.getSessionKey();
						
						Frame frame = new Frame();
						JOptionPane.showMessageDialog(frame, "RSuite Check In Complete", "Success", JOptionPane.INFORMATION_MESSAGE);
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
					  
					setVisible(false);
					dispose();
				}
			});
			
			  // Add action listener for the cancel button
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
	}
	
	private static PrintStream openLogFile(
	        String name
	) {
	    return OxyUtils.openLogFile(name);
	}
}
