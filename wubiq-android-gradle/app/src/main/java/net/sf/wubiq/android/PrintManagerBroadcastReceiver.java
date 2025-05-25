/**
 * 
 */
package net.sf.wubiq.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Receiver for ensuring boot up service initialization.
 * @author Federico Alcantara
 *
 */
public class PrintManagerBroadcastReceiver extends BroadcastReceiver {

	/**
	 * @see BroadcastReceiver#onReceive(Context, Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			PrintManagerService.startService(context);
		}
	}

}
