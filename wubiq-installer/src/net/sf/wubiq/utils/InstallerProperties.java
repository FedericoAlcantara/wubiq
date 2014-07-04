/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.util.Properties;

import net.sf.wubiq.common.InstallerKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects the value from wubiq-client.properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public class InstallerProperties extends BaseProperties {
	private static final Log LOG = LogFactory.getLog(InstallerProperties.class);
	private static Properties properties;
	
	protected InstallerProperties() {
	}
	/**
	 * Reads property file.
	 * @return Properties according to the file contents.
	 */
	protected static Properties getProperties() {
		return getProperties(false);
	}
	
	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to the file contents.
	 */
	public static Properties getProperties(boolean reload) {
		if (properties == null || reload) {
			properties = BaseProperties.getProperties();
		}
		return properties;
	}
	
	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to file contents.
	 */
	public static File getPropertiesFile() {
		return BaseProperties.getPropertiesFile(InstallerKeys.INSTALLER_PROPERTIES_FILE_NAME);
	}
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected static void showPropertiesFileFound(){
		LOG.info(InstallerBundle.getMessage("info.properties_found_file"));
	}
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected static void showPropertiesFileNotFound(){
		LOG.info(InstallerBundle.getMessage("info.no_properties_found_file"));
	}

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesFound(){
		LOG.info(InstallerBundle.getMessage("info.properties_found"));
	}
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesNotFound(){
		LOG.info(InstallerBundle.getMessage("info.no_properties_found"));
	}

}
