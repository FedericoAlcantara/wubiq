/**
 * 
 */
package net.sf.wubiq.android.devices;

import android.os.SystemClock;
import android.util.Log;

import com.starmicronics.stario.StarIOPort;
import com.starmicronics.stario.StarIOPortException;
import com.starmicronics.stario.StarPrinterStatus;

/**
 * Implements Wubiq Device mechanism.
 * @author Federico Alcantara
 *
 */
public class DeviceStarMicronics extends BaseWubiqDevice {
	private static final String TAG = DeviceStarMicronics.class.getSimpleName();
	private static final String LOG_TAG = "WubiqStar";
	private static final int CHECKED_BLOCK_TIMEOUT = 60000;
	
	/**
	 * Outputs to a star micronics portable printer. Every copy is sent inside a checked block, which returns
	 * once the printer reports it has printed everything, and the pause between copies starts from there.
	 */
	protected boolean print() {
		boolean returnValue = false;
		StarIOPort port = null;
		try 
    	{
			port = StarIOPort.getPort("bt:" + getDeviceAddress(), "mini", 10000);
			port.setEndCheckedBlockTimeoutMillis(CHECKED_BLOCK_TIMEOUT);
			for (int copy = 0; copy < getCopies(); copy++) {
				if (copy > 0) {
					waitBetweenCopies();
				}
				printCopy(port, copy + 1);
			}
			returnValue = true;
		}
    	catch (Exception e)
    	{
    		Log.e(LOG_TAG, "Printing failed: " + e.getMessage(), e);
		}
		finally
		{
			if(port != null)
			{
				try {
					StarIOPort.releasePort(port);
				} catch (StarIOPortException e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		return returnValue;
	}

	/**
	 * Sends one copy and waits until the printer says it has printed it.
	 * @param port Open port.
	 * @param number Number of the copy, for logging.
	 */
	private void printCopy(StarIOPort port, int number) throws Exception {
		long start = SystemClock.elapsedRealtime();
		StarPrinterStatus status = port.beginCheckedBlock();
		if (status.offline) {
			throw new StarIOPortException("Printer is offline: " + describe(status));
		}
		Thread.sleep(getPrintDelay());
		port.writePort(getPrintData(), 0, getPrintData().length);
		long sent = SystemClock.elapsedRealtime();
		status = port.endCheckedBlock();
		long printed = SystemClock.elapsedRealtime();
		Log.i(LOG_TAG, "copy " + number + ": sent after " + (sent - start) + " ms, printed after "
				+ (printed - start) + " ms (waited " + (printed - sent) + " ms for the printer), " + describe(status));
		if (status.offline) {
			throw new StarIOPortException("Printer went offline: " + describe(status));
		}
	}

	private String describe(StarPrinterStatus status) {
		return "offline=" + status.offline + " coverOpen=" + status.coverOpen
				+ " paperEmpty=" + status.receiptPaperEmpty + " etbAvailable=" + status.etbAvailable;
	}

}
