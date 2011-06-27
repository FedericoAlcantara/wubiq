/**
 * 
 */
package org.wubiq.utils;

/**
 * Helper class for testing common conditions.
 * @author Federico Alcantara
 *
 */
public class Is {
	/**
	 * Checks if the string is null or is empty
	 * @param string String to be checked
	 * @return True if the string is null or empty.
	 */
	public static boolean emptyString(String string) {
		if (string == null || string.equals("")) {
			return true;
		} else {
			return false;
		}
	}
}
