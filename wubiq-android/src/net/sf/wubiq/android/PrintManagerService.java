/**
 * 
 */
package net.sf.wubiq.android;

import java.lang.Thread.State;

import net.sf.wubiq.android.enums.NotificationIds;
import net.sf.wubiq.android.utils.NotificationUtils;
import net.sf.wubiq.clients.BluetoothPrintManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Service that controls the communication to server.
 * Design to keep connection alive and restore dropped communication.
 * @author Federico Alcantara
 *
 */
public class PrintManagerService extends Service {

	private static final String TAG = PrintManagerService.class.getSimpleName();
	public static int connectionErrors = 0;
	private Thread managerThread;
	private Resources resources;
	private SharedPreferences preferences;
	private BluetoothPrintManager manager;
	private boolean cancelManager;
	private Handler timerHandler = new Handler();
	
	private Runnable timerRunnable = new Runnable() {
		public void run() {
			checkPrintManagerStatus();
			timerHandler.postDelayed(this, 15000);
		}
		
	};

	public class PrintManagerBinder extends Binder {
		PrintManagerService getService() {
			return PrintManagerService.this;
		}
	}
	
	/**
	 * Keep the service sticky.
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY + Service.START_STICKY_COMPATIBILITY;
	}
	
	/**
	 * @see android.app.Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		resources = getResources();
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
		startPrintManager();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		cancelManager = true;
		if (manager != null) {
			manager.setCancelManager(true);
		}
		timerHandler.removeCallbacks(timerRunnable);
		timerHandler = null;
		
		return super.onUnbind(intent);
	}

	private void startPrintManager() {
    	if (!cancelManager) {
			manager = new BluetoothPrintManager(this, resources, preferences);
	        managerThread = new Thread(manager);
	        managerThread.start();
			startTimer();
    	} else {
    		manager = null;
    	}
    }
    
    private void startTimer() {
    	timerHandler.removeCallbacks(timerRunnable);
    	timerHandler.postDelayed(timerRunnable, 5000);
    }

    /**
     * @return True if print manager is running
     */
    public boolean checkPrintManagerStatus() {
    	boolean returnValue = false;
    	if (managerThread.getState().equals(State.TERMINATED)) {
    		String message = getString(R.string.error_cant_connect_to);
    		Log.e(TAG, message);
    		connectionErrors++;
    		NotificationUtils.INSTANCE.notify(getApplicationContext(), 
    				NotificationIds.CONNECTION_ERROR_ID, 
    				connectionErrors,
    				message);
    		startPrintManager();
    	} else {
    		if (cancelManager) {
    			manager.setCancelManager(true);
    		} else {
    			returnValue = true;
    		}
    	}
    	return returnValue;
    }
    
}
