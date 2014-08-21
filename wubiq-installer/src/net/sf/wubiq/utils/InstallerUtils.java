/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum InstallerUtils {
	INSTANCE;
	
	private final Log LOG = LogFactory.getLog(InstallerUtils.class);
	private final String VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";
	private final String VALID_INTERNET_CHARACTERS = VALID_CHARACTERS + "./:";
	
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
	
	/**
	 * Validates the given address.
	 * @param address Address to be validated.
	 * @return True if the address has a valid format. False otherwise.
	 */
	public boolean validateAddress(String address) {
		boolean returnValue = true;
		String message = invalidAddressMessage(address);
		if (!Is.emptyString(message)) {
			LOG.debug(message);
			returnValue = false;
		}
		return returnValue;
	}
	
	/**
	 * Generates an invalid address localized message. If address is valid a blank message is returned instead.
	 * @param address Address to be checked.
	 * @return Message.
	 */
	public String invalidAddressMessage(String address) {
		String returnValue = "";
		try {
			URL url = new URL(address);
			String protocol = url.getProtocol();
			String host = url.getHost();
			if (!Is.emptyString(protocol) &&
					!Is.emptyString(host)) {
				returnValue = "";
			}
		} catch (MalformedURLException e) {
			returnValue = InstallerBundle.getMessage("error.invalid_address",
					address, e.getMessage());
		}
		return returnValue;
	}
	
	/**
	 * Cleans the string.
	 * @param sentText Text sent.
	 * @return Text without invalid characters.
	 */
	public String cleanString(String sentText) {
		return cleanText(VALID_CHARACTERS, sentText);
	}
	
	/**
	 * Cleans the text and anly allows internet valid characters.
	 * @param sentText Text sent.
	 * @return Clean address and converted to lower case.
	 */
	public String cleanInternetAddress(String sentText) {
		return cleanText(VALID_INTERNET_CHARACTERS, sentText).toLowerCase();
	}
	
	/**
	 * Cleans the text. Leading and trailing text are eliminated.
	 * Spaces are replaced with _, other characters are deleted.
	 * @param validChars String of valid characters.
	 * @param sentText Input text.
	 * @return Parsed text.
	 */
	private String cleanText(String validChars, String sentText) {
		StringBuffer returnValue = new StringBuffer("");
		String text = sentText.trim();
		for (int index = 0; index < text.length(); index++) {
			char charAt = text.charAt(index);
			if (charAt == ' ' ||
					validChars.indexOf(charAt) > -1) {
				returnValue.append(charAt);
			}
		}
		
		return returnValue.toString().trim().replaceAll(" ", "_");
	}
	
}
