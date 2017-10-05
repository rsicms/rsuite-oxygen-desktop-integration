package orbis.jnlp;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.remoteapi.CallArgument;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.DefaultRemoteApiHandler;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.XmlRemoteApiResult;

public class OxygenJnlpGenerator extends DefaultRemoteApiHandler  {
	private static final Log log = LogFactory.getLog(OxygenJnlpGenerator.class);
	@Override
	public RemoteApiResult execute(RemoteApiExecutionContext ctx, CallArgumentList args) throws RSuiteException {
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			String host = args.getFirstString("@_HTTP_HEADER_HOST");
			String pluginId = getRemoteApiDefinition().getPlugin().getId();
			String moId = args.getFirstString("id");
			if (moId == null) {
				throw new RSuiteException("Please pass a ManagedObject ID as ?id");
			}
			doc.appendChild(buidJnlpNode(doc, 
				host,
				pluginId, 
				ctx.getSession().getUser().getUserId(), 
				ctx.getSession().getKey(), 
				moId
			));
			XmlRemoteApiResult result = new XmlRemoteApiResult(doc);
			result.setContentType("application/application/x-java-jnlp-file");
			result.setSuggestedFileName("rsuite-oxygen-desktop-integration-" + moId + ".jnlp");
			return result;
		} catch (ParserConfigurationException e) {
			throw new RSuiteException(RSuiteException.ERROR_CONFIGURATION_PROBLEM, "Couldn't create JNLP document", e);
		}
	}

	private static Element buidJnlpNode(Document doc, String host, String pluginId, String userId, String sessionKey, String moId) {
		// Server-level TODO: no way from a RemoteApiHandler to tell if connection neeeds to be secure.
		String root = "http://" + host + "/rsuite/rest/v1";
		Element jnlp = doc.createElement("jnlp");
		jnlp.setAttribute("spec", "6.0+");
		jnlp.setAttribute("codebase", root + "/api/" + pluginId + "/");
		jnlp.appendChild(buildInformationNode(doc,
			"Oxygen Desktop launcher",
			"RSI Content Solutions",
			"Oxygen Desktop launcher for " + moId
		));
		jnlp.appendChild(buildSecurityNode(doc));
		jnlp.appendChild(buildResourcesNode(doc, root + "/static/" + pluginId + "/SignedOxygenLauncher.jar"));
		jnlp.appendChild(buildApplicationDescNode(doc, 
			"OxygenLauncherApplet", 
			"http://" + host,
			userId,
			sessionKey,
			moId
		));
		return jnlp;
	}
	private static Element buildArgumentNode(Document doc, String content) {
		return buildTextNode(doc, "argument", content);
	}

	private static Element buildApplicationDescNode(Document doc, String mainClass, String...args) {
		Element appDesc = doc.createElement("application-desc");
		appDesc.setAttribute("main-class", mainClass);
		for (String arg : args) {
			appDesc.appendChild(buildArgumentNode(doc, arg));
		}
		return appDesc;
	}

	private static Element buildResourcesNode(Document doc, String jarPath) {
		Element res = doc.createElement("resources");
		Element jar = doc.createElement("jar");
		jar.setAttribute("href",  jarPath);
		res.appendChild(jar);
		return res;
	}

	private static Element buildSecurityNode(Document doc) {
		Element sec = doc.createElement("security");
		Element perms = doc.createElement("all-permissions");
		sec.appendChild(perms);
		return sec;
	}
	private static Element buildTextNode(Document doc, String nodeName, String content) {
		Element node = doc.createElement(nodeName);
		node.setTextContent(content);
		return node;
	}
	private static Element buildInformationNode(Document doc, String title, String vendor, String description) {
		Element info = doc.createElement("information");
		info.appendChild(buildTextNode(doc, "title", title));
		info.appendChild(buildTextNode(doc, "vendor", vendor));
		info.appendChild(buildTextNode(doc, "description", description));
		return info;
	}
	

}
