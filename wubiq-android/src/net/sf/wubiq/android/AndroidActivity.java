package net.sf.wubiq.android;

import java.lang.Thread.State;

import net.sf.wubiq.clients.BluetoothPrintManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class AndroidActivity extends Activity {
	public static final String PREFERENCES = "WUBIQ_ANDROID";
	public static final String HOST_KEY="server_host";
	public static final String PORT_KEY="server_port";
	public static final String UUID_KEY="client_uuid";
	public static final String DEVICE_PREFIX = "wubiq-android-bt_";
	private Thread managerThread;
	SharedPreferences preferences;
	private Handler timerHandler = new Handler();
	private Runnable timerRunnable = new Runnable() {
		public void run() {
			checkPrintManagerStatus();
			timerHandler.postDelayed(this, 15000);
		}
		
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		preferences = getSharedPreferences(AndroidActivity.PREFERENCES, MODE_PRIVATE);
		startPrintManager();
    }
    
    public void configureServer(View view) {
    	Intent intent = new Intent(this, ConfigureServerActivity.class);
    	startActivity(intent);
    }

    public void configureBluetooth(View view) {
    	Intent intent = new Intent(this, ConfigureBluetoothActivity.class);
    	startActivity(intent);
    }
    
    public void checkPrintManagerStatus(View view) {
    	checkPrintManagerStatus();
    }
    
    public void checkPrintManagerStatus() {
		TextView status = (TextView) findViewById(R.id.managerStatusField);
    	if (managerThread.getState().equals(State.TERMINATED)) {
    		status.setText(R.string.status_disconnected);
    	} else {
    		status.setText(R.string.status_connected);
    	}
    }
    
    public void reconnectManager(View view) {
    	if (managerThread.getState().equals(State.TERMINATED)) {
    		startPrintManager();
    	}
    }
    
    private void startPrintManager() {
		BluetoothPrintManager manager = new BluetoothPrintManager(this, preferences);
        managerThread = new Thread(manager);
        managerThread.start();
		TextView status = (TextView) findViewById(R.id.managerStatusField);
		status.setText(R.string.status_pending);
		startTimer();
    }
    
    private void startTimer() {
    	timerHandler.removeCallbacks(timerRunnable);
    	timerHandler.postDelayed(timerRunnable, 5000);
    }
}