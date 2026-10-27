/**
 *
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.Manifest;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.utils.BluetoothUtils;
import net.sf.wubiq.common.PropertyKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates the bluetooth list of paired devices.
 * @author Federico Alcantara
 *
 */
public class BluetoothDeviceListAdapter extends BaseAdapter {
	private final String DEVICE_PREFIX = "wubiq-android-bt_";

	List<View> rows = new ArrayList<View>();
	Context context;
	SharedPreferences preferences;

	/**
	 * Constructor.
	 * @param context Context running the application.
	 * @param preferences Application preferences.
	 * @param showAll If false, devices that are clearly not printers are hidden.
	 */
	public BluetoothDeviceListAdapter(Context context, SharedPreferences preferences, boolean showAll) {
		super();
		this.context = context;
		this.preferences = preferences;
		if (preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false)
				|| preferences.getBoolean(WubiqActivity.ENABLE_DEVELOPMENT_MODE, false)) {
			for (int index = 1; index <= DeviceForTesting.TEST_DEVICE_COUNT; index++) {
				String suffix = index != 1 ? "_" + index : "";
				Spinner spinner = addDevice(MobileDevices.TEST_DEVICE_NAME + suffix, DeviceForTesting.TEST_DEVICE_ADDRESS + suffix);
				spinner.setOnItemSelectedListener(new BluetoothDeviceListListener(preferences, key(DeviceForTesting.TEST_DEVICE_ADDRESS + suffix)));
			}
		}
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null) {
			if (BluetoothUtils.bluetoothGranted((Activity) context)) {
				Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
				for (BluetoothDevice device : devices) {
					String deviceKey = key(device.getAddress());
					boolean configured = !"--".equals(preferences.getString(deviceKey, "--"));
					boolean possiblePrinter = BluetoothUtils.isPossiblePrinter(device);
					if (!showAll && !configured && !possiblePrinter) {
						continue;
					}
					Spinner spinner = addDevice(device.getName(), device.getAddress());
					spinner.setOnItemSelectedListener(new BluetoothDeviceListListener(preferences, deviceKey));
				}
			}
		}
	}
	
	/**
	 * Adds a device to the list of available devices.
	 * @param name Name of the device
	 * @param address Address.
	 * @return Created spinner for device configuration.
	 */
	private Spinner addDevice(String name, String address) {
		String deviceKey = key(address);
		LinearLayout row = new LinearLayout(context);
		row.setOrientation(LinearLayout.VERTICAL);
		row.setPadding(0, dp(12), 0, dp(12));

		TextView deviceName = new TextView(context);
		deviceName.setText(name);
		deviceName.setTextAppearance(android.R.style.TextAppearance_Medium);
		row.addView(deviceName);

		TextView deviceAddress = new TextView(context);
		deviceAddress.setText(address);
		deviceAddress.setTextAppearance(android.R.style.TextAppearance_Small);
		row.addView(deviceAddress);

		Spinner spinner = new Spinner(context);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, MobileDevices.INSTANCE.getDeviceNames());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setMinimumHeight(dp(48));
		spinner.setPromptId(R.string.select_driver);
		String selection = preferences.getString(deviceKey, "--");
		int index = MobileDevices.INSTANCE.getDeviceNames().indexOf(selection);
		if (index > -1) {
			spinner.setSelection(index);
		}
		row.addView(spinner, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		rows.add(row);
		return spinner;
	}

	/**
	 * Converts density independent pixels to pixels.
	 * @param value Value in dp.
	 * @return Value in pixels.
	 */
	private int dp(int value) {
		return Math.round(value * context.getResources().getDisplayMetrics().density);
	}
	
	/**
	 * Forms the default address.
	 * @param address Address to form the key.
	 * @return Created key.
	 */
	private String key(String address) {
		return DEVICE_PREFIX + address;
	}
	
	/**
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return rows.size();
	}

	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/**
	 * @see android.widget.Adapter#getView(int, View, ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return rows.get(position);
	}

}
