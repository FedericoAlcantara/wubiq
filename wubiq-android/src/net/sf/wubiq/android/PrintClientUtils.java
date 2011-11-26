/**
 * 
 */
package net.sf.wubiq.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.wubiq.common.ParameterKeys;
import android.content.Context;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

/**
 * Handles the necessary steps for printing on the client.
 * @author Federico Alcantara
 *
 */
public enum PrintClientUtils {
	INSTANCE;
	
	/**
	 * Prints the given input to the device, performing all required conversion steps.
	 * @param context Android context.
	 * @param deviceName Complete device name.
	 * @param input Input data as a stream
	 */
	public void print(Context context, String deviceName, InputStream input) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String[] deviceData = deviceName.split(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
		MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(deviceData[2]);
		String deviceAddress = deviceData[1];
		try {
			input.reset();
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
			byte[] printData = output.toByteArray();
			for (MobileClientConversionStep step : deviceInfo.getClientSteps()) {
				if (step.equals(MobileClientConversionStep.OUTPUT_SM_BYTES)) {
					printStarMicronicsByteArray(deviceInfo, deviceAddress, printData);
				}
			}
		} catch (IOException e) {
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
				Thread.sleep(500);
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
			port.writePort(printData, 0, printData.length);
			try
			{
				Thread.sleep(3000);
				return true;
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}			
		}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    		/*
    		Builder dialog = new AlertDialog.Builder(context);
    		dialog.setNegativeButton("Ok", null);
    		AlertDialog alert = dialog.create();
    		alert.setTitle("Failure");
    		alert.setMessage("Failed to connect to printer");
    		alert.show();
    		*/
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
}
