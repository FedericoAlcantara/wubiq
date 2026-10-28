/**
 *
 */
package net.sf.wubiq.android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * Network related utilities.
 * @author Federico Alcantara
 *
 */
public class NetworkUtils {
	/**
	 * Tells whether the app may reach devices on the local network. Apps targeting
	 * Android 17+ (API 37) need ACCESS_LOCAL_NETWORK to connect to a Wubiq server on the LAN;
	 * on older versions it is always allowed.
	 * @param context Running context.
	 * @return True if the local network can be reached.
	 */
	public static boolean localNetworkGranted(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
			return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK)
					== PackageManager.PERMISSION_GRANTED;
		}
		return true;
	}
}
