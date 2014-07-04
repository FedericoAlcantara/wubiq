/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.util.Properties;

import net.sf.wubiq.common.ConfigurationKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects the value from wubiq-client.properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public class ClientProperties extends BaseProperties {
	private static final Log LOG = LogFactory.getLog(ClientProperties.class);
	private static Properties properties;
	
	protected ClientProperties() {
	}
	
	protected static Properties getProperties() {
		if (properties == null) {
			properties = BaseProperties.getProperties();
		}
		return properties;
	}
	
	/**
	 * Must produce a file where the properties reside. Should return null if not found.
	 * @return Found file or null.
	 */
	protected static File getPropertiesFile() {
		return BaseProperties.getPropertiesFile(ConfigurationKeys.CLIENT_PROPERTIES_FILE_NAME);
	}
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected static void showPropertiesFileFound(){
		LOG.info(ClientLabels.get("client.info_client_properties_found_file"));
	}
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected static void showPropertiesFileNotFound(){
		LOG.info(ClientLabels.get("client.info_no_client_properties_found_file"));
	}

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesFound(){
		LOG.info(ClientLabels.get("client.info_client_properties_found"));
	}
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesNotFound(){
		LOG.info(ClientLabels.get("client.info_no_client_properties_found"));
	}	
}
