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
public class DeviceAdapter extends BaseAdapter {
	Context context;
	int deviceCount;
	List<TextView> texts = new ArrayList<TextView>();
	List<Spinner> spinners = new ArrayList<Spinner>();
	
	public DeviceAdapter(Context context) {
		super();
		this.context = context;
		Set<BluetoothDevice> devices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
		for (BluetoothDevice device : devices) {
			TextView deviceName = new TextView(context);
			deviceName.setText(device.getName() + " " + device.getName());
			texts.add(deviceName);
			
			Spinner spinner = new Spinner(context);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.drivers, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinners.add(spinner);
		}
		this.deviceCount = devices.size();
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
		if (position % 2 > 0) {
			return spinners.get(new Integer(position / 2) + 1);
		} else {
			return texts.get(new Integer(position / 2));
		}
	}

}