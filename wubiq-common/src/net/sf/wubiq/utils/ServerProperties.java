/**
 * 
 */
package net.sf.wubiq.utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.wubiq.common.WebKeys;

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
		return get("host", "file:");
	}
	
	public static String getHsqldbPort() {
		return get("hsqldb.port", "");
	}
	
	public static String getHsqldbDatabaseName() {
		return get("hsqldb.database", WebKeys.DEFAULT_HSQLDB_DATABASE_NAME);
	}
	
	public static String getHsqldbDbName() {
		return get("hsqldb.dbname", WebKeys.DEFAULT_HSQLDB_DB_ALIAS);
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
				ResourceBundle bundle = ResourceBundle.getBundle("./" + WebKeys.SERVER_PROPERTIES_FILE_NAME, new Locale(""));
				if (bundle != null) {
					LOG.info(ServerLabels.get("server.info_server_properties_found"));
					for (String key : bundle.keySet()) {
						String value = bundle.getString(key);
						properties.put(key, value);
					}
				} else {
					LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
				}
			} catch (MissingResourceException e) {
				LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
			}
		}
		return properties;
	}
	
}
