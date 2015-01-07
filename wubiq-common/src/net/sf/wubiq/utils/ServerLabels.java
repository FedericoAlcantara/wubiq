/**
 * 
 */
package net.sf.wubiq.utils;

import java.util.Locale;

/**
 * Gets the labels associated to wubiq's servers.
 * @author Federico Alcantara
 *
 */
public class ServerLabels {
	private static Labels labels;
	
	private ServerLabels() {
	}
	
	/**
	 * Gets the localized label.
	 * @param key Key name of the label to search for.
	 * @return The found label or the value sent in the key.
	 */
	public static String get(String key, String... parameters) {
		return getLabels().get(key, parameters);
	}

	/**
	 * @return the labels.
	 */
	private static Labels getLabels() {
		if (labels == null) {
			labels = new Labels("server_labels");
		}
		return labels;
	}
	
	/**
	 * Returns the current locale.
	 * @return Value of the locale.
	 */
	public static Locale getLocale() {
		if (getLabels().getLocale() == null) {
			return Locale.getDefault();
		}
		return getLabels().getLocale();
	}

	/**
	 * Sets a new locale for the server labels.
	 * @param locale Locale to be set.
	 */
	public static void setLocale(Locale locale) {
		getLabels().setLocale(locale);
	}
	
}
