package net.sf.wubiq.android.devices;

import java.io.IOException;
import java.lang.reflect.Method;

import net.sf.wubiq.android.MobileDeviceInfo;
import net.sf.wubiq.android.enums.DeviceStatus;
import net.sf.wubiq.android.enums.PrintingStatus;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Defines the implementation needed for a wubiq enabled device.
 * @author Federico Alcantara
 *
 */
public abstract class BaseWubiqDevice extends Thread {
	private MobileDeviceInfo mobileDeviceInfo;
	private String deviceAddress;
	private byte[] printData;
	private int printDelay;
	private int printPause;
	private PrintingStatus printingStatus;
	private String TAG = "BaseWubiqDevice";
	
	/**
	 * Outputs to a star micronics portable printer.
	 * @param deviceInfo Device information
	 * @param deviceAddress Device address (mac address)
	 * @param printData Data to print
	 * @param printDelay Delays to apply between data chunks
	 * @param printPause Pause after print job is finished.
	 * @return true if everything is okey.
	 */
	public void initialize(MobileDeviceInfo mobileDeviceInfo, String deviceAddress,
			byte[] printData, int printDelay, int printPause) {
		this.mobileDeviceInfo = mobileDeviceInfo;
		this.deviceAddress = deviceAddress;
		this.printData = printData;
		this.printDelay = printDelay;
		this.printPause = printPause;
		this.printingStatus = PrintingStatus.PRINTING;
	}

	/**
	 * Finds the bluetooth device.
	 * @param deviceAddress
	 * @return Found bluetooth device or null if not bonded.
	 */
	protected BluetoothDevice btDevice(String deviceAddress) {
		BluetoothDevice returnValue = null;
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			if (device.getAddress().equals(deviceAddress)) {
				returnValue = device;
				break;
			}
		}
		return returnValue;		
	}
	
	/**
	 * Indicates the connectivity of the device.
	 * @param deviceAddress Device address.
	 * @return Status, either not found, cant_connect, ready.
	 */
	public DeviceStatus deviceStatus (String deviceAddress) {
		DeviceStatus returnValue = DeviceStatus.NOT_FOUND;
		BluetoothDevice device = btDevice(deviceAddress);
		if (device != null) {
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
		}
		return returnValue;
	}
	
	@Override
	public void run() {
		print();
	}

	/**
	 * Process the printing.
	 * @return True if printing was okey, false otherwise.
	 */
	protected abstract boolean print();

	/**
	 * @return the mobileDeviceInfo
	 */
	public MobileDeviceInfo getMobileDeviceInfo() {
		return mobileDeviceInfo;
	}

	/**
	 * @param mobileDeviceInfo the mobileDeviceInfo to set
	 */
	public void setMobileDeviceInfo(MobileDeviceInfo mobileDeviceInfo) {
		this.mobileDeviceInfo = mobileDeviceInfo;
	}

	/**
	 * @return the deviceAddress
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * @param deviceAddress the deviceAddress to set
	 */
	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	/**
	 * @return the printData
	 */
	public byte[] getPrintData() {
		return printData;
	}

	/**
	 * @param printData the printData to set
	 */
	public void setPrintData(byte[] printData) {
		this.printData = printData;
	}

	/**
	 * @return the printDelay
	 */
	public int getPrintDelay() {
		return printDelay;
	}

	/**
	 * @param printDelay the printDelay to set
	 */
	public void setPrintDelay(int printDelay) {
		this.printDelay = printDelay;
	}

	/**
	 * @return the printPause
	 */
	public int getPrintPause() {
		return printPause;
	}

	/**
	 * @param printPause the printPause to set
	 */
	public void setPrintPause(int printPause) {
		this.printPause = printPause;
	}

	/**
	 * @return the printingStatus
	 */
	public PrintingStatus getPrintingStatus() {
		return printingStatus;
	}

	/**
	 * @param printingStatus the printingStatus to set
	 */
	public void setPrintingStatus(PrintingStatus printingStatus) {
		this.printingStatus = printingStatus;
	}

}
