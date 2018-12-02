/**
 * 
 */
package net.sf.wubiq.android.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author Federico Alcantara
 *
 */
public class AndroidTestProperties {
	
	private static AndroidTestProperties instance;
	
	private static Properties properties;
	
	private AndroidTestProperties() {
	}
	
	/**
	 * Instance of test client properties.
	 * @return
	 */
	public static AndroidTestProperties instance() {
		if (instance == null) {
			instance = new AndroidTestProperties();
		}
		return instance;
	}
	
	/**
	 * Gets a value from properties file.
	 * @param key Key to search for.
	 * @param defaultValue Value to return in case key is not found in properties.
	 * @return Found value or default value.
	 */
	public static String get(String key, String defaultValue) {
		String returnValue = instance().getProperties().getProperty(key);
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
	public static int getInt(String key, int defaultValue) {
		String returnValue = get(key, Integer.toString(defaultValue));
		return Integer.parseInt(returnValue);
	}

	/**
	 * Gets current properties.
	 * @return Properties, might be null.
	 */
	private Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
				InputStream inputStream = this.getClass().getResourceAsStream("/wubiq-android-test.properties" );
				if (inputStream != null) {
					properties.load(inputStream);
					inputStream.close();
				}
			} catch (IOException e) {
				
			}
		}
		return properties;
	}
}
