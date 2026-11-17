/**
 * 
 */
package net.sf.wubiq.common;

/**
 * @author Federico Alcantara
 *
 */
public final class InstallerKeys {
	private InstallerKeys(){
	}
	
	public static final String INSTALLER_PROPERTIES_FILE_NAME = "wubiq-installer";
	/** Options that open the JDK packages the client reaches by reflection (client.add.opens of wubiq-client/build.xml). */
	public static final String[] CLIENT_ADD_OPENS = {
		"--add-opens=java.base/java.lang=ALL-UNNAMED",
		"--add-opens=java.desktop/javax.print=ALL-UNNAMED",
		"--add-opens=java.desktop/sun.print=ALL-UNNAMED"
	};
	public static final String CLIENT_MAIN_CLASS = "net.sf.wubiq.clients.LocalPrintManager";
	public static final String CONFIG_DIRECTORY_NAME = "Wubiq";
}
