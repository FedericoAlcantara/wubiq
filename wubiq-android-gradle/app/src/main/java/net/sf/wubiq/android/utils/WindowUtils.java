/**
 *
 */
package net.sf.wubiq.android.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Window related utilities.
 * @author Federico Alcantara
 *
 */
public class WindowUtils {

	/**
	 * Keeps the activity content clear of the status bar, navigation bar, display cutouts and keyboard.
	 * Android 15+ draws apps targeting API 35+ edge-to-edge (behind the system bars), so the
	 * content root must be padded with the window insets. On older versions the insets are
	 * already consumed by the window and no padding is added.
	 * Must be called after setContentView.
	 * @param activity Activity whose content must be kept clear of the system bars.
	 */
	public static void applySystemBarsInsets(Activity activity) {
		ViewGroup content = activity.findViewById(android.R.id.content);
		if (content == null || content.getChildCount() == 0) {
			return;
		}
		View root = content.getChildAt(0);
		final int paddingLeft = root.getPaddingLeft();
		final int paddingTop = root.getPaddingTop();
		final int paddingRight = root.getPaddingRight();
		final int paddingBottom = root.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
			Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
					| WindowInsetsCompat.Type.displayCutout());
			Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
			view.setPadding(paddingLeft + bars.left,
					paddingTop + bars.top,
					paddingRight + bars.right,
					paddingBottom + Math.max(bars.bottom, ime.bottom));
			return WindowInsetsCompat.CONSUMED;
		});
	}
}
