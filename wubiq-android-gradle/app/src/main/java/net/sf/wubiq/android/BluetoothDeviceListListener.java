/**
 * 
 */
package net.sf.wubiq.android;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Sets the driver that will handle the device printing.
 * @author Federico Alcantara
 *
 */
public class BluetoothDeviceListListener implements OnItemSelectedListener {
	String deviceKey;
	SharedPreferences preferences;
	
	public BluetoothDeviceListListener(SharedPreferences savedInstanceState, String deviceKey) {
		this.preferences = savedInstanceState;
		this.deviceKey = deviceKey;
	}
	
	/**
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(deviceKey, parent.getItemAtPosition(pos).toString());
		editor.commit();
	}

	/**
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(deviceKey);
		editor.commit();
	}

}
