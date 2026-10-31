package net.sf.wubiq.android;

import android.Manifest;
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

import androidx.core.app.ActivityCompat;

import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.utils.BluetoothUtils;
import net.sf.wubiq.android.utils.NetworkUtils;
import net.sf.wubiq.android.utils.NotificationUtils;
import net.sf.wubiq.android.utils.WindowUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity.
 * @author Federico Alcantara
 *
 */
public class WubiqActivity extends Activity {
    private static final String TAG = WubiqActivity.class.getSimpleName();
    private static final int OPTIONAL_PERMISSIONS_REQUEST_CODE = 3;

    public static final String PREFERENCES = "WUBIQ_ANDROID";
	public static final String HOST_KEY="server_host";
	public static final String PORT_KEY="server_port";
	public static final String CONNECTIONS_KEY="server_connections";
	public static final String PRINT_DELAY_KEY="print_delay";
	public static final String PRINT_PAUSE_KEY="print_pause";
	public static final String PRINT_POLL_INTERVAL_KEY="print_poll_interval";
	public static final String PRINT_PAUSE_BETWEEN_JOBS_KEY="print_pause_between_jobs";
	public static final String PRINT_PAUSE_BETWEEN_COPIES_KEY="print_pause_between_copies";
	public static final String PRINT_CONNECTION_ERRORS_RETRY_KEY="print_error_retry";
	public static final String UUID_KEY="client_uuid";
	public static final String GROUPS_KEY="groups";
	public static final String DEVICE_PREFIX = "wubiq-android-bt_";
    public static final String ANDROID_TEST_DEVICE_KEY = DEVICE_PREFIX + DeviceForTesting.TEST_DEVICE_ADDRESS;
	public static final String KEEP_SERVICE_ALIVE = "keep_service_alive";
    public static final String SUPPRESS_NOTIFICATIONS = "suppress_notifications";
    public static final String ENABLE_DEVELOPMENT_MODE="enable_development_mode";
	public static final String STOP_SERVICE_STATUS="stop_wubiq_service";
	public static final String PACKAGE_NAME="net.sf.wubiq.android";
    public static final String FORCE_DEVICES_REFRESH="force_devices_refresh";
    public static final String PAUSE_PRINTING_TO_DEVICES="pause_printing_to_devices";
    public static final String SHOW_ALL_BLUETOOTH_DEVICES="show_all_bluetooth_devices";

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
        WindowUtils.applySystemBarsInsets(this);
        LinearLayout versionLayout = findViewById(R.id.versionLayout);
		TextView version = versionLayout.findViewById(R.id.version);
        Resources resources = getResources();
		String versionTitle = getVersionTitle(this, resources);
		version.setText(versionTitle);

        updatePreferences(WubiqActivity.this);
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
        SharedPreferences preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WubiqActivity.FORCE_DEVICES_REFRESH, true);
        editor.apply();
        startPrintService();
    }

    /**
     * Starts the print service once the bluetooth permissions are granted. The optional
     * permissions (notifications on Android 13+, local network on Android 17+) are then
     * requested together; the service starts even if they are denied, as printing works
     * without notifications and the server might not be on the local network.
     */
    private void startPrintService() {
        if (BluetoothUtils.bluetoothPermissionBlocked(this)) {
            BluetoothUtils.showBluetoothPermissionDialog(this);
            return;
        }
        if (!BluetoothUtils.bluetoothGranted(this)) {
            return; // Permissions requested, continues in onRequestPermissionsResult.
        }
        List<String> missing = new ArrayList<>();
        if (!NotificationUtils.INSTANCE.notificationsGranted(this)) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!NetworkUtils.localNetworkGranted(this)) {
            missing.add(Manifest.permission.ACCESS_LOCAL_NETWORK);
        }
        if (!missing.isEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toArray(new String[0]), OPTIONAL_PERMISSIONS_REQUEST_CODE);
            return; // Continues in onRequestPermissionsResult.
        }
        PrintManagerService.startService(WubiqActivity.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BluetoothUtils.REQUEST_CODE) {
            if (BluetoothUtils.allGranted(grantResults)) {
                startPrintService();
            } else {
                BluetoothUtils.showBluetoothPermissionDialog(this);
            }
        } else if (requestCode == OPTIONAL_PERMISSIONS_REQUEST_CODE) {
            for (int index = 0; index < permissions.length && index < grantResults.length; index++) {
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permission denied: " + permissions[index]);
                }
            }
            PrintManagerService.startService(WubiqActivity.this);
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

    public static void updatePreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
        Resources resources = context.getResources();
        putNewValue(preferences, WubiqActivity.ANDROID_TEST_DEVICE_KEY, MobileDevices.TEST_DEVICE_INFO_KEY);
        putNewValue(preferences, WubiqActivity.PRINT_PAUSE_BETWEEN_COPIES_KEY, resources.getInteger(R.integer.print_pause_between_copies_default));
        putNewValue(preferences, WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
        putNewValue(preferences, WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
        putNewValue(preferences, WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(R.integer.print_poll_interval_default));
        putNewValue(preferences, WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default));
        putNewValue(preferences, WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default));
        putNewValue(preferences, WubiqActivity.KEEP_SERVICE_ALIVE, resources.getBoolean(R.bool.keep_service_alive_default_value));
        putNewValue(preferences, WubiqActivity.SUPPRESS_NOTIFICATIONS, resources.getBoolean(R.bool.suppress_notifications_default_value));
        putNewValue(preferences, WubiqActivity.ENABLE_DEVELOPMENT_MODE, resources.getBoolean(R.bool.enable_development_mode_default_value));

        // FORCED initial values
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(WubiqActivity.PAUSE_PRINTING_TO_DEVICES, false);
        editor.putBoolean(WubiqActivity.FORCE_DEVICES_REFRESH, false);
        editor.apply();
    }

    private static void putNewValue(SharedPreferences preferences, String key, String value) {
        if (!preferences.contains(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    private static void putNewValue(SharedPreferences preferences, String key, Integer value) {
        if (!preferences.contains(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    private static void putNewValue(SharedPreferences preferences, String key, Boolean value) {
        if (!preferences.contains(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

}