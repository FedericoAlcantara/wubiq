/**
 * 
 */
package net.sf.wubiq.android.utils;

import net.sf.wubiq.android.R;
import net.sf.wubiq.android.WubiqActivity;
import net.sf.wubiq.android.enums.NotificationIds;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


/**
 * Handle notifications
 * @author Federico Alcantara
 *
 */
public enum NotificationUtils {
	INSTANCE;
	private final String TAG = NotificationUtils.class.getSimpleName();
	
	/**
	 * Cancels previous notifications.
	 * @param ctx Context.
	 * @param notificationId Notification type identification.
	 */
	public void cancelNotification(Context ctx, NotificationIds notificationId) {
		NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(notificationId.ordinal());
	}

	/**
	 * Notifies to the status bar.
	 * @param ctx Context
	 * @param notificationId Notification type identification.
	 * @param number A number of notifications of the same type.
	 * @param errorMessage Message to show in the status bar.
	 */
	public void notify(Context ctx, NotificationIds notificationId, int number, String errorMessage) {
		SharedPreferences preferences = ctx.getSharedPreferences(WubiqActivity.PREFERENCES, Context.MODE_PRIVATE);
		boolean suppressNotifications = preferences.getBoolean(WubiqActivity.SUPPRESS_NOTIFICATIONS_KEY, false);
		if (!suppressNotifications) {
			try {
				Notification notification = new Notification(
						R.drawable.ic_notification, 
						errorMessage, 
						System.currentTimeMillis());
				if (notificationId.triggerAlarm(number)) {
					long[] vibrate = { 100, 100, 200, 300 };
					notification.vibrate = vibrate;
			        notification.ledARGB = 0xff00ff00;
			        notification.ledOnMS = 300;
			        notification.ledOffMS = 1000;
			        notification.defaults = Notification.DEFAULT_ALL;
				}
		        if (number > 0) {
		        	notification.number = number;
		        }
//		        CharSequence contentTitle = notificationId.title();
//		        CharSequence contentText = errorMessage;
//		        Intent notifyIntent = notificationId.getIntent(ctx);
//		        PendingIntent intent = PendingIntent.getActivity(ctx, 0,notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
//		        notification.setLatestEventInfo(ctx, contentTitle, contentText, intent);
				NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(notificationId.ordinal(), notification);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
}
