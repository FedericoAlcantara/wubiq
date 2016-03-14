/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.wubiq.common.ConfigurationKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects the value from wubiq-server.properties.
 * This file search is as followed. It is first searched in the folder in the system file where the client is run.
 * If not found then it is look up in the default paths of java class paths.
 * @author Federico Alcantara
 *
 */
public class ServerProperties extends BaseProperties {
	private static final Log LOG = LogFactory.getLog(ServerProperties.class);
	private static Properties properties;
	private static Map<String, String> users;
	private static String realPath;
	
	public static final ServerProperties INSTANCE = new ServerProperties();
	
	private ServerProperties(){
	}
	
	public String getHsqldbHost() {
		return INSTANCE.get(ConfigurationKeys.PROPERTY_HSQLDB_HOST, 
				ConfigurationKeys.DEFAULT_HSQLDB_HOST);
	}
	
	public String getHsqldbPort() {
		return get(ConfigurationKeys.PROPERTY_HSQLDB_PORT, "");
	}
	
	public String getHsqldbDatabaseName() {
		return get(ConfigurationKeys.PROPERTY_HSQLDB_DATABASE_NAME, ConfigurationKeys.DEFAULT_HSQLDB_DATABASE_NAME);
	}
	
	public String getHsqldbDbName() {
		return get(ConfigurationKeys.PROPERTY_HSQLDB_DB_ALIAS, ConfigurationKeys.DEFAULT_HSQLDB_DB_ALIAS);
	}
	
	public String getPrintJobManager() {
		return get(ConfigurationKeys.PROPERTY_PRINT_JOB_MANAGER, ConfigurationKeys.DEFAULT_PRINT_JOB_MANAGER);
	}

	public String getRemotePrintJobManager() {
		return get(ConfigurationKeys.PROPERTY_REMOTE_PRINT_JOB_MANAGER, ConfigurationKeys.DEFAULT_REMOTE_PRINT_JOB_MANAGER);
	}
	
	/**
	 * Gets the list of users with their privileges. The list is gather from a comma separated
	 * list of elements in the form userid:password.
	 * @return List of users.
	 */
	public Map<String, String> getUsers() {
		users = new HashMap<String, String>();
		properties = null;
		String usersList = get(ConfigurationKeys.PROPERTY_USERS, "");
		for (String userPassword : usersList.split("[,;]")) {
			if (userPassword.contains(":")) {
				String[] data = userPassword.split(":");
				String userId = ServerUtils.INSTANCE.normalizedUserId(data[0]);
				if (!Is.emptyString(userId)) {
					users.put(userId, ServerUtils.INSTANCE.normalizedPassword(data[1]));
				}
			}
		}
		return users;
	}
	
	/**
	 * True if the user / password combination is found in the map of users.
	 * @param userId Id of the user to search for.
	 * @param password Password.
	 * @return True if the user/password combination.
	 */
	public boolean isValidUser(String userId, String password) {
		return getUsers().containsKey(userId) &&
				getUsers().get(userId).equals(ServerUtils.INSTANCE.normalizedPassword(password));
	}

	/**
	 * It lookup for wubiq-server.properties file.
	 * First it searches in ../tomcat/webapps/wubiq-server/WEB-INF/classes. Then searches in:<br/>
	 * ../tomcat/webapps/wubiq-server/WEB-INF/, ../tomcat/webapps/wubiq-server/WEB-INF/conf<br/>
	 * ../tomcat/webapps/wubiq-server/, ../tomcat/webapps/wubiq-server/conf<br/>
	 * ../tomcat/webapps/, ../tomcat/webapps/conf<br/>
	 * ../tomcat/, ../tomcat/conf<br/>
	 * ../, ../conf<br/>
	 * 
	 * @return the properties.
	 */
	protected Properties getProperties() {
		if (properties == null) {
			try {
				properties = new Properties();
				
				File propertyFile = new File(getRealPath() + "/WEB-INF/classes/" + ConfigurationKeys.SERVER_PROPERTIES_FILE_NAME + ".properties");
				if (!propertyFile.exists()) {
					propertyFile = propertyFile.getParentFile();
					if (propertyFile != null) {
						for (int i = 0; i < 8; i++) {
							propertyFile = propertyFile.getParentFile();
							if (propertyFile == null) {
								break;
							}
							File testFile = new File(propertyFile.getParent() + "/" + ConfigurationKeys.SERVER_PROPERTIES_FILE_NAME + ".properties");
							if (testFile.exists()) {
								propertyFile = testFile;
								break;
							} else {
								testFile = new File(propertyFile.getParent() + "/conf/" + ConfigurationKeys.SERVER_PROPERTIES_FILE_NAME + ".properties");
								if (testFile.exists()) {
									propertyFile = testFile;
									break;
								}		
							}
						}
					}
				}
				FileInputStream inputStream = null;
				try {
					if (propertyFile != null && propertyFile.exists()) {
						inputStream = new FileInputStream(propertyFile);
						properties.load(inputStream);
						LOG.info(ServerLabels.get("server.info_server_properties_found"));
					} else {
						LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
					}
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							LOG.debug("Exception possible:" + e);
						}
					}
				}
			} catch (FileNotFoundException e) {
				LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
			} catch (IOException e) {
				LOG.info(ServerLabels.get("server.info_no_server_properties_found"));
			}
		}
		return properties;
	}

	public String getRealPath() {
		return realPath;
	}

	public void setRealPath(String realPath) {
		ServerProperties.realPath = realPath;
	}

	@Override
	protected File getPropertiesFile() {
		return null;
	}

	@Override
	protected void showPropertiesFileFound() {
	}

	@Override
	protected void showPropertiesFileNotFound() {
	}

	@Override
	protected void showPropertiesFound() {
	}

	@Override
	protected void showPropertiesNotFound() {
	}
	
}
