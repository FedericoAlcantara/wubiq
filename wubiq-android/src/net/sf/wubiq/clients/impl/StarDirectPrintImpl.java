/**
 * 
 */
package net.sf.wubiq.clients.impl;

import java.io.InputStream;

import net.sf.wubiq.clients.IMobileDirectPrint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;

/**
 * @author Federico Alcantara
 *
 */
public class StarDirectPrintImpl implements IMobileDirectPrint {

	private Context context;
	
	public StarDirectPrintImpl(Context context) {
		this.context = context;
	}
	
	/**
	 * @see net.sf.wubiq.clients.IMobileDirectPrint#printBmpImage(byte[])
	 */
	public void printBmpImage(String portName, InputStream input) {
		Bitmap source = BitmapFactory.decodeStream(input);
    	int maxWidth = Integer.MAX_VALUE;
    	StarBitmap starbitmap = new StarBitmap(source, false, maxWidth);
		byte[] inputBmp = starbitmap.getImageEscPosDataForPrinting();
		StarIOPort port = null;
		try 
    	{
			port = StarIOPort.getPort("bt:" + portName, "mini", 10000);
			
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e) {}
			port.writePort(inputBmp, 0, inputBmp.length);
			
			try
			{
				Thread.sleep(3000);
			}
			catch(InterruptedException e) {}			
		}
    	catch (Exception e)
    	{
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
		

	}

}
