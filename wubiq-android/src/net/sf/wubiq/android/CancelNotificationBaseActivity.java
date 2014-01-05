package net.sf.wubiq.android;

import net.sf.wubiq.android.enums.NotificationIds;
import net.sf.wubiq.android.utils.NotificationUtils;
import android.app.Activity;
import android.os.Bundle;

/**
 * Cancels a given notification type.
 * @author Federico Alcantara
 *
 */
public class CancelNotificationBaseActivity extends Activity {
	private NotificationIds notificationId;
	
	public CancelNotificationBaseActivity(NotificationIds notificationId) {
		this.notificationId = notificationId;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationUtils.INSTANCE.cancelNotification(getApplicationContext(), notificationId);
		finish();
	}
}
