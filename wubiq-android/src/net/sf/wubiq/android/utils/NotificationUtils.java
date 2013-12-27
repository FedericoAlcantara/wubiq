/**
 * 
 */
package net.sf.wubiq.android.utils;

import net.sf.wubiq.android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


/**
 * Handle notifications
 * @author Federico Alcantara
 *
 */
public enum NotificationUtils {
	INSTANCE;
	
	private int EXCEPTION_NOTIFICATION_ID = 123;
	
	public void notifyException(Context ctx, Throwable e) {
		Notification notification = new Notification(
				R.drawable.ic_notification, 
				e.getMessage(), 
				System.currentTimeMillis());
		NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(EXCEPTION_NOTIFICATION_ID, notification);
	}
}
