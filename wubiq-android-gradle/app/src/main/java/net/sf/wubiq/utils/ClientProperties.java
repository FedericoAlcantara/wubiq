/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.util.Properties;

import net.sf.wubiq.common.ConfigurationKeys;

import java.util.logging.Logger;

/**
 * Collects the value from wubiq-client.properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public class ClientProperties extends BaseProperties {
	private static final Logger LOG = Logger.getLogger(ClientProperties.class.getName());
	private static Properties properties;
	
	public static final ClientProperties INSTANCE = new ClientProperties();

	protected ClientProperties(){
		
	}
	
	protected Properties getProperties() {
		if (properties == null) {
			properties = super.getProperties();
		}
		return properties;
	}
	
	/**
	 * Must produce a file where the properties reside. Should return null if not found.
	 * @return Found file or null.
	 */
	protected File getPropertiesFile() {
		return super.getPropertiesFile(ConfigurationKeys.CLIENT_PROPERTIES_FILE_NAME);
	}
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected void showPropertiesFileFound(){
		LOG.info(String.valueOf(ClientLabels.get("client.info_client_properties_found_file")));
	}
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected void showPropertiesFileNotFound(){
		LOG.info(String.valueOf(ClientLabels.get("client.info_no_client_properties_found_file")));
	}

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected void showPropertiesFound(){
		LOG.info(String.valueOf(ClientLabels.get("client.info_client_properties_found")));
	}
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected void showPropertiesNotFound(){
		LOG.info(String.valueOf(ClientLabels.get("client.info_no_client_properties_found")));
	}	
}
