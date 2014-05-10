/**
 * 
 */
package net.sf.wubiq.android;

import net.sf.wubiq.android.enums.NotificationIds;
import net.sf.wubiq.android.utils.NotificationUtils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author Federico Alcantara
 *
 */
public class CallBluetoothSettingsNotificationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationUtils.INSTANCE.cancelNotification(getApplicationContext(), NotificationIds.BLUETOOTH_ERROR_ID);
		Intent bluetoothSettings = new Intent();
		bluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
		startActivity(bluetoothSettings);
		finish();
	}

}
