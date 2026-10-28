/**
 * 
 */
package net.sf.wubiq.android.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import net.sf.wubiq.android.R;
import net.sf.wubiq.android.WubiqActivity;
import net.sf.wubiq.android.enums.NotificationIds;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
	/** Preference set once the bluetooth permissions have been requested at least once. */
	private static final String PERMISSIONS_REQUESTED = "bluetooth_permissions_requested";
	/** Name fragments (lower case) of printers supported by Wubiq, or of devices that are obviously printers. */
	private static final String[] PRINTER_NAMES = {"star", "zebra", "datamax", "porti", "print"};

	
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
				((Activity) activity).getSharedPreferences(WubiqActivity.PREFERENCES, Context.MODE_PRIVATE)
						.edit().putBoolean(PERMISSIONS_REQUESTED, true).apply();
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
	 * Tells whether a bonded device could be a printer, based on its name and bluetooth class.
	 * Devices named after a known printer brand are always accepted, as the class stored by
	 * Android can be wrong (e.g. a Star printer paired while in iAP mode is stored as audio/video).
	 * Otherwise only devices that are clearly something else (audio, phones, computers, wearables,
	 * toys, health devices, keyboards and mice) are rejected, as many portable printers don't
	 * report the printer class.
	 * Requires bluetooth permissions to be granted.
	 * @param device Bonded bluetooth device.
	 * @return False only if the device is clearly not a printer.
	 */
	public static boolean isPossiblePrinter(BluetoothDevice device) {
		BluetoothClass bluetoothClass;
		String name;
		try {
			bluetoothClass = device.getBluetoothClass();
			name = device.getName();
		} catch (SecurityException e) {
			return true;
		}
		if (bluetoothClass == null) {
			return true;
		}
		int majorClass = bluetoothClass.getMajorDeviceClass();
		Log.i(TAG, "Device " + device.getAddress() + " class:0x" + Integer.toHexString(bluetoothClass.getDeviceClass())
				+ " major:0x" + Integer.toHexString(majorClass));
		if (isPrinterName(name)) {
			return true;
		}
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
	 * Tells whether the device name belongs to a known printer brand.
	 * @param name Bluetooth device name, may be null.
	 * @return True if the name contains a known printer name fragment.
	 */
	static boolean isPrinterName(String name) {
		if (name == null) {
			return false;
		}
		String lowerName = name.toLowerCase(Locale.ROOT);
		for (String printerName : PRINTER_NAMES) {
			if (lowerName.contains(printerName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tells whether any bluetooth runtime permission is missing. Before Android 12 the
	 * bluetooth permissions are granted at install time, so nothing is ever missing.
	 * @param context Running context.
	 * @return True if a bluetooth permission still has to be granted.
	 */
	public static boolean bluetoothPermissionMissing(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			for (String permission : BLUETOOTH_PERMISSIONS) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Tells whether Android stopped showing the bluetooth permission prompt (denied twice, or
	 * "don't ask again"). Then the permission can only be granted from the app settings.
	 * @param activity Current activity.
	 * @return True if the permissions are missing and requesting them no longer shows a prompt.
	 */
	public static boolean bluetoothPermissionBlocked(Activity activity) {
		if (!bluetoothPermissionMissing(activity)
				|| !activity.getSharedPreferences(WubiqActivity.PREFERENCES, Context.MODE_PRIVATE)
						.getBoolean(PERMISSIONS_REQUESTED, false)) {
			return false;
		}
		for (String permission : BLUETOOTH_PERMISSIONS) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Asks again for the bluetooth permissions, or opens the app settings if Android no longer
	 * shows the prompt.
	 * @param activity Current activity.
	 */
	public static void resolveBluetoothPermission(Activity activity) {
		if (bluetoothPermissionBlocked(activity)) {
			activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
					Uri.fromParts("package", activity.getPackageName(), null)));
		} else {
			bluetoothGranted(activity);
		}
	}

	/**
	 * Label for the action that resolves the missing bluetooth permissions.
	 * @param activity Current activity.
	 * @return "Open settings" if the prompt is no longer shown, "Allow" otherwise.
	 */
	public static String bluetoothPermissionAction(Activity activity) {
		return activity.getString(bluetoothPermissionBlocked(activity)
				? R.string.bluetooth_permission_settings
				: R.string.bluetooth_permission_allow);
	}

	/**
	 * Explains that the bluetooth permissions are needed, with a button that asks again or
	 * opens the app settings.
	 * @param activity Current activity.
	 */
	public static void showBluetoothPermissionDialog(Activity activity) {
		new AlertDialog.Builder(activity)
				.setMessage(R.string.bluetooth_permission_needed)
				.setPositiveButton(bluetoothPermissionAction(activity),
						(dialog, which) -> resolveBluetoothPermission(activity))
				.setNegativeButton(android.R.string.cancel, null)
				.show();
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
