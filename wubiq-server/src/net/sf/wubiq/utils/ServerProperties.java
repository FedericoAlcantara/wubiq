/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects the value from wubiq-server.properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public class ServerProperties {
	private static final Log LOG = LogFactory.getLog(ServerProperties.class);
	private static Properties properties;
	
	private ServerProperties() {
	}

	public static String getHsqldbHost() {
		return get("host", "hsql://localhost");
	}
	
	public static String getHsqldbPort() {
		return get("hsqldb.port", "");
	}
	
	public static String getHsqldbDatabaseName() {
		return get("hsqldb.database", "wubiq_data");
	}
	
	public static String getHsqldbDbName() {
		return get("hsqldb.dbname", "wubiq");
	}
	
	public static String getPrintJobManager() {
		return get("manager", "net.sf.wubiq.print.managers.impl.HsqldbPrintJobManager");
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
				InputStream stream = null;
				try {
					stream = new FileInputStream("./wubiq-server.properties");
					LOG.info(ServerLabels.get("server.info_server_properties_found_file"));
				} catch (FileNotFoundException e) {
					LOG.info(ServerLabels.get("server.info_no_server_properties_found_file"));
				}
				if (stream == null) {
					stream = Class.class.getResourceAsStream("/wubiq-server.properties");
					if (stream == null) {
						throw new IOException("null");
					}
					LOG.info(ServerLabels.get("server.info_server_properties_found"));
				}
				properties.load(stream);
			} catch (IOException e) {
				LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
			}
		}
		return properties;
	}
	
}
