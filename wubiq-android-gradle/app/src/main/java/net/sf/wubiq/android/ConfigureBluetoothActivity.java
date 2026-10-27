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
import android.widget.ListView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.sf.wubiq.android.utils.BluetoothUtils;
import net.sf.wubiq.android.utils.WindowUtils;

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
		WindowUtils.applySystemBarsInsets(this);
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
			if (BluetoothUtils.allGranted(grantResults)) {
				initialize();
			}
		}
	}

	/**
	 * Loads view with new or previously stored preferences.
	 */
	private void initialize() {
		SwitchCompat showAll = (SwitchCompat) findViewById(R.id.showAllDevices);
		showAll.setOnCheckedChangeListener(null);
		showAll.setChecked(preferences.getBoolean(WubiqActivity.SHOW_ALL_BLUETOOTH_DEVICES, false));
		showAll.setOnCheckedChangeListener((button, checked) -> {
			preferences.edit().putBoolean(WubiqActivity.SHOW_ALL_BLUETOOTH_DEVICES, checked).apply();
			initialize();
		});
		ListView list = (ListView) findViewById(R.id.devices);
		list.setAdapter(new BluetoothDeviceListAdapter(this, preferences, showAll.isChecked()));
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
