/**
 * 
 */
package net.sf.wubiq.common;

/**
 * @author Federico Alcantara
 *
 */
public final class ConfigurationKeys {
	private ConfigurationKeys() {
	}
	public static final String CLIENT_PROPERTIES_FILE_NAME = "wubiq-client";
	public static final String SERVER_PROPERTIES_FILE_NAME = "wubiq-server";
	public static final String INSTALLER_PROPERTIES_FILE_NAME = "wubiq-installer";
	
	public static final Integer DEFAULT_PORT_ADDRESS = 8080;
	public static final Integer DEFAULT_INSTALLER_PORT_ADDRESS = 51000;
	
	public static final String DEFAULT_APPLICATION_NAME = "wubiq-server";
	public static final String DEFAULT_SERVLET_NAME = "wubiq.do";
	public static final String DEFAULT_HSQLDB_DATABASE_NAME = "wubiq-data";
	public static final String DEFAULT_HSQLDB_DB_ALIAS = "wubiq";
	public static final String DEFAULT_HSQLDB_HOST = "file:";
	public static final Integer DEFAULT_CONNECTION_RETRIES = -1;
	public static final Integer DEFAULT_LOG_LEVEL = 0;
	public static final Integer DEFAULT_POLL_INTERVAL = 300; // Default interval for checking for pending jobs. 300 ms.
	public static final Integer DEFAULT_PRINT_JOB_WAIT = 500; // Pause between print jobs, default 1/2 a second.
	public static final Integer DEFAULT_PDF_TO_IMAGE_DOTS_PER_INCH = 144; // Dots per inch.
	
	/**
	 * @since version 2.2 HsqlDB Job manager is deprecated in favor of DirectConnectPrintJobManager for all cases.
	 */
	public static final String DEFAULT_PRINT_JOB_MANAGER = "net.sf.wubiq.print.managers.impl.DirectConnectPrintJobManager";
	public static final String DEFAULT_REMOTE_PRINT_JOB_MANAGER = "net.sf.wubiq.print.managers.impl.DirectConnectPrintJobManager";

	public static final String DEFAULT_JDBC_USERNAME = "wubiq";
	public static final String DEFAULT_JDBC_PASSWORD = "wubiq2011";
	
	public static final String PROPERTY_APPLICATION_NAME = "application";
	public static final String PROPERTY_SERVLET_NAME = "servlet";
	public static final String PROPERTY_UUID = "uuid";
	public static final String PROPERTY_GROUPS = "groups";
	public static final String PROPERTY_JVM_PARAMETERS = "jvm.parameters";
	public static final String PROPERTY_HOST = "host";
	public static final String PROPERTY_PORT = "port";
	public static final String PROPERTY_CONNECTIONS = "connections";
	public static final String PROPERTY_CONNECTION_RETRIES = "connection.retries";
	public static final String PROPERTY_DEBUG_ENABLED = "debug.enabled";
	public static final String PROPERTY_DEBUG_LOG_LEVEL = "debug.log_level";
	public static final String PROPERTY_DEFAULT_PRINTER = "printer";
	
	public static final String PROPERTY_HSQLDB_HOST = "hsqldb.host";
	public static final String PROPERTY_HSQLDB_PORT = "hsqldb.port";
	public static final String PROPERTY_HSQLDB_DATABASE_NAME = "hsqldb.database";
	public static final String PROPERTY_HSQLDB_DB_ALIAS = "hsqldb.dbname";
	
	public static final String PROPERTY_PRINT_JOB_MANAGER = "manager";
	public static final String PROPERTY_REMOTE_PRINT_JOB_MANAGER = "remote.manager";
	public static final String PROPERTY_POLL_INTERVAL = "interval";
	public static final String PROPERTY_PRINT_JOB_WAIT = "wait";
	
	public static final String PROPERTY_USERS = "users";
	
	public static final String PROPERTY_INSTALLER_PORT_ADDRESS = "installer.port.address";

	public static final String PROPERTY_JDBC_URL = "jdbc.url";
	public static final String PROPERTY_JDBC_USERNAME = "jdbc.username";
	public static final String PROPERTY_JDBC_PASSWORD = "jdbc.password";
	
	public static final String PROPERTY_PDF_TO_IMAGE_DOTS_PER_INCH = "pdf.to.image.dpi";
	public static final String PROPERTY_KEEP_ALIVE = "keep.alive";
	public static final String PROPERTY_SUPPRESS_LOGS = "suppress.logs";	
	private static boolean persistenceActive = false;

	/**
	 * Sets the new state of the persistence.
	 * @param state New state to set.
	 */
	public static void setPersistenceActive(boolean state) {
		ConfigurationKeys.persistenceActive = state;
	}
	
	/**
	 * Gets the current persistence state.
	 * @return Persistence state.
	 */
	public static boolean isPersistenceActive() {
		return ConfigurationKeys.persistenceActive;
	}
}
