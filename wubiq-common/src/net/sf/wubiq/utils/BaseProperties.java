/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import net.sf.wubiq.common.ConfigurationKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects the value from properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public abstract class BaseProperties {
	private static final Log LOG = LogFactory.getLog(BaseProperties.class);
	
	protected BaseProperties() {
	}
	
	public static String getApplicationName() {
		return get(ConfigurationKeys.PROPERTY_APPLICATION_NAME, ConfigurationKeys.DEFAULT_APPLICATION_NAME).trim();
	}
	
	public static String getServletName() {
		return get(ConfigurationKeys.PROPERTY_SERVLET_NAME, ConfigurationKeys.DEFAULT_SERVLET_NAME).trim();
	}
	
	public static String getUuid() {
		return get(ConfigurationKeys.PROPERTY_UUID, UUID.randomUUID().toString()).trim();
	}

	public static String getGroups() {
		return get(ConfigurationKeys.PROPERTY_GROUPS, "").trim();
	}
	
	public static String getClientParameters() {
		return get(ConfigurationKeys.PROPERTY_CLIENT_PARAMETERS, "");
	}
	
	/**
	 * @deprecated Use getConnections instead.
	 * @return Current host.
	 */
	public static String getHost() {
		return get(ConfigurationKeys.PROPERTY_HOST, "").trim();
	}
	
	/**
	 * @deprecated Use getConnections instead.
	 * @return Current port.
	 */
	public static Integer getPort() {
		int returnValue = ConfigurationKeys.DEFAULT_PORT_ADDRESS;
		String portAddressString = get(ConfigurationKeys.PROPERTY_PORT, "");
		try {
			returnValue = Integer.parseInt(portAddressString);
		} catch (NumberFormatException e) {
			returnValue = ConfigurationKeys.DEFAULT_PORT_ADDRESS;
		}
		return returnValue;
	}
	
	/**
	 * Gets the installer port address.
	 * @return The installer port address read or the default one.
	 */
	public static Integer getInstallerPortAddress() {
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
	
	
	
	/**
	 * Reads the possible connections from the client properties. This should be the preferred method.
	 * @return A comma separated list of connections.
	 */
	public static String getConnections() {
		StringBuffer returnValue = new StringBuffer("");
		String host = getHost();
		String port = getPort().toString();
		if (!Is.emptyString(host)) {
			returnValue.append(host);
			if (!Is.emptyString(port)) {
				returnValue.append(':')
					.append(port);
			}
		}
		String connectionsString = get(ConfigurationKeys.PROPERTY_CONNECTIONS, "").trim();
		if (!Is.emptyString(connectionsString)) {
			String [] connections = connectionsString.split("[,;]");
			for (String connection : connections) {
				if (!Is.emptyString(connection.trim())) {
					if (returnValue.length() > 0) {
						returnValue.append(',');
					}
					returnValue.append(connection.trim());
				}
			}
		}
		return returnValue.toString();
	}
	
	/**
	 * Gets a value from properties file.
	 * @param key Key to search for.
	 * @param defaultValue Value to return in case key is not found in properties.
	 * @return Found value or default value.
	 */
	protected static String get(String key, String defaultValue) {
		String returnValue = getProperties().getProperty(key);
		if (returnValue == null) {
			returnValue = defaultValue;
		}
		return returnValue;
	}

	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to the file contents.
	 */
	protected static Properties getProperties() {
		Properties properties = new Properties();
		File propertyFile = getPropertiesFile();
		if (propertyFile != null) {
			InputStream stream = null;
			try {
				stream = new FileInputStream(propertyFile);
				properties.load(stream);
			} catch (FileNotFoundException e) {
				showPropertiesNotFound();
			} catch (IOException e) {
				showPropertiesNotFound();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch(IOException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		}
		return properties;
	}
	
	/**
	 * Reads property file.
	 * @param reload If true reloads the file.
	 * @return Properties according to file contents.
	 */
	protected static File getPropertiesFile(String filePrefix) {
		File propertyFile = null;
		propertyFile = new File("./" + filePrefix +".properties");
		if (propertyFile.exists() && propertyFile.isDirectory()) {
			showPropertiesFileFound();
		} else {
			propertyFile = null;
			showPropertiesFileNotFound();
			URL resource = Class.class.getResource("/" + filePrefix + ".properties");
			if (resource != null) {
				showPropertiesFound();
				propertyFile = new File(resource.getPath());
			}
		}
		return propertyFile;
	}
	
	/**
	 * Must produce a file where the properties reside. Should return null if not found.
	 * @return Found file or null.
	 */
	protected static File getPropertiesFile() {
		throw new UnsupportedOperationException("getPropertiesFile");
	}
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected static void showPropertiesFileFound(){
		throw new UnsupportedOperationException("showPropertiesFileFound");
	}
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected static void showPropertiesFileNotFound(){
		throw new UnsupportedOperationException("showPropertiesFileNotFound");
	}

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesFound(){
		throw new UnsupportedOperationException("showPropertiesFound");
	}
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected static void showPropertiesNotFound(){
		throw new UnsupportedOperationException("showPropertiesNotFound");
	}
}
