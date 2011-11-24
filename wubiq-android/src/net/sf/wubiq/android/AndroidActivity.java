package net.sf.wubiq.android;

import net.sf.wubiq.clients.BluetoothPrintManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class AndroidActivity extends Activity {
	public static final String PREFERENCES = "WUBIQ_ANDROID";
	public static final String HOST_KEY="server_host";
	public static final String PORT_KEY="server_port";
	public static final String UUID_KEY="client_uuid";
	public static final String DEVICE_PREFIX = "wubiq-android-bt_";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		SharedPreferences preferences = getSharedPreferences(AndroidActivity.PREFERENCES, MODE_PRIVATE);
        BluetoothPrintManager manager = new BluetoothPrintManager(this, preferences);
        Thread t = new Thread(manager);
        t.start();
    }
    
    public void configureServer(View view) {
    	Intent intent = new Intent(this, ConfigureServerActivity.class);
    	startActivity(intent);
    }

    public void configureBluetooth(View view) {
    	Intent intent = new Intent(this, ConfigureBluetoothActivity.class);
    	startActivity(intent);
    }
    
}