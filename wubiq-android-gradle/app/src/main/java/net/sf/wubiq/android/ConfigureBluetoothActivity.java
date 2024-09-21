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

/**
 * Allows configuration of bluetooth devices.
 * @author Federico Alcantara
 *
 */
public class ConfigureBluetoothActivity extends Activity {
	private static final int REQUEST_CODE = 2;

	private SharedPreferences preferences = null;

	/**
	 * Called upon activity creation.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_bluetooth);
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
		requestBluetooth();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == REQUEST_CODE) {
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

		if (hasBluetooth) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE);
				}
			} else {
				initialize();
			}
		}
	}
}
