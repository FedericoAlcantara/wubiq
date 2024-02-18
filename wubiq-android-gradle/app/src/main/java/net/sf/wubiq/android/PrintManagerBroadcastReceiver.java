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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
				Intent printManagerIntent = new Intent(context, PrintManagerService.class);
				PrintManagerService.enqueueWork(context, PrintManagerService.class, PrintManagerService.JOB_ID, printManagerIntent);
			} else {
                Intent printManagerIntent = new Intent(context, PrintManagerServiceV7.class);
                context.startService(printManagerIntent);
            }
		}
	}

}
