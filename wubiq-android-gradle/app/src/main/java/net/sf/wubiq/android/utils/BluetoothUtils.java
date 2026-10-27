/**
 * 
 */
package net.sf.wubiq.android.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Federico Alcantara
 *
 */
public class BluetoothUtils {
	public static final int REQUEST_CODE = 2;

	private static final String[] BLUETOOTH_PERMISSIONS = {
			Manifest.permission.BLUETOOTH_CONNECT,
			Manifest.permission.BLUETOOTH_SCAN
	};

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
		notifyBluetoothError(context, context.getString(R.string.error_bluetooth));
	}

	/**
	 * Notifies a bluetooth error with the given message.
	 * @param context Context where the app is running.
	 * @param message Message to show.
	 */
	private static void notifyBluetoothError(Context context, String message) {
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

	/**
	 * Checks the bluetooth runtime permissions. On Android 12+ both BLUETOOTH_CONNECT and
	 * BLUETOOTH_SCAN are required (printer libraries such as StarIO need BLUETOOTH_SCAN to connect).
	 * Missing permissions are requested when called from an activity, otherwise the user is notified.
	 * @param activity Activity or context requesting bluetooth access.
	 * @return True if all bluetooth permissions are granted.
	 */
	public static boolean bluetoothGranted(Object activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			List<String> missing = new ArrayList<>();
			for (String permission : BLUETOOTH_PERMISSIONS) {
				if (ActivityCompat.checkSelfPermission((Context)activity, permission) != PackageManager.PERMISSION_GRANTED) {
					missing.add(permission);
				}
			}
			if (missing.isEmpty()) {
				return true;
			}
			if (activity instanceof Activity) {
				ActivityCompat.requestPermissions((Activity) activity, missing.toArray(new String[0]), REQUEST_CODE);
			} else {
				notifyBluetoothError((Context) activity, ((Context) activity).getString(R.string.error_bluetooth_permission));
			}
		} else {
			return (ActivityCompat.checkSelfPermission((Context)activity, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED);
		}
		return false;
	}

	/**
	 * Tells whether a bonded device could be a printer, based on its bluetooth class.
	 * Only devices that are clearly something else (audio, phones, computers, wearables, toys,
	 * health devices, keyboards and mice) are rejected, as many portable printers don't report
	 * the printer class.
	 * Requires bluetooth permissions to be granted.
	 * @param device Bonded bluetooth device.
	 * @return False only if the device is clearly not a printer.
	 */
	public static boolean isPossiblePrinter(BluetoothDevice device) {
		BluetoothClass bluetoothClass;
		try {
			bluetoothClass = device.getBluetoothClass();
		} catch (SecurityException e) {
			return true;
		}
		if (bluetoothClass == null) {
			return true;
		}
		int majorClass = bluetoothClass.getMajorDeviceClass();
		Log.i(TAG, "Device " + device.getAddress() + " class:0x" + Integer.toHexString(bluetoothClass.getDeviceClass())
				+ " major:0x" + Integer.toHexString(majorClass));
		switch (majorClass) {
			case BluetoothClass.Device.Major.AUDIO_VIDEO:
			case BluetoothClass.Device.Major.PHONE:
			case BluetoothClass.Device.Major.COMPUTER:
			case BluetoothClass.Device.Major.WEARABLE:
			case BluetoothClass.Device.Major.TOY:
			case BluetoothClass.Device.Major.HEALTH:
				return false;
			case BluetoothClass.Device.Major.PERIPHERAL:
				// Keyboards and pointing devices (mice, touchpads); other peripherals are kept.
				int deviceClass = bluetoothClass.getDeviceClass();
				return deviceClass != BluetoothClass.Device.PERIPHERAL_KEYBOARD
						&& deviceClass != BluetoothClass.Device.PERIPHERAL_POINTING
						&& deviceClass != BluetoothClass.Device.PERIPHERAL_KEYBOARD_POINTING;
			default:
				return true;
		}
	}

	/**
	 * Checks the result of a bluetooth permissions request.
	 * @param grantResults Results as received by onRequestPermissionsResult.
	 * @return True if every requested permission was granted.
	 */
	public static boolean allGranted(int[] grantResults) {
		if (grantResults.length == 0) {
			return false;
		}
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}
