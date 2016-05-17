/**
 * 
 */
package net.sf.wubiq.android.devices;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Validates the output of any of the printer types.
 * @author Federico Alcantara
 *
 */
public class DeviceForTesting extends BaseWubiqDevice {
	private final static String TAG = DeviceForTesting.class.getSimpleName();
	
	public final static String TEST_DEVICE_NAME = "test_device";
	public final static String TEST_DEVICE_ADDRESS = "test_address";
	public final static int TEST_DEVICE_COUNT = 3;
	public final static String TEST_DEVICE_RESULT_KEY = "test_device_result_key";
	
	private SharedPreferences preferences;
	
	public DeviceForTesting(SharedPreferences preferences) {
		this.preferences = preferences;
	}
	
	@Override
	protected boolean print() {
		Editor edit = preferences.edit();
		String md5 = getMD5EncryptedString(getPrintData());
		edit.putString(TEST_DEVICE_RESULT_KEY, md5);
		edit.commit();
		return true;
	}
	
	public static String getMD5EncryptedString(byte[] source) {
		String returnValue = "";
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(source, 0, source.length);
            returnValue = new BigInteger(1, md.digest()).toString(16);
            while ( returnValue.length() < 32 ) {
                returnValue = "0"+returnValue;
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        } // Encryption algorithm
        return returnValue;
    }

}
