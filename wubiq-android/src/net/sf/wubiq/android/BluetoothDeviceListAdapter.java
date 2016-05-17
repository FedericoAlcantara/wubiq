/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.common.PropertyKeys;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Creates the bluetooth list of paired devices.
 * @author Federico Alcantara
 *
 */
public class BluetoothDeviceListAdapter extends BaseAdapter {
	private final String DEVICE_PREFIX = "wubiq-android-bt_";
	
	int deviceCount;
	List<TextView> texts = new ArrayList<TextView>();
	List<Spinner> spinners = new ArrayList<Spinner>();
	Context context;
	SharedPreferences preferences;
	
	/**
	 * Constructor.
	 * @param context Context running the application.
	 * @param preferences Application preferences.
	 */
	public BluetoothDeviceListAdapter(Context context, SharedPreferences preferences) {
		super();
		this.context = context;
		this.preferences = preferences;
		this.deviceCount = 0;
		if (preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false)) {
			for (int index = 1; index <= DeviceForTesting.TEST_DEVICE_COUNT; index++) {
				String suffix = index != 1 ? "_" + index : "";
				Spinner spinner = addDevice(DeviceForTesting.TEST_DEVICE_NAME + suffix, DeviceForTesting.TEST_DEVICE_ADDRESS + suffix);
				spinner.setOnItemSelectedListener(new BluetoothDeviceListListener(preferences, key(DeviceForTesting.TEST_DEVICE_ADDRESS + suffix)));
				this.deviceCount = this.deviceCount + 1;
			}
		}
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null) {
			Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
			for (BluetoothDevice device : devices) {
				String deviceKey = key(device.getAddress());
				Spinner spinner = addDevice(device.getName(), device.getAddress());
				spinner.setOnItemSelectedListener(new BluetoothDeviceListListener(preferences, deviceKey));
			}
			this.deviceCount += devices.size();
		}
	}
	
	/**
	 * Adds a device to the list of available devices.
	 * @param name Name of the device
	 * @param address Address.
	 * @param deviceKey Device key.
	 * @return Created spinner for device configuration.
	 */
	private Spinner addDevice(String name, String address) {
		String deviceKey = key(address);
		int minimumHeight = 50;
		TextView deviceName = new TextView(context);
		deviceName.setText(name + " " + address);
		deviceName.setHeight(minimumHeight);
		deviceName.setTextAppearance(context, android.R.attr.textAppearanceMedium);
		texts.add(deviceName);

		Spinner spinner = new Spinner(context);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, MobileDevices.INSTANCE.getDeviceNames());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setMinimumHeight(minimumHeight);
		spinner.setPromptId(R.string.select_driver);
		String selection = preferences.getString(deviceKey, "--");
		int index = MobileDevices.INSTANCE.getDeviceNames().indexOf(selection);
		if (index > -1) {
			spinner.setSelection(index);
		}
		spinners.add(spinner);
		return spinner;
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
		return deviceCount * 2;
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
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View returnValue;
		int index = position / 2;
		if (position % 2 > 0) {
			returnValue = spinners.get(index);
		} else {
			returnValue = texts.get(index);
		}
		return returnValue;
	}

}
