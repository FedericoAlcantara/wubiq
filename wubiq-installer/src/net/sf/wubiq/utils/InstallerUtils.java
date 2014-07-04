/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum InstallerUtils {
	INSTANCE;
	
	private final Log LOG = LogFactory.getLog(InstallerUtils.class);
	
	public String jrePath() {
		if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
			return System.getProperty("java.home") + "/bin/java.exe";
		}
		return System.getProperty("java.home") + "/bin/java";
	}
	
	public File installPath() {
		return new File(System.getProperty("user.dir"));
	}
	
	public File wubiqClientFile() {
		return new File(installPath().getPath() + "/wubiq-client.jar");
	}
	
	/**
	 * Wubiq client version.
	 * @param wubiqClientJar Jar location of the wubiq client.
	 * @return String containing the client version or blank if client is too old or does not exists.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public String wubiqClientVersion() {
		String returnValue = "";
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(jrePath(), "-cp", wubiqClientFile().toString(), "net.sf.wubiq.clients.VersionInformation");
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				returnValue = line;
			}
			process.waitFor();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			LOG.fatal(e.getMessage(), e);
		} finally {
			
		}
		return returnValue;
	}
}
