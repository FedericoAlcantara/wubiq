/**
 * 
 */
package net.sf.wubiq.android.devices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.wubiq.android.enums.DeviceStatus;
import android.util.Log;

import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnectionException;

/**
 * @author Federico Alcantara
 *
 */
public class DeviceZebra extends BaseWubiqDevice {
	private final static String TAG = "DeviceZebra";

	/**
	 * @see net.sf.wubiq.android.devices.BaseWubiqDevice#print()
	 */
	@Override
	protected boolean print() {
		boolean returnValue = false;
		ZebraPrinterConnection connection = null;
		try {
			connection = new BluetoothPrinterConnection(getDeviceAddress());
			connection.open();
			ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
			int index = 0;
			for (index = 0; index < getPrintData().length; index++) {
				byte byteVal = getPrintData()[index];
				if (byteVal == 0x0D && (index + 1) < getPrintData().length  
						&& getPrintData()[index + 1] == 0x0A) { // Line feed
					index++;
					lineOut.write(new byte[]{0x0D, 0x0A});
					connection.write(lineOut.toByteArray());
					lineOut = new ByteArrayOutputStream();
					Thread.sleep(getPrintDelay());
				} else {
					lineOut.write(byteVal);
				}
			}
			if (lineOut.size() > 0) {
				connection.write(lineOut.toByteArray());
				Thread.sleep(getPrintDelay());
			}
			Thread.sleep(getPrintPause() * getPrintData().length / 1024);
			returnValue = true;
		} catch (ZebraPrinterConnectionException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (InterruptedException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (ZebraPrinterConnectionException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns the status of the device.
	 * @param deviceAddress Device address.
	 * @return Current device status.
	 */
	@Override
	public DeviceStatus deviceStatus(String deviceAddress) {
		DeviceStatus returnValue = DeviceStatus.NOT_FOUND;
		if (btDevice(deviceAddress) != null) {
			ZebraPrinterConnection connection = null;
			try {
				connection = new BluetoothPrinterConnection(deviceAddress);
				connection.open();
				Thread.sleep(getPrintPause());
				returnValue = DeviceStatus.READY;
			} catch (Exception e) {
				returnValue = DeviceStatus.CANT_CONNECT;
				Log.e(TAG, e.getMessage());
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (ZebraPrinterConnectionException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			}
		}
		return returnValue;
	}

}
