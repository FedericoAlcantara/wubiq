/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.wubiq.common.InstallerKeys;
import net.sf.wubiq.common.PropertyKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities for installing.
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
	
	/**
	 * Package type the client was installed with.
	 * @return Value of the wubiq.package.type system property, blank when it is not set.
	 */
	public String packageType() {
		String returnValue = System.getProperty(PropertyKeys.WUBIQ_PACKAGE_TYPE);
		return returnValue == null ? "" : returnValue;
	}

	/**
	 * Tells whether this client runs from a native package.
	 * @return True when a package type is set.
	 */
	public boolean isPackaged() {
		return !Is.emptyString(packageType());
	}

	/**
	 * Directory that holds wubiq-installer.jar and the other jars, whatever the operating system's package layout.
	 * @return Application directory.
	 */
	public File appDirectory() {
		try {
			return new File(InstallerUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Configuration directory of a packaged client. It is created when it does not exist.
	 * @return Configuration directory.
	 */
	public File configDirectory() {
		String os = System.getProperty("os.name");
		File returnValue;
		if (os.startsWith("Windows")) {
			String programData = System.getenv("ProgramData");
			if (programData == null) {
				programData = "C:\\ProgramData";
			}
			returnValue = new File(programData, InstallerKeys.CONFIG_DIRECTORY_NAME);
		} else if (os.startsWith("Mac")) {
			returnValue = new File("/Library/Application Support", InstallerKeys.CONFIG_DIRECTORY_NAME);
		} else {
			returnValue = new File("/etc/wubiq");
		}
		if (!returnValue.exists()) {
			returnValue.mkdirs();
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
