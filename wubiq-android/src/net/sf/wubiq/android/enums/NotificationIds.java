/**
 * 
 */
package net.sf.wubiq.android.enums;

import net.sf.wubiq.android.CallBluetoothSettingsNotificationActivity;
import net.sf.wubiq.android.CancelConnectionNotificationActivity;
import net.sf.wubiq.android.CancelPrintingErrorNotificationActivity;
import net.sf.wubiq.android.CancelPrintingInfoNotificationActivity;
import android.content.Context;
import android.content.Intent;

/**
 * Different notifications.
 * @author Federico Alcantara
 *
 */
public enum NotificationIds {
	CONNECTION_ERROR_ID(true, false, "Error"), 
	BLUETOOTH_ERROR_ID(true, false, "Error"), 
	PRINTING_ERROR_ID(true, true, "Error"),
	PRINTING_INFO_ID(false, false, "Info");
	
	private boolean alarmOnOne;
	private boolean alarmOnGreaterThanOne;
	private String title;
	
	private NotificationIds(boolean alarmOnOne, boolean alarmOnGreaterThanOne, String title) {
		this.alarmOnOne = alarmOnOne;
		this.alarmOnGreaterThanOne = alarmOnGreaterThanOne;
		this.title = title;
	}
	
	/**
	 * Creates an appropriate intent for the id.
	 * @param ctx Context in which to create the intent.
	 * @return Intent object. Never null.
	 */
	public Intent getIntent(Context ctx) {
		Intent returnValue = null;
		
		switch (this) {
			case BLUETOOTH_ERROR_ID:
				returnValue = new Intent(ctx,  CallBluetoothSettingsNotificationActivity.class);
				break;
			case PRINTING_ERROR_ID:
				returnValue = new Intent(ctx,  CancelPrintingErrorNotificationActivity.class);
				break;				
			case PRINTING_INFO_ID:
				returnValue = new Intent(ctx,  CancelPrintingInfoNotificationActivity.class);
				break;
			default :
				returnValue = new Intent(ctx,  CancelConnectionNotificationActivity.class);
				break;
		}
		
		return returnValue;
	}
	
	/**
	 * Determines if the notification should trigger an alarm or not.
	 * @param number Number of notification of the same type issued
	 * @return True if an alarm should be triggered, false otherwise.
	 */
	public boolean triggerAlarm(int number) {
		return number <= 1 ? alarmOnOne : alarmOnGreaterThanOne; 
	}
	
	/**
	 * The title for the notification.
	 * @return String containing the title.
	 */
	public String title() {
		return title;
	}
}
