/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.GridView;

/**
 * Allows configuration of bluetooth devices.
 * @author Federico Alcantara
 *
 */
public class ConfigureBluetoothActivity extends Activity {
	
	/**
	 * Called upon activity creation.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_bluetooth);
		SharedPreferences preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
		initialize(preferences);
	}
	
	/**
	 * Loads view with new or previously stored preferences.
	 * @param preferences Application preferences.
	 */
	private void initialize(SharedPreferences preferences) {
		GridView grid = (GridView) findViewById(R.id.devices);
		grid.setAdapter(new BluetoothDeviceListAdapter(this, preferences));
	}
}
