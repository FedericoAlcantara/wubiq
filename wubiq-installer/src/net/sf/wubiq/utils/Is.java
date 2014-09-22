/**
 * 
 */
package net.sf.wubiq.utils;

/**
 * Specific tests.
 * @author Federico Alcantara
 *
 */
public class Is {
	private Is() {
	}
	
	/**
	 * Returns true if empty or null.
	 * @param input String to test.
	 * @return True or false.
	 */
	public static boolean emptyString(String input) {
		return input == null || "".equals(input);
	}
}
