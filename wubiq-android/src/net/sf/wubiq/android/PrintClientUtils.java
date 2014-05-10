/**
 * 
 */
package net.sf.wubiq.android;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import net.sf.wubiq.android.devices.BaseWubiqDevice;
import net.sf.wubiq.android.devices.WubiqDeviceFactory;
import net.sf.wubiq.android.enums.DeviceStatus;
import net.sf.wubiq.android.enums.PrintingStatus;
import android.bluetooth.BluetoothDevice;
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
	private int printDelay = 500;
	private int printPause = 100; // Per each 1024 bytes
	private BaseWubiqDevice currentDevice = null;
	private final String TAG = "PrintClientUtils";
	
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
					currentDevice = WubiqDeviceFactory.INSTANCE.getInstance(step);
					if (currentDevice != null) {
						DeviceStatus deviceStatus = currentDevice.deviceStatus(deviceAddress);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							Log.e(TAG, e.getMessage());
						}
						currentDevice.initialize(deviceInfo, deviceAddress, printData, printDelay, printPause);
						if (deviceStatus.equals(DeviceStatus.READY)) {
							currentDevice.start();
						} else {
							returnValue = false;
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				returnValue = false;
			}
		}
		return returnValue;
	}
	

}
