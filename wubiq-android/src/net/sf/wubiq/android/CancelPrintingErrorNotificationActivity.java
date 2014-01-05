/**
 * 
 */
package net.sf.wubiq.android;

import net.sf.wubiq.android.enums.NotificationIds;

/**
 * Cancels printing error notification.
 * @author Federico Alcantara
 *
 */
public class CancelPrintingErrorNotificationActivity extends CancelNotificationBaseActivity {
	public CancelPrintingErrorNotificationActivity() {
		super(NotificationIds.PRINTING_ERROR_ID);
	}
}
