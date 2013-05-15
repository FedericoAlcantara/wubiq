/**
 * 
 */
package net.sf.wubiq.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages the label resources. Used as a platform for client and server labels handling.
 * @author Federico Alcantara
 *
 */
public class Labels {
	public static final String VERSION = "1.7.3";
	private static final Log LOG = LogFactory.getLog(Labels.class);
	private ResourceBundle bundle;
	private String labelFile;
	
	protected Labels(String labelFile) {
		this.labelFile = labelFile;
	}
	/**
	 * Gets the localized label.
	 * @param key Key name of the label to search for.
	 * @return The found label or the value sent in the key.
	 */
	public String get(String key, String... parameters) {
		String returnValue = null;
		try {
			returnValue = getBundle().getString(key);
			if (parameters.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					returnValue = returnValue.replaceAll("%"+i, parameters[i]);
				}
			}
		} catch (MissingResourceException e) {
			LOG.debug(e.getMessage());
			returnValue = key;
		}
		return returnValue;
	}
		
	/**
	 * @return the localized bundle.
	 */
	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(labelFile);
		}
		return bundle;
	}
	
	
	
}
