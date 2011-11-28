/**
 * 
 */
package net.sf.wubiq.android;

import java.lang.Thread.State;

import net.sf.wubiq.clients.BluetoothPrintManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Federico Alcantara
 *
 */
public class PrintManagerService extends Service {

	private static final String TAG = "PrintManagerService";
	private Thread managerThread;
	SharedPreferences preferences;
	BluetoothPrintManager manager;
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
		preferences = getSharedPreferences(AndroidActivity.PREFERENCES, MODE_PRIVATE);
		startPrintManager();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (manager != null) {
			manager.setCancelManager(true);
		}
	}
	
    private void startPrintManager() {
    	if (!cancelManager) {
			manager = new BluetoothPrintManager(this, preferences);
	        managerThread = new Thread(manager);
	        managerThread.start();
			startTimer();
    	}
    }
    
    private void startTimer() {
    	timerHandler.removeCallbacks(timerRunnable);
    	timerHandler.postDelayed(timerRunnable, 5000);
    }

    private void checkPrintManagerStatus() {
    	if (managerThread.getState().equals(State.TERMINATED)) {
    		String message = getString(R.string.error_cant_connect_to).replaceAll("%0", manager.hostServletUrl());
    		Log.e(TAG, message);
    		startPrintManager();
    	}
    }

    
}