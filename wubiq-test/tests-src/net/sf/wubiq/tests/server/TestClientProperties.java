/**
 * 
 */
package net.sf.wubiq.tests.server;

import net.sf.wubiq.utils.ClientProperties;

/**
 * @author Federico Alcantara
 *
 */
public class TestClientProperties extends ClientProperties {
	
	public static final TestClientProperties INSTANCE = new TestClientProperties(); 
	
	private TestClientProperties() {
		
	}
	/**
	 * Gets a value from properties file.
	 * @param key Key to search for.
	 * @param defaultValue Value to return in case key is not found in properties.
	 * @return Found value or default value.
	 */
	public String get(String key, String defaultValue) {
		String returnValue = getProperties().getProperty(key);
		if (returnValue == null) {
			returnValue = defaultValue;
		}
		return returnValue;
	}
	
	/**
	 * Returns the integer value of a key.
	 * @param key Key to look for.
	 * @param defaultValue Default value to use if key is not found.
	 * @return Integer value found or the given default value.
	 */
	public int getInt(String key, int defaultValue) {
		String returnValue = get(key, Integer.toString(defaultValue));
		return Integer.parseInt(returnValue);
	}

}
