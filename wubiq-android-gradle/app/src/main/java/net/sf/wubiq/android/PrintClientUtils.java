/**
 * 
 */
package net.sf.wubiq.android;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import net.sf.wubiq.android.devices.BaseWubiqDevice;
import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.devices.WubiqDeviceFactory;
import net.sf.wubiq.android.enums.PrintingStatus;
import net.sf.wubiq.android.utils.BluetoothUtils;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;

/**
 * Handles the necessary steps for printing on the client.
 * @author Federico Alcantara
 *
 */
public enum PrintClientUtils {
	INSTANCE;
	private final String TAG = PrintClientUtils.class.getSimpleName();

	private int printDelay = 500;
	private int printPause = 100; // Per each 1024 bytes
	private BaseWubiqDevice currentDevice = null;
	
	/**
	 * Prints the given input to the device, performing all required conversion steps.
	 * @param context Android context.
	 * @param deviceAddress address of the bluetooth device.
	 * @param input Input data as a stream.
	 * @param resources Application resources.
	 * @param preferences Shared preferences of the application.
	 * @return True if print job is processed.
	 */
	public boolean print(Context context, String deviceAddress, InputStream input, Resources resources, 
			SharedPreferences preferences) throws ConnectException {
		boolean returnValue = false;
		printDelay = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		printPause = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		long pauseBetweenPrints = preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default));
		int errorRetries = preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default));
		while (returnValue == false) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			boolean useDeviceTest = false;
			String deviceKey = null;
			BluetoothDevice device = BluetoothUtils.device(context, deviceAddress);
			if (device != null) {
				deviceKey = WubiqActivity.DEVICE_PREFIX + device.getAddress();
			}
			if (deviceKey == null 
					&& (preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false)
						|| preferences.getBoolean(WubiqActivity.ENABLE_DEVELOPMENT_MODE, false))
					&& deviceAddress.startsWith(DeviceForTesting.TEST_DEVICE_ADDRESS)) {
				deviceKey = WubiqActivity.DEVICE_PREFIX + deviceAddress;
				useDeviceTest = true;
			}
			if (deviceKey != null) {
				String selection = preferences.getString(deviceKey, null);

				MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(selection);
				if (deviceInfo == null
                        && useDeviceTest) {
				    deviceInfo = MobileDevices.INSTANCE.getDevices().get(MobileDevices.TEST_DEVICE_NAME.replaceAll("_", " "));
                }
				if (currentDevice == null ||
						!currentDevice.isAlive()) {
					if (currentDevice != null &&
							currentDevice.getPrintingStatus().equals(PrintingStatus.FINISHED_OKEY)) {
					}
						
					try {
						returnValue = true;
						IOUtils.INSTANCE.copy(input, output);
						output.flush();
						byte[] printData = output.toByteArray();
						for (MobileClientConversionStep step : deviceInfo.getClientSteps()) {
							currentDevice = !useDeviceTest 
									? WubiqDeviceFactory.INSTANCE.getInstance(step)
									: new DeviceForTesting(preferences);
							if (currentDevice != null) {
								currentDevice.initialize(deviceInfo, deviceAddress, printData, printDelay, printPause);
								currentDevice.start();
							}
						}
					} catch (Throwable e) {
						Log.e(TAG, e.getMessage());
						returnValue = false;
					} finally {
						try {
							output.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
				}
			}
			if (returnValue || errorRetries-- < 0) {
				break;
			}
			try {
				Thread.sleep(pauseBetweenPrints);
			} catch (InterruptedException e) {
				throw new ConnectException(e.getMessage());
			}
		}
		return returnValue;
	}
	
	/**
	 * Serializes the service name for sending the information to the server.
	 * @param name Name of the device.
	 * @param address Address of the device.
	 * @param selection Driver selected.
	 * @return Serialized name / driver combination.
	 */
	public String serializeServiceName(String name, String address, String selection) {
		StringBuffer printServiceRegister = new StringBuffer("")
			.append(cleanPrintServiceName(name))
			.append(ParameterKeys.ATTRIBUTE_SET_SEPARATOR)
			.append(address)
			.append(ParameterKeys.ATTRIBUTE_SET_SEPARATOR)
			.append(cleanPrintServiceName(selection));
		return printServiceRegister.toString();
	}

	/**
	 * @param printServiceName Returns a cleaned print service name.
	 * @return A cleaned (no strange characters) print service name.
	 */
	private String cleanPrintServiceName(String printServiceName) {
		StringBuffer returnValue = new StringBuffer("");
		for (int index = 0; index < printServiceName.length(); index++) {
			char charAt = printServiceName.charAt(index);
			if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".indexOf(charAt) >= 0) {
				returnValue.append(charAt);
			} else {
				returnValue.append("_");
			}
		}
		return returnValue.toString();
	}

}
