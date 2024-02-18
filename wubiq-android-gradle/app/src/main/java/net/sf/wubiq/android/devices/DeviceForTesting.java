/**
 * 
 */
package net.sf.wubiq.android.devices;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Validates the output of any of the printer types.
 * @author Federico Alcantara
 *
 */
public class DeviceForTesting extends BaseWubiqDevice {
	private final static String TAG = DeviceForTesting.class.getSimpleName();

    public final static String TEST_DEVICE_ADDRESS = "test_address";
	public final static int TEST_DEVICE_COUNT = 1;
	public final static String TEST_DEVICE_RESULT_KEY = "test_device_result_key";
    public final static String TEST_DEVICE_RESULT_IMAGE_KEY = "test_device_result_image_key";
	private DateFormat sdf = null;
	private SharedPreferences preferences;

	public DeviceForTesting(SharedPreferences preferences) {
	    this.preferences = preferences;
        sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
	}
	
	@Override
	protected boolean print() {
        String base64Image = "";
        byte[] data = getPrintData();
		base64Image = Base64.encodeToString(data, Base64.DEFAULT);
		String key = sdf.format(new Date()) + "-" + getMobileDeviceInfo().getName();
        Editor edit = preferences.edit();
		edit.putString(TEST_DEVICE_RESULT_KEY, key);
        edit.putString(TEST_DEVICE_RESULT_IMAGE_KEY, base64Image);
		edit.commit();
		return true;
	}
}
