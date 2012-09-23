package net.sf.wubiq.android;

import net.sf.wubiq.utils.Labels;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity.
 * @author Federico Alcantara
 *
 */
public class WubiqActivity extends Activity {
	public static final String PREFERENCES = "WUBIQ_ANDROID";
	public static final String HOST_KEY="server_host";
	public static final String PORT_KEY="server_port";
	public static final String PRINT_DELAY_KEY="print_delay";
	public static final String PRINT_PAUSE_KEY="print_pause";
	public static final String PRINT_POLL_INTERVAL_KEY="print_poll_interval";
	public static final String PRINT_PAUSE_BETWEEN_JOBS_KEY="print_pause_between_jobs";
	public static final String PRINT_CONNECTION_ERRORS_RETRY_KEY="print_error_retry";
	public static final String UUID_KEY="client_uuid";
	public static final String DEVICE_PREFIX = "wubiq-android-bt_";
	
	@SuppressWarnings("unused")
	private PrintManagerService printManagerService;
	private boolean printManagerServiceBound = false;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			printManagerService = ((PrintManagerService.PrintManagerBinder)binder).getService();
			Toast toast = Toast.makeText(WubiqActivity.this, R.string.service_started, Toast.LENGTH_SHORT);
			toast.show();
		}

		public void onServiceDisconnected(ComponentName name) {
			printManagerService = null;
		}
		
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);		

        setContentView(R.layout.main);
        LinearLayout versionLayout = (LinearLayout) findViewById(R.id.versionLayout);
		TextView version = (TextView) versionLayout.findViewById(R.id.version);
		Resources resources = getResources();
		version.setText(resources.getString(R.string.wubiq_version_title) + Labels.VERSION);
    }
    
    /**
     * Called when configure server is selected.
     * @param view Calling view object.
     */
    public void configureServer(View view) {
    	Intent intent = new Intent(this, ConfigureServerActivity.class);
    	startActivity(intent);
    }

    /**
     * Invokes bluetooth devices configuration.
     * @param view Calling view object.
     */
    public void configureBluetooth(View view) {
    	Intent intent = new Intent(this, ConfigureBluetoothActivity.class);
    	startActivity(intent);
    }
    
    public void advancedConfiguration(View view) {
    	Intent intent = new Intent(this, AdvancedConfigurationActivity.class);
    	startActivity(intent);
    }

    /**
     * Start print services.
     * @param view Calling view object.
     */
    public void startService(View view) {
		bindService(new Intent(this, PrintManagerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		printManagerServiceBound = true;
    }
    
    /**
     * Stop print services. 
     * @param view Calling view object.
     */
    public void stopService(View view) {
    	if (printManagerServiceBound) {
    		unbindService(serviceConnection);
    		printManagerServiceBound = false;
    	}
    }
}