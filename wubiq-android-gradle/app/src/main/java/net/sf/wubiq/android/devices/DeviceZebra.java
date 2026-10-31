/**
 * 
 */
package net.sf.wubiq.android.devices;

import android.content.Context;
import android.util.Log;

import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnectionException;

import net.sf.wubiq.android.enums.DeviceStatus;
import net.sf.wubiq.android.utils.BluetoothUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;

/**
 * @author Federico Alcantara
 *
 */
public class DeviceZebra extends BaseWubiqDevice {
	private final static String TAG = DeviceZebra.class.getSimpleName();

	/**
	 * @see BaseWubiqDevice#print()
	 */
	@Override
	protected boolean print() {
		boolean returnValue = false;
		ZebraPrinterConnection connection = null;
		try {
			connection = new BluetoothPrinterConnection(getDeviceAddress());
			connection.open();
			// Copies are sent back to back through the same connection, without a pause between them.
			// NOTE: this path is not tested, there was no Zebra printer to try it on. A pause between copies
			// (waitBetweenCopies(), as DeviceStarMicronics does after endCheckedBlock()) needs a way to know the
			// printer has finished. The Zebra library can report it: connection -> ZebraPrinterFactory.getInstance()
			// -> printer.getCurrentStatus() gives PrinterStatus with isReadyToPrint, isPaused,
			// numberOfFormatsInReceiveBuffer and isPartialFormatInProgress. A possible approach is to poll after
			// each copy until the receive buffer is empty and the printer is ready, with a time limit. It is
			// unknown whether the CPCL models (MZ220, MZ320) report those fields while printing. Time it with a
			// log line like the one in DeviceStarMicronics before enabling it, and then show the Pause Between
			// Copies option for Zebra drivers (see PrintClientUtils.usesStarMicronics()).
			for (int copy = 0; copy < getCopies(); copy++) {
				writeCopy(connection);
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
	 * Writes one copy of the print data, line by line.
	 * @param connection Open connection to the printer.
	 */
	private void writeCopy(ZebraPrinterConnection connection) throws Exception {
		ByteArrayOutputStream lineOut = new ByteArrayOutputStream();
		for (int index = 0; index < getPrintData().length; index++) {
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
	}

	/**
	 * Returns the status of the device.
	 * @param deviceAddress Device address.
	 * @return Current device status.
	 */
	public DeviceStatus deviceStatus(Context context, String deviceAddress) throws ConnectException {
		DeviceStatus returnValue = DeviceStatus.NOT_FOUND;
		if (BluetoothUtils.device(context, deviceAddress) != null) {
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
