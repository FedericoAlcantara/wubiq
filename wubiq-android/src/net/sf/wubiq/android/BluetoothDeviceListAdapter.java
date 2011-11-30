/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
 * @author Federico Alcantara
 *
 */
public class BluetoothDeviceListAdapter extends BaseAdapter {
	private final String DEVICE_PREFIX = "wubiq-android-bt_";
	int deviceCount;
	List<TextView> texts = new ArrayList<TextView>();
	List<Spinner> spinners = new ArrayList<Spinner>();
	
	public BluetoothDeviceListAdapter(Context context, SharedPreferences preferences) {
		super();
		int minimumHeight = 50;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter != null) {
			Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
			for (BluetoothDevice device : devices) {
				String deviceKey = DEVICE_PREFIX + device.getAddress();
				TextView deviceName = new TextView(context);
				deviceName.setText(device.getName() + " " + device.getAddress());
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
				spinner.setOnItemSelectedListener(new BluetoothDeviceListListener(preferences, deviceKey));
				spinners.add(spinner);
			}
			this.deviceCount = devices.size();
		} else {
			this.deviceCount = 0;
		}
	}
	
	/*
	 * @see android.widget.Adapter#getCount()
	 */
	public int getCount() {
		return deviceCount * 2;
	}

	/**
	 * @see android.widget.Adapter#getItem(int)
	 */
	public Object getItem(int position) {
		return null;
	}

	/**
	 * @see android.widget.Adapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return 0;
	}

	/**
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
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
