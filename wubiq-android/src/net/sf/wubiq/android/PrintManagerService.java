/**
 * 
 */
package net.sf.wubiq.android;

import java.lang.Thread.State;

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

	private static final String TAG = "PrintManagerService";
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
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
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
    		String message = getString(R.string.error_cant_connect_to).replaceAll("%0", manager.hostServletUrl());
    		Log.e(TAG, message);
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

    /**
     * @return Status of the manager.
     * @throws Exception
     */
    public String checkKilledStatus() throws Exception {
    	return manager.askServer("isKilled");
    }
    
}
