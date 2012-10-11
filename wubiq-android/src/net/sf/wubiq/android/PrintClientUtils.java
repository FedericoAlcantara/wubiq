/**
 * 
 */
package net.sf.wubiq.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

import net.sf.wubiq.android.devices.BaseWubiqDevice;
import net.sf.wubiq.android.devices.WubiqDeviceFactory;
import net.sf.wubiq.android.enums.DeviceStatus;
import net.sf.wubiq.android.enums.PrintingStatus;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

/**
 * Handles the necessary steps for printing on the client.
 * @author Federico Alcantara
 *
 */
public enum PrintClientUtils {
	INSTANCE;
	private final String TAG = "PrintClientUtils";
	private int printDelay = 500;
	private int printPause = 100; // Per each 1024 bytes
	private BaseWubiqDevice currentDevice = null;
	
	/**
	 * Prints the given input to the device, performing all required conversion steps.
	 * @param context Android context.
	 * @param printServiceName Complete device name.
	 * @param input Input data as a stream.
	 * @param resources Application resources.
	 * @param preferences Shared preferences of the application.
	 * @param printServicesName Available bluetooth devices.
	 * @return True if print job is processed.
	 */
	public boolean print(Context context, String printServiceName, InputStream input, Resources resources, 
			SharedPreferences preferences, Map<String, BluetoothDevice> printServicesName) {
		boolean returnValue = false;
		printDelay = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		printPause = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BluetoothDevice device = printServicesName.get(printServiceName);
		String deviceKey = WubiqActivity.DEVICE_PREFIX + device.getAddress();
		String selection = preferences.getString(deviceKey, null);
		MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(selection);
		String deviceAddress = device.getAddress();
		if (currentDevice == null ||
				!currentDevice.isAlive()) {
			if (currentDevice != null &&
					currentDevice.getPrintingStatus().equals(PrintingStatus.FINISHED_OKEY)) {
			}
				
			try {
				returnValue = true;
				byte[] b = new byte[16 * 1024];  
				int read;  
				while ((read = input.read(b)) != -1) {  
					output.write(b, 0, read);  
				}  
				byte[] printData = output.toByteArray();
				for (MobileClientConversionStep step : deviceInfo.getClientSteps()) {
					DeviceStatus deviceStatus = deviceStatus(deviceAddress);
					if (deviceStatus.equals(DeviceStatus.READY)) {
						currentDevice = WubiqDeviceFactory.INSTANCE.getInstance(step);
						if (currentDevice != null) {
							currentDevice.initialize(deviceInfo, deviceAddress, printData, printDelay, printPause);
							currentDevice.start();
						}
					} else if (deviceStatus.equals(DeviceStatus.CANT_CONNECT)) {
						returnValue = false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return returnValue;
	}
	
	/**
	 * Indicates the connectivity of the device.
	 * @param deviceAddress Device address.
	 * @return Status, either not found, cant_connect, ready.
	 */
	private DeviceStatus deviceStatus (String deviceAddress) {
		DeviceStatus returnValue = DeviceStatus.NOT_FOUND;
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			if (device.getAddress().equals(deviceAddress)) {
		        // Get a BluetoothSocket to connect with the given BluetoothDevice
				BluetoothSocket mmSocket = null;
				try {
		            // MY_UUID is the app's UUID string, also used by the server code
		        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
		            mmSocket = (BluetoothSocket) m.invoke(device, 1);
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		            returnValue = DeviceStatus.READY;
				} catch (Exception e) {
					returnValue = DeviceStatus.CANT_CONNECT;
					Log.e(TAG, e.getMessage());
				} finally {
					if (mmSocket != null) {
						try {
							mmSocket.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
				break;
			}
		}
		return returnValue;
	}
}
