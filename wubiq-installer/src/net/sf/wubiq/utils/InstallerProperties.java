/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.util.Properties;

import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.InstallerKeys;
import net.sf.wubiq.common.PropertyKeys;

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
	
	public static final InstallerProperties INSTANCE = new InstallerProperties();
	
	private InstallerProperties(){
	}
	
	/**
	 * Comma separated list of photo printers identifiers.
	 * @return String containing the list.
	 */
	public String getPhotoPrinters() {
		return get(PropertyKeys.WUBIQ_PRINTERS_PHOTO, "");
	}
	
	/**
	 * Comma separated list of dot matrix printers identifiers.
	 * @return String containing the list.
	 */
	public String getDmPrinters() {
		return get(PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX, "");
	}

	/**
	 * Comma separated list of high quality dot matrix printers identifiers.
	 * @return String containing the list.
	 */
	public String getDmHqPrinters() {
		return get(PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ, "");
	}
	
	/**
	 * Font to use for dot matrix printing.
	 * @return String containing the font name.
	 */
	public String getDefaultDmFont() {
		return get(PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT, "");
	}
	
	/**
	 * If true fonts will be defaulted to java's logical font in dot matrix printing.
	 * @return Property value blank, true or false.
	 */
	public String getForceLogicalFontOnDm() {
		return get(PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL, "");
	}

	/**
	 * Gets the parameters to apply to the jvm.
	 * @return String containing jvm parameters.
	 */
	public String getJvmParameters() {
		return get(ConfigurationKeys.PROPERTY_JVM_PARAMETERS, "");
	}
	
	/**
	 * Gets the installer port address.
	 * @return The installer port address read or the default one.
	 */
	public Integer getInstallerPortAddress() {
		int returnValue = ConfigurationKeys.DEFAULT_INSTALLER_PORT_ADDRESS;
		String portAddressString = get(ConfigurationKeys.PROPERTY_INSTALLER_PORT_ADDRESS, 
				"");
		try {
			returnValue = Integer.parseInt(portAddressString);
		} catch (NumberFormatException e) {
			returnValue = ConfigurationKeys.DEFAULT_INSTALLER_PORT_ADDRESS;
		}
		return returnValue;
	}
	
	public String getClientParameters() {
		StringBuffer returnValue = new StringBuffer("");
		addParameter(returnValue, "connections", ConfigurationKeys.PROPERTY_CONNECTIONS);
		addParameter(returnValue, "app", ConfigurationKeys.PROPERTY_APPLICATION_NAME);
		addParameter(returnValue, "servlet", ConfigurationKeys.PROPERTY_SERVLET_NAME);
		if (returnValue.length() > 0) {
			returnValue.append(' ');
		}
		returnValue
				.append("--uuid ")
				.append('"')
				.append(getUuid())
				.append('"');
		addParameter(returnValue, "groups", ConfigurationKeys.PROPERTY_GROUPS);
		if (isDebugMode()) {
			if (returnValue.length() > 0) {
				returnValue.append(' ');
			}
			returnValue.append("--verbose");
			addParameter(returnValue, "logLevel", ConfigurationKeys.PROPERTY_DEBUG_LOG_LEVEL);
		}
		addParameter(returnValue, "interval", ConfigurationKeys.PROPERTY_POLL_INTERVAL);
		addParameter(returnValue, "wait", ConfigurationKeys.PROPERTY_PRINT_JOB_WAIT);
		
		return returnValue.toString();
	}

	/**
	 * Adds a client parameter. If the value from property name is blank or null, parameter is not added.
	 * @param buffer Buffer containing the string of parameters.
	 * @param parameterName Parameter name.
	 * @param propertyName Name of the property in the properties file.
	 */
	private void addParameter(StringBuffer buffer, String parameterName, String propertyName) {
		String value = get(propertyName, "");
		if (!Is.emptyString(value)) {
			if (buffer.length() > 0) {
				buffer.append(' ');
			}
			buffer.append("--")
				.append(parameterName)
				.append('"')
				.append(value)
				.append('"');
		}
	}
	

	/**
	 * Reads property file.
	 * @return Properties according to the file contents.
	 */
	protected Properties getProperties() {
		return getProperties(false);
	}
	
	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to the file contents.
	 */
	public Properties getProperties(boolean reload) {
		if (properties == null || reload) {
			properties = super.getProperties();
		}
		return properties;
	}
	
	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to file contents.
	 */
	public File getPropertiesFile() {
		return super.getPropertiesFile(InstallerKeys.INSTALLER_PROPERTIES_FILE_NAME);
	}
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected void showPropertiesFileFound(){
		LOG.info(InstallerBundle.getMessage("info.properties_found_file"));
	}
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected void showPropertiesFileNotFound(){
		LOG.info(InstallerBundle.getMessage("info.no_properties_found_file"));
	}

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected void showPropertiesFound(){
		LOG.info(InstallerBundle.getMessage("info.properties_found"));
	}
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected void showPropertiesNotFound(){
		LOG.info(InstallerBundle.getMessage("info.no_properties_found"));
	}

}
