/**
 * 
 */
package net.sf.wubiq.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.wubiq.common.ParameterKeys;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

/**
 * Handles the necessary steps for printing on the client.
 * @author Federico Alcantara
 *
 */
public enum PrintClientUtils {
	INSTANCE;
	private static final String TAG = "PrintClientUtils";
	int printDelay = 500;
	int printPause = 100; // Per each 1024 bytes
	
	/**
	 * Prints the given input to the device, performing all required conversion steps.
	 * @param context Android context.
	 * @param deviceName Complete device name.
	 * @param input Input data as a stream
	 */
	public void print(Context context, String deviceName, InputStream input, Resources resources, SharedPreferences preferences) {
		printDelay = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		printPause = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String[] deviceData = deviceName.split(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
		MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(deviceData[2]);
		String deviceAddress = deviceData[1];
		try {
			byte[] b = new byte[16 * 1024];  
			int read;  
			while ((read = input.read(b)) != -1) {  
				output.write(b, 0, read);  
			}  
			byte[] printData = output.toByteArray();
			for (MobileClientConversionStep step : deviceInfo.getClientSteps()) {
				if (step.equals(MobileClientConversionStep.OUTPUT_BYTES)) {
					printBytes(deviceInfo, deviceAddress, printData);
				} else if (step.equals(MobileClientConversionStep.OUTPUT_SM_BYTES)) {
					printStarMicronicsByteArray(deviceInfo, deviceAddress, printData); // Does not print all the data
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Outputs to a star micronics portable printer.
	 * @param deviceInfo Device information
	 * @param deviceAddress Device address (mac address)
	 * @param printData Data to print
	 * @return true if everything is okey.
	 */
	private boolean printStarMicronicsByteArray(MobileDeviceInfo deviceInfo, String deviceAddress, byte[] printData) {
		StarIOPort port = null;
		try 
    	{
			port = StarIOPort.getPort("bt:" + deviceAddress, "mini", 10000);
			
			try
			{
				Thread.sleep(printDelay);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
        	int start = 0;
        	int chunk = 4096;
        	while (start < printData.length) {
        		int count = (start + chunk) < printData.length ? chunk : printData.length - start;
        		port.writePort(printData, start, count);
        		start += count;
    			try
    			{
    				Thread.sleep(printDelay);
    			}
    			catch(InterruptedException e) {
    				e.printStackTrace();
    			}
        	}
			
			try
			{
				int sleepTime = (int) (printData.length / 1024.0 * printPause);
				Thread.sleep(sleepTime);
				return true;
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}			
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
		}
		finally
		{
			if(port != null)
			{
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {}
			}
		}
		
		return false;
	}
	
	/**
	 * Print bytes creating a basic bluetooth connection
	 * @param deviceInfo Device to be connected to.
	 * @param deviceAddress Address of the device
	 * @param printData Data to be printed.
	 * @return true if printing was okey.
	 */
	private boolean printBytes(MobileDeviceInfo deviceInfo, String deviceAddress, byte[] printData) {
		boolean returnValue = false;
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		for (BluetoothDevice device : adapter.getBondedDevices()) {
			if (device.getAddress().equals(deviceAddress)) {
				Thread connectThread = new ConnectThread(device, UUID.randomUUID(), printData);
				connectThread.start();
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	/**
	 * Private class for handling connection.
	 * @author Federico Alcantara
	 *
	 */
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private byte[] printData;
	 
	    public ConnectThread(BluetoothDevice device, UUID uuid, byte[] printData) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        this.printData = printData;
	        
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	            tmp = (BluetoothSocket) m.invoke(device, 1);
	        } catch (Exception e) {
	        	Log.e(TAG, e.getMessage());
	        	e.printStackTrace();
	        	if (tmp != null) {
	        		try {
	        			tmp.close();
	        		} catch(IOException ex) {
	        			Log.d(TAG, ex.getMessage());
	        		}
	        	}
	        	tmp = null;
			}
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	    	if (mmSocket != null) {
		    	try {
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		            mmSocket.connect();
		        } catch (IOException connectException) {
		            // Unable to connect; close the socket and get out
		            try {
		                mmSocket.close();
		            } catch (IOException closeException) { 
		            	Log.d(TAG, closeException.getMessage());
		            }
		            return;
		        }
		 
		        // Do work to manage the connection (in a separate thread)
		        ConnectedThread connectedThread = new ConnectedThread(mmSocket, printData);
		        connectedThread.start();
	    	}
	    }
	 
	}

	private class ConnectedThread extends Thread {
		private BluetoothSocket socket;
	    private final OutputStream mmOutStream;
	    private byte[] printData;
	    
	    public ConnectedThread(BluetoothSocket socket, byte[] printData) {
	        this.socket = socket;
	    	this.printData = printData;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) {
	        	Log.e(TAG, e.getMessage());
	        }
	 
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        try {
	        	int start = 0;
	        	int chunk = 4096;
	        	while (start < printData.length) {
	        		int count = (start + chunk) < printData.length ? chunk : printData.length - start;
	        		mmOutStream.write(printData, start, count);
	        		start += count;
	    			try
	    			{
	    				Thread.sleep(printDelay);
	    			}
	    			catch(InterruptedException e) {
	    				e.printStackTrace();
	    			}
	        	}
	        } catch (IOException e) {
	        	Log.e(TAG, e.getMessage());
	        	e.printStackTrace();
	        } finally {
        		try {
    				int sleepTime = (int) (printData.length / 1024.0 * printPause);
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Log.e(TAG, e.getMessage());
				}
	        	try {
					mmOutStream.close();
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
	        	try {
					socket.close();
				} catch (IOException e) {
					Log.d(TAG, e.getMessage());
				}
	        }
	    }
	 
	}
}
