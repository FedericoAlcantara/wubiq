/**
 * 
 */
package net.sf.wubiq.utils;

/**
 * @author Federico Alcantara
 *
 */
public enum ServerUtils {
	INSTANCE;
	
	/**
	 * Returns only lower case characters a-z (upper case are converted to lower case)
	 * numbers and underscore. Leading and trailing spaces are removed.
	 * Blank spaces in the middle of characters are converted to underscore.
	 * @param userId the userId to be normalized.
	 */
	public String normalizedUserId(String userId) {
		if (!Is.emptyString(userId)) {
			return userId.trim().toLowerCase()
					.replaceAll(" ", "_")
					.replaceAll("[^a-z_0-9]", "");
		} else {
			return "";
		}
	}
	
	/**
	 * Returns only characters a-z (upper or lowercase),
	 * numbers, - . ,underscore and space. Other characters are removed.
	 * @param password the password to be normalized. 
	 */
	public String normalizedPassword(String password) {
		if (!Is.emptyString(password)) {
			return password
					.replaceAll("[^a-z \\.A-Z_\\-0-9]", "");
		} else {
			return "";
		}
	}
}
