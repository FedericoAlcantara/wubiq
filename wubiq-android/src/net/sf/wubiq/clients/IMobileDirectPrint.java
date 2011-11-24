/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.InputStream;

/**
 * @author Federico Alcantara
 *
 */
public interface IMobileDirectPrint {
	/**
	 * Print Bitmap represented as byte array.
	 * @param inputBmp Input byte array.
	 */
	void printBmpImage(String deviceId, InputStream input);
}
