/**
 * 
 */
package net.sf.wubiq.android;

import net.sf.wubiq.android.enums.NotificationIds;

/**
 * Cancels connection notification activity.
 * @author Federico Alcantara
 *
 */
public class CancelConnectionNotificationActivity extends CancelNotificationBaseActivity {
	public CancelConnectionNotificationActivity() {
		super(NotificationIds.CONNECTION_ERROR_ID);
	}
}
