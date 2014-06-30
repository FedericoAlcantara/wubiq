package net.sf.wubiq.utils;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the messages.
 * @author Federico Alcantara
 *
 */
public final class InstallerBundle {
	private static final Log LOG = LogFactory.getLog(InstallerBundle.class);
	
	private static ResourceBundle messagesBundle;
	private static ResourceBundle labelsBundle;
	
	private InstallerBundle() {
	}
	
	/**
	 * Resets all bundles.
	 */
	public static void resetLocale() {
		messagesBundle = null;
		labelsBundle = null;
	}
	/**
	 * Returns the value of the messagesBundle (if found).
	 * @param key Key to look for.
	 * @param parameters substitution parameters.
	 * @return The found and translated string or the same key.
	 */
	public static String getMessage(String key, Object... parameters) {
		String returnValue = key;
		try {
			returnValue = getMessagesBundle().getString(key);
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] != null) {
					String lookFor = "\\{" + i + "\\}";
					returnValue = returnValue.replaceAll(lookFor, parameters[i].toString());
				}
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage() + ":" + key);
			returnValue = key;
		}
		return returnValue;
	}
	
	/**
	 * Returns the value of the labels bundle (if found).
	 * @param key Key to look for.
	 * @return The found and translated string or the same key.
	 */
	public static String getLabel(String key) {
		String returnValue = null;
		try {
			returnValue = getLabelsBundle().getString(key);
		} catch (Exception e) {
			LOG.error(e.getMessage() + ":" + key);
			returnValue = key;
		}
		return returnValue;
	}
	
	/**
	 * Returns the translated error code description.
	 * @param errorCode Error code.
	 * @return Localized error code or errorCode if not found.
	 */
	public static String getError(String errorCode) {
		return getMessage("error." + errorCode);
	}
	
	/**
	 * @return The singleton instance of the messages bundle.
	 */
	private static ResourceBundle getMessagesBundle() {
		if (messagesBundle == null) {
			messagesBundle = ResourceBundle.getBundle("installer_messagess");
		}
		return messagesBundle;
	}
	
	/**
	 * @return The singleton instance of the labels bundle.
	 */
	private static ResourceBundle getLabelsBundle() {
		if (labelsBundle == null) {
			labelsBundle = ResourceBundle.getBundle("installer_labels");
		}
		return labelsBundle;
	}

}
