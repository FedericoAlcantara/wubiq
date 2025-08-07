/**
 * 
 */
package net.sf.wubiq.android.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import net.sf.wubiq.android.R;
import net.sf.wubiq.android.enums.NotificationIds;

import java.net.ConnectException;

/**
 * @author Federico Alcantara
 *
 */
public class BluetoothUtils {
	public static final int REQUEST_CODE = 2;

	public static int bluetoothErrors = 0;
	private static final String TAG = BluetoothUtils.class.getSimpleName();

	
	/**
	 * Gets the Bluetooth adapter.
	 * @return Bluetooth adapter or null if an errors occurs.
	 */
	public static BluetoothAdapter getAdapter(Context context) throws ConnectException {
		BluetoothAdapter bAdapter = null;
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		try {
			bAdapter = BluetoothAdapter.getDefaultAdapter();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
    		notifyError(context);
    		bAdapter = null;
		}
		return bAdapter;
	}
	
	/**
	 * Finds the bluetooth device.
	 * @param context Running context.
	 * @param deviceAddress
	 * @return Found bluetooth device or null if not bonded.
	 */
	public static BluetoothDevice device(Context context,
			String deviceAddress) throws ConnectException {
		return device(context, getAdapter(context), deviceAddress);
	}

	
	/**
	 * Finds the bluetooth device.
	 * @param adapter Bluetooth adapter to gather the device from.
	 * @param deviceAddress
	 * @return Found bluetooth device or null if not bonded.
	 */
	public static BluetoothDevice device(Context context, BluetoothAdapter adapter,
			String deviceAddress) throws ConnectException {
		BluetoothDevice returnValue = null;
		if (adapter != null) {
			try {
				for (BluetoothDevice device : adapter.getBondedDevices()) {
					if (device.getAddress().equals(deviceAddress)) {
						returnValue = device;
						break;
					}
				}
			} catch (Exception e) {
                Log.e(TAG, e.getMessage());
                notifyError(context);
                return null;
            }
		}
		return returnValue;		
	}

	/**
	 * Notifies a bluetooth connection error.
	 * @param context Context where the app is running.
	 */
	public static void notifyError(Context context) {
		String message = context.getString(R.string.error_bluetooth);
		NotificationUtils.INSTANCE.notify(context, 
				NotificationIds.BLUETOOTH_ERROR_ID,
				bluetoothErrors,
				message);
		Log.e(TAG, message);
		bluetoothErrors++;
	}
	
	/**
	 * Cancels previous notification cancel.
	 * @param context Current application context.
	 */
	public static void cancelError(Context context) {
		NotificationUtils.INSTANCE.cancelNotification(context, NotificationIds.BLUETOOTH_ERROR_ID);
		bluetoothErrors = 0;
	}

	public static boolean bluetoothGranted(Object activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			if (ActivityCompat.checkSelfPermission((Context)activity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				if (activity instanceof Activity) {
					ActivityCompat.requestPermissions((Activity) activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE);
				}
			} else {
				return true;
			}
		} else {
			return (ActivityCompat.checkSelfPermission((Context)activity, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED);
		}
		return false;
	}
}
