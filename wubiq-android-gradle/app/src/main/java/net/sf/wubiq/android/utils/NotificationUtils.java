/**
 * 
 */
package net.sf.wubiq.android.utils;

import net.sf.wubiq.android.R;
import net.sf.wubiq.android.WubiqActivity;
import net.sf.wubiq.android.enums.NotificationIds;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


/**
 * Handle notifications
 * @author Federico Alcantara
 *
 */
public enum NotificationUtils {
	INSTANCE;
	private final String TAG = NotificationUtils.class.getSimpleName();
	private final static String CHANNEL_ID = "net.sf.wubiq.android";
	
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
//		boolean suppressNotifications = preferences.getBoolean(WubiqActivity.SUPPRESS_NOTIFICATIONS_KEY, false);
//		if (!suppressNotifications) {
			try {
			    createNotificationChannel(ctx);
                Intent notifyIntent = notificationId.getIntent(ctx);
                PendingIntent intent = PendingIntent.getActivity(ctx, 0,notifyIntent, PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(notificationId.title())
                        .setContentIntent(intent)
                        .setContentText(errorMessage)
                        .setDefaults(NotificationCompat.DEFAULT_ALL);

//				Notification notification = new Notification(
//						R.drawable.ic_notification,
//						errorMessage,
//						System.currentTimeMillis());
//				if (notificationId.triggerAlarm(number)) {
//					long[] vibrate = { 100, 100, 200, 300 };
//					notification.vibrate = vibrate;
//			        notification.ledARGB = 0xff00ff00;
//			        notification.ledOnMS = 300;
//			        notification.ledOffMS = 1000;
//			        notification.defaults = Notification.DEFAULT_ALL;
//				}
		        if (number > 0) {
		            mBuilder.setNumber(number);
		        }
				NotificationManager manager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(notificationId.ordinal(), mBuilder.build());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
//		}
	}

    private void createNotificationChannel(Context ctx) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_name);
            String description = ctx.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
