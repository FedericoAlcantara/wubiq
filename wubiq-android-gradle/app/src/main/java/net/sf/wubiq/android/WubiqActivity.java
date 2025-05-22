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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.wubiq.android.devices.DeviceForTesting;

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
    public static final String ANDROID_TEST_DEVICE_KEY = DEVICE_PREFIX + DeviceForTesting.TEST_DEVICE_ADDRESS;
	public static final String ENABLE_DEVELOPMENT_MODE="enable_development_mode";
	public static final String STOP_SERVICE_STATUS="stop_wubiq_service";
	public static final String PACKAGE_NAME="net.sf.wubiq.android";
	private final ServiceConnection serviceConnection = new ServiceConnection() {

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
		String versionTitle = getVersionTitle(this, resources);
		version.setText(versionTitle);

        setAndroidTestDeviceKey(this);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Intent printManagerIntent = new Intent(this, PrintManagerService.class);
            PrintManagerService.enqueueWork(this, PrintManagerService.class, PrintManagerService.JOB_ID, printManagerIntent);
        } else {
            bindService(new Intent(this, PrintManagerServiceV7.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }
    
    /**
     * Stop print services. 
     * @param view Calling view object.
     */
    public void stopService(View view) {
        SharedPreferences preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WubiqActivity.STOP_SERVICE_STATUS, true);
        editor.apply();
    }

    /**
     * Get version title.
     * @param context Application context.
     * @param resources Resources which contains application data.
     * @return Version title.
     */
    public static String getVersionTitle(Context context, Resources resources) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return resources.getString(R.string.wubiq_version_title) + pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    /**
     * Get version number
     * @param context Application context.
     * @return Version number.
     */
    public static String getVersionName(Context context) {
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return "";
    }

    private void setAndroidTestDeviceKey(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(WubiqActivity.ANDROID_TEST_DEVICE_KEY, MobileDevices.TEST_DEVICE_INFO_KEY);
        editor.apply();

    }
}