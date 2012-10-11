/**
 * 
 */
package net.sf.wubiq.android.devices;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


/**
 * Use common communication mechanism to bluetooth devices.
 * @author Federico Alcantara
 *
 */
public class DeviceGeneric extends BaseWubiqDevice {
	private static final String TAG = "DeviceGeneric";

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
		        	int start = 0;
		        	int chunk = 4096;
		        	while (start < getPrintData().length) {
		        		int count = (start + chunk) < getPrintData().length ? chunk : getPrintData().length - start;
		        		mmOutStream.write(getPrintData(), start, count);
		        		start += count;
		    			try
		    			{
		    				Thread.sleep(getPrintDelay());
		    			}
		    			catch(InterruptedException e) {
		    				e.printStackTrace();
		    			}
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
}
