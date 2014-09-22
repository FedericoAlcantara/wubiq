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
	
	public String getApplicationName() {
		return get(ConfigurationKeys.PROPERTY_APPLICATION_NAME, ConfigurationKeys.DEFAULT_APPLICATION_NAME).trim();
	}
	
	public String getServletName() {
		return get(ConfigurationKeys.PROPERTY_SERVLET_NAME, ConfigurationKeys.DEFAULT_SERVLET_NAME).trim();
	}
	
	public String getUuid() {
		return get(ConfigurationKeys.PROPERTY_UUID, UUID.randomUUID().toString()).trim();
	}

	public String getGroups() {
		return get(ConfigurationKeys.PROPERTY_GROUPS, "").trim();
	}
	
	/**
	 * @deprecated Use getConnections instead.
	 * @return Current host.
	 */
	public String getHost() {
		return get(ConfigurationKeys.PROPERTY_HOST, "").trim();
	}
	
	/**
	 * @deprecated Use getConnections instead.
	 * @return Current port.
	 */
	public Integer getPort() {
		return getInt(ConfigurationKeys.PROPERTY_PORT, ConfigurationKeys.DEFAULT_PORT_ADDRESS);
	}
	
	/**
	 * A value indicating the number of times to keep trying to connect before
	 * aborting client.
	 * @return Connection retries count.
	 */
	public Integer getConnectionRetries() {
		return getInt(ConfigurationKeys.PROPERTY_CONNECTION_RETRIES, ConfigurationKeys.DEFAULT_CONNECTION_RETRIES);
	}
		
	/**
	 * Reads the possible connections from the client properties. This should be the preferred method.
	 * @return A comma separated list of connections.
	 */
	public String getConnections() {
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
	 * If true the debug mode is enabled (verbosed).
	 * @return Debug mode.
	 */
	public boolean isDebugMode() {
		return "true".equalsIgnoreCase(get(ConfigurationKeys.PROPERTY_DEBUG_ENABLED, "false"));
	}
	
	/**
	 * The level to use for log output.
	 * @return Integer value between 0 - 5.
	 */
	public Integer getDebugLogLevel() {
		return getInt(ConfigurationKeys.PROPERTY_DEBUG_LOG_LEVEL, ConfigurationKeys.DEFAULT_LOG_LEVEL);
	}
	
	/**
	 * Gets the interval for polling the server about pending jobs.
	 * @return Integer with the value. Never null.
	 */
	public Integer getPollInterval() {
		return getInt(ConfigurationKeys.PROPERTY_POLL_INTERVAL, ConfigurationKeys.DEFAULT_POLL_INTERVAL);
	}
	
	/**
	 * Gets the print job wait value. This is the time (in milliseconds) to wait after a successful print job handling.
	 * @return Integer with the value. Never null.
	 */
	public Integer getPrintJobWait() {
		return getInt(ConfigurationKeys.PROPERTY_PRINT_JOB_WAIT, ConfigurationKeys.DEFAULT_PRINT_JOB_WAIT);
	}
	
	/**
	 * Gets an integer value out of a property.
	 * @param key Key for the property. 
	 * @param defaultValue Default value to use.
	 * @return Property value as string.
	 */
	public Integer getInt(String key, Integer defaultValue) {
		Integer returnValue = defaultValue;
		try {
			returnValue = Integer.parseInt(get(key, ""));
		} catch (NumberFormatException e) {
			returnValue = defaultValue;
		}
		return returnValue;
	}
	
	/**
	 * Gets a value from properties file.
	 * @param key Key to search for.
	 * @param defaultValue Value to return in case key is not found in properties.
	 * @return Found value or default value.
	 */
	protected String get(String key, String defaultValue) {
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
	protected Properties getProperties() {
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
	protected File getPropertiesFile(String filePrefix) {
		File propertyFile = null;
		propertyFile = new File("./" + filePrefix +".properties");
		if (propertyFile.exists() && !propertyFile.isDirectory()) {
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
	protected abstract File getPropertiesFile();
	
	/**
	 * Shows a message indicating that a properties file was found.
	 */
	protected abstract void showPropertiesFileFound();
	
	/**
	 * Shows a message indicating that the properties file was not found.
	 */
	protected abstract void showPropertiesFileNotFound();

	/**
	 * Shows a message indicating that a properties element was found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected abstract void showPropertiesFound();
	
	/**
	 * Shows a message indicating that a properties element was not found.
	 * This might be a different message, since the properties file might be inside a jar object.
	 */
	protected abstract void showPropertiesNotFound();
}
