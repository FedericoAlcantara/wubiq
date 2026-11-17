/**
 * 
 */
package net.sf.wubiq.utils;

import net.sf.wubiq.utils.Labels;

/**
 * Gets the labels associated to wubiq's clients.
 * @author Federico Alcantara
 *
 */
public class ClientLabels {
	private static Labels labels;
	private ClientLabels() {
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
	 * @return the labels
	 */
	private static Labels getLabels() {
		if (labels == null) {
			labels = new Labels("client_labels");
		}
		return labels;
	}
	
}
