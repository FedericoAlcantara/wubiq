/**
 * 
 */
package net.sf.wubiq.android.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;


/**
 * Use common communication mechanism to bluetooth devices.
 * @author Federico Alcantara
 *
 */
public class DeviceGeneric extends BaseWubiqDevice {
	private static final String TAG = DeviceGeneric.class.getSimpleName();

	/**
	 * Called from a run() method as a Thread runnable method.
	 */
	protected boolean print() {
		boolean returnValue = false;
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			if (device.getAddress().equals(getDeviceAddress())) {
		        // Get a BluetoothSocket to connect with the given BluetoothDevice
				BluetoothSocket mmSocket = null;
			    OutputStream mmOutStream = null;
				try {
		            // MY_UUID is the app's UUID string, also used by the server code
		        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
		            mmSocket = (BluetoothSocket) m.invoke(device, 1);
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		            mmOutStream = mmSocket.getOutputStream();
		        	// Copies are sent back to back through the same connection, without a pause between them.
		        	// NOTE: a pause between copies (waitBetweenCopies(), as DeviceStarMicronics does) is not possible
		        	// here. Generic ESC/POS printers have no standard command that tells when printing has finished,
		        	// and the write returns as soon as Bluetooth has the data (about 0 s for a full page), so a pause
		        	// counted from the write would not match what is printed. The Pause Between Copies option is only
		        	// shown when a Star Micronics printer is configured. If a way is found (a printer specific status
		        	// query), time it with a log line like the one in DeviceStarMicronics before enabling it.
		        	for (int copy = 0; copy < getCopies(); copy++) {
		        		writeCopy(mmOutStream);
		        	}
    				int sleepTime = (int) (getPrintData().length / 1024.0 * getPrintPause());
					Thread.sleep(sleepTime);
		        	returnValue = true;
		        } catch (Exception e) {
		        	Log.e(TAG, e.getMessage());
		        	e.printStackTrace();
		        	mmSocket = null;
				} finally {
					if (mmOutStream != null) {
						try {
							mmOutStream.flush();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
						try {
							mmOutStream.close();
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						}
					}
		        	if (mmSocket != null) {
		        		try {
		        			mmSocket.close();
		        		} catch(IOException ex) {
		        			Log.d(TAG, ex.getMessage());
		        		}
		        	}
				}	
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Writes one copy of the print data in chunks.
	 * @param mmOutStream Output stream of the open connection.
	 */
	private void writeCopy(OutputStream mmOutStream) throws IOException {
		int start = 0;
		int chunk = 4096;
		while (start < getPrintData().length) {
			int count = (start + chunk) < getPrintData().length ? chunk : getPrintData().length - start;
			mmOutStream.write(getPrintData(), start, count);
			start += count;
			try {
				Thread.sleep(getPrintDelay());
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
