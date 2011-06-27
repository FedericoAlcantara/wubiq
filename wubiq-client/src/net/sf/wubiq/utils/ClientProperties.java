/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class ClientProperties {
	private static final Log LOG = LogFactory.getLog(ClientProperties.class);
	private static Properties properties;
	
	private ClientProperties() {
	}

	public static String getHost() {
		return get("host", "http://localhost");
	}
	
	public static String getPort() {
		return get("port", "8080");
	}
	
	public static String getApplicationName() {
		return get("application", "wubiq");
	}
	
	public static String getServletName() {
		return get("servlet", "wubiq.do");
	}
	
	public static String getUuid() {
		return get("uuid", UUID.randomUUID().toString());
	}
	
	private static String get(String key, String defaultValue) {
		String returnValue = getProperties().getProperty(key);
		if (returnValue == null) {
			returnValue = defaultValue;
		}
		return returnValue;
	}

	/**
	 * @return the properties
	 */
	private static Properties getProperties() {
		if (properties == null) {
			try {
				properties = new Properties();
				InputStream stream = Class.class.getResourceAsStream("/wubiq-client.properties");
				if (stream == null) {
					throw new IOException("null");
				}
				properties.load(stream);
			} catch (IOException e) {
				LOG.info(ClientLabels.get("client.info_no_client_properties_found"));
			}
		}
		return properties;
	}
	
}
