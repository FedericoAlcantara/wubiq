/**
 * 
 */
package net.sf.wubiq.android.utils;

import java.net.ConnectException;

import net.sf.wubiq.android.R;
import net.sf.wubiq.android.enums.NotificationIds;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * @author Federico Alcantara
 *
 */
public class BluetoothUtils {
	
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
		return device(getAdapter(context), deviceAddress);
	}

	
	/**
	 * Finds the bluetooth device.
	 * @param adapter Bluetooth adapter to gather the device from.
	 * @param deviceAddress
	 * @return Found bluetooth device or null if not bonded.
	 */
	public static BluetoothDevice device(BluetoothAdapter adapter,
			String deviceAddress) throws ConnectException {
		BluetoothDevice returnValue = null;
		if (adapter != null) {
			for (BluetoothDevice device : adapter.getBondedDevices()) {
				if (device.getAddress().equals(deviceAddress)) {
					returnValue = device;
					break;
				}
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
}
