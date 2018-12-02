package net.sf.wubiq.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
    private static final String TAG = WubiqActivity.class.getSimpleName();

    public static final String PREFERENCES = "WUBIQ_ANDROID";
	public static final String HOST_KEY="server_host";
	public static final String PORT_KEY="server_port";
	public static final String CONNECTIONS_KEY="server_connections";
	public static final String PRINT_DELAY_KEY="print_delay";
	public static final String PRINT_PAUSE_KEY="print_pause";
	public static final String PRINT_POLL_INTERVAL_KEY="print_poll_interval";
	public static final String PRINT_PAUSE_BETWEEN_JOBS_KEY="print_pause_between_jobs";
	public static final String PRINT_CONNECTION_ERRORS_RETRY_KEY="print_error_retry";
	public static final String UUID_KEY="client_uuid";
	public static final String GROUPS_KEY="groups";
	public static final String DEVICE_PREFIX = "wubiq-android-bt_";
	public static final String SUPPRESS_NOTIFICATIONS_KEY="suppress_notifications";
	public static final String ENABLE_DEVELOPMENT_MODE="enable_development_mode";
	public static final String STOP_SERVICE_STATUS="stop_wubi_service";

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			Toast toast = Toast.makeText(WubiqActivity.this, R.string.service_started, Toast.LENGTH_SHORT);
			toast.show();
		}

		public void onServiceDisconnected(ComponentName name) {
		}
		
	};

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);		

        setContentView(R.layout.main);
        LinearLayout versionLayout = findViewById(R.id.versionLayout);
		TextView version = versionLayout.findViewById(R.id.version);
        Resources resources = getResources();
		String versionTitle = getVersion(this, resources);
		version.setText(versionTitle);
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
		Intent printManagerIntent = new Intent(this, PrintManagerService.class);
		PrintManagerService.enqueueWork(this, PrintManagerService.class, PrintManagerService.JOB_ID, printManagerIntent);
    }
    
    /**
     * Stop print services. 
     * @param view Calling view object.
     */
    public void stopService(View view) {
        SharedPreferences preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WubiqActivity.STOP_SERVICE_STATUS, true);
        editor.commit();
    }

    public static String getVersion(Context context, Resources resources) {
        PackageInfo pInfo;
        String packageName="net.sf.wubiq/android";
        try {
            pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return resources.getString(R.string.wubiq_version_title) + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

}