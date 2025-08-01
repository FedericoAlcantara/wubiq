/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

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
public class PrintManagerService extends Service {
	private static final String TAG = PrintManagerService.class.getSimpleName();
    private static final int INTERVAL_MILLIS = 15000;
	private static final int SERVICE_NOTIFICATION_ID = 829;
	private Thread managerThread;
	private Resources resources;
	private SharedPreferences preferences;
	private BluetoothPrintManager manager;
	private Handler handler;
	private Runnable runnable;

	/**
	 * Keep the service sticky.
	 * @see Service#onStartCommand(Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, getString(R.string.service_started));
		handler.postDelayed(runnable, getResources().getInteger(R.integer.print_delay_default));
		return Service.START_STICKY + Service.START_STICKY_COMPATIBILITY;
	}

	/**
	 * @see Service#onBind(Intent)
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

		String message = getString(R.string.service_running);
		Log.e(TAG, message);

		NotificationUtils.INSTANCE.createNotificationChannel(this);
		Notification notification = NotificationUtils.INSTANCE.createNotification(this, NotificationIds.PRINTING_INFO_ID, 0, getString(R.string.service_running), true).build();
		startForeground(SERVICE_NOTIFICATION_ID, notification);
		startPrintManager();

		handler = new Handler(Looper.getMainLooper());

		runnable = new Runnable() {
			public void run() {
				updateNotifyPrintServiceStatus();
				if (!preferences.getBoolean(WubiqActivity.STOP_SERVICE_STATUS, false)) {
					handler.postDelayed(this, INTERVAL_MILLIS);
				}
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		removeHandler();
		stopForeground(true);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		removeHandler();
		return super.onUnbind(intent);
	}

	private void removeHandler() {
		handler.removeCallbacks(runnable);
		handler = null;
		Log.d(TAG, getString(R.string.service_stopped));
	}

	private void startPrintManager() {
		if (manager == null) {
			manager = new BluetoothPrintManager(this, resources, preferences);
			managerThread = new Thread(manager);
			managerThread.start();
		}
	}

	private void updateNotifyPrintServiceStatus() {
		if (managerThread != null) {
			if (preferences.getBoolean(WubiqActivity.STOP_SERVICE_STATUS, false)
				|| managerThread.getState() == State.TERMINATED
				|| !managerThread.isAlive()) {
				manager.setCancelManager(true);
				manager.setKillManager(true);
				manager = null;
				managerThread = null; // will be automatically terminated
				stopSelf();

				String message = getString(R.string.service_stopped);
				Log.e(TAG, message);
				NotificationUtils.INSTANCE.notify(getApplicationContext(),
						NotificationIds.PRINTING_INFO_ID,
						0,
						message);
			}
		}
	}

	public static void startService(Context context) {
		Intent serviceIntent = new Intent(context, PrintManagerService.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(serviceIntent);
		} else {
			context.startService(serviceIntent);
		}
	}

}
