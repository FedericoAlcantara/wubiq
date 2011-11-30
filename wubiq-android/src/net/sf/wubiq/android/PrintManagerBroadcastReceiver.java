/**
 * 
 */
package net.sf.wubiq.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for ensuring boot up service initialization.
 * @author Federico Alcantara
 *
 */
public class PrintManagerBroadcastReceiver extends BroadcastReceiver {

	/**
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent printManagerIntent = new Intent(context, PrintManagerService.class);
		context.startService(printManagerIntent);
	}

}
