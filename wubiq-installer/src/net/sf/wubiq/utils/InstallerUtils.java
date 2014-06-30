/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import net.sf.wubiq.clients.Constants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum InstallerUtils {
	INSTANCE;
	
	private final Log LOG = LogFactory.getLog(InstallerUtils.class);
	
	/**
	 * Port address use by the printer launcher listener. This allow to 
	 * start / stop / restart the drivers.
	 * @return The port address. Always a valid port number.
	 */
	public int getPortAddress() {
		int returnValue = Constants.CONFIGURATION_DEFAULT_PORT_ADDRESS;
		URL url = this.getClass()
				.getResource(Constants.CONFIGURATION_FILE_NAME);
		if (url != null) {
			Properties properties = new Properties();
			try {
				properties.load(url.openStream());
				String portAddress = properties.getProperty(Constants.CONFIGURATION_PROPERTY_PORT_ADDRESS);
				if (!Is.emptyString(portAddress)) {
					returnValue = Integer.parseInt(portAddress);
				}
			} catch (NumberFormatException e) {
				LOG.error(e.getMessage(), e);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		if (returnValue < 1 || returnValue > 65535) {
			returnValue = Constants.CONFIGURATION_DEFAULT_PORT_ADDRESS;
		}
		return returnValue;
	}
	
	/**
	 * Wubiq client version.
	 * @param wubiqClientJar Jar location of the wubiq client.
	 * @return String containing the client version or blank if client is too old or does not exists.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String wubiqClientVersion(File wubiqClientJar) throws 
			IOException, InterruptedException {
		String returnValue = "";
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("java", "-cp", wubiqClientJar.toString(), "net.sf.wubiq.clients.VersionInformation");
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			returnValue = line;
		}
		process.waitFor();
		return returnValue;
	}
	
	public String wubiqServerVersion() {
		return null;
	}
}
