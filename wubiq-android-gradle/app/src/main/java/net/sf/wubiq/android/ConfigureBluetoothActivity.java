/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.GridView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.sf.wubiq.android.utils.BluetoothUtils;

/**
 * Allows configuration of bluetooth devices.
 * @author Federico Alcantara
 *
 */
public class ConfigureBluetoothActivity extends Activity {
	private SharedPreferences preferences = null;

	/**
	 * Called upon activity creation.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_bluetooth);
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);

		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(WubiqActivity.PAUSE_PRINTING_TO_DEVICES, true);
		editor.apply();

		requestBluetooth();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(WubiqActivity.PAUSE_PRINTING_TO_DEVICES, false);
		editor.apply();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == BluetoothUtils.REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				initialize();
			}
		}
	}

	/**
	 * Loads view with new or previously stored preferences.
	 */
	private void initialize() {
		GridView grid = (GridView) findViewById(R.id.devices);
		grid.setAdapter(new BluetoothDeviceListAdapter(this, preferences));
	}

	private void requestBluetooth() {
		boolean hasBluetooth = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
		boolean initialize = false;
		if (hasBluetooth) {
			initialize = BluetoothUtils.bluetoothGranted(this);
		}
		if (initialize) {
			initialize();
		}
	}
}
