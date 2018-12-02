/**
 * 
 */
package net.sf.wubiq.android.devices;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import net.sf.wubiq.android.MobileClientConversionStep;
import net.sf.wubiq.android.MobileDeviceInfo;
import net.sf.wubiq.android.MobileServerConversionStep;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//            outputStream.flush();
            base64Image = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
		String key = sdf.format(new Date()) + "-" + getMobileDeviceInfo().getName();
        Editor edit = preferences.edit();
		edit.putString(TEST_DEVICE_RESULT_KEY, key);
        edit.putString(TEST_DEVICE_RESULT_IMAGE_KEY, base64Image);
		edit.commit();
		return true;
	}
}
