/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import net.sf.wubiq.android.clients.BluetoothPrintManager;
import net.sf.wubiq.android.enums.NotificationIds;
import net.sf.wubiq.android.utils.NotificationUtils;

import java.lang.Thread.State;

/**
 * Service that controls the communication to server.
 * Design to keep connection alive and restore dropped communication.
 * @author Federico Alcantara
 *
 */
public class PrintManagerService extends JobIntentService {
    public static int connectionErrors = 0;
    public static final int JOB_ID = 19640229;

	private static final String TAG = PrintManagerService.class.getSimpleName();
	private Thread managerThread;
	private Resources resources;
	private SharedPreferences preferences;
	private BluetoothPrintManager manager;
	private Handler timerHandler = new Handler();
	
	private Runnable timerRunnable = new Runnable() {
		public void run() {
			if (checkPrintManagerStatus()) {
				timerHandler.postDelayed(this, 15000);
			}
		}
		
	};

	/**
	 * Keep the service sticky.
	 * @see Service#onStartCommand(Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY + Service.START_STICKY_COMPATIBILITY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		resources = getResources();
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        manager = new BluetoothPrintManager(this, resources, preferences);
        managerThread = new Thread(manager, "Wubiq-PrintManager thread");
        managerThread.start();
        startTimer();
    }

    private void startTimer() {
    	timerHandler.removeCallbacks(timerRunnable);
    	timerHandler.postDelayed(timerRunnable, 5000);
    }

    /**
     * @return True if print manager is running
     */
    private boolean checkPrintManagerStatus() {
    	boolean returnValue = false;
    	if (managerThread.getState().equals(State.TERMINATED)) {
			if (preferences.getBoolean(WubiqActivity.STOP_SERVICE_STATUS, false)) {
				String message = getString(R.string.service_stopped);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(WubiqActivity.STOP_SERVICE_STATUS, false);
                editor.apply();
                Log.e(TAG, message);
                NotificationUtils.INSTANCE.notify(getApplicationContext(),
                        NotificationIds.PRINTING_INFO_ID,
                        0,
                        message);
			} else {
                String message = getString(R.string.error_cant_connect_to);
                Log.e(TAG, message);
                connectionErrors++;
                NotificationUtils.INSTANCE.notify(getApplicationContext(),
                        NotificationIds.CONNECTION_ERROR_ID,
                        connectionErrors,
                        message);
            }
    	} else {
    	    connectionErrors = 0;
			returnValue = true;
    	}
    	return returnValue;
    }
    
}
