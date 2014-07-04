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

	public static final String DEFAULT_PRINT_JOB_MANAGER = "net.sf.wubiq.print.managers.impl.HsqldbPrintJobManager";
	public static final String DEFAULT_REMOTE_PRINT_JOB_MANAGER = "net.sf.wubiq.print.managers.impl.DirectConnectPrintJobManager";

	
	public static final String PROPERTY_APPLICATION_NAME = "application";
	public static final String PROPERTY_SERVLET_NAME = "servlet";
	public static final String PROPERTY_UUID = "uuid";
	public static final String PROPERTY_GROUPS = "groups";
	public static final String PROPERTY_CLIENT_PARAMETERS = "client.parameters";
	public static final String PROPERTY_HOST = "host";
	public static final String PROPERTY_PORT = "port";
	public static final String PROPERTY_CONNECTIONS = "connections";
	
	public static final String PROPERTY_HSQLDB_HOST = "hsqldb.host";
	public static final String PROPERTY_HSQLDB_PORT = "hsqldb.port";
	public static final String PROPERTY_HSQLDB_DATABASE_NAME = "hsqldb.database";
	public static final String PROPERTY_HSQLDB_DB_ALIAS = "hsqldb.dbname";
	
	public static final String PROPERTY_PRINT_JOB_MANAGER = "manager";
	public static final String PROPERTY_REMOTE_PRINT_JOB_MANAGER = "remote.manager";
	
	public static final String PROPERTY_USERS = "users";
	
	public static final String PROPERTY_INSTALLER_PORT_ADDRESS = "installer.port.address";
	

}
