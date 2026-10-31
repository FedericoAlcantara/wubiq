/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.utils.SecondsUtils;
import net.sf.wubiq.android.utils.WindowUtils;

/**
 * Advanced configuration activity.
 * @author Federico Alcantara
 *
 */
public class AdvancedConfigurationActivity extends Activity {
	private SharedPreferences preferences;
	private int pauseBetweenCopiesPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_configuration);
		WindowUtils.applySystemBarsInsets(this);
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
		initialize();
	}
	
	/**
	 * Loads view with new or previously stored preferences.
	 */
	private void initialize() {
		EditText printDelay = (EditText) findViewById(R.id.printDelayField);
		EditText printPause = (EditText) findViewById(R.id.printPauseField);
		EditText printPollInterval = (EditText) findViewById(R.id.printPollIntervalField);
		EditText printPauseBetweenJobs = (EditText) findViewById(R.id.printPauseBetweenJobsField);
		EditText printConnectionErrorsRetry = (EditText) findViewById(R.id.printConnectionErrorRetries);
		SwitchCompat keepServiceAlive = (SwitchCompat) findViewById(R.id.keepServiceAlive);
		SwitchCompat suppressNotifications = (SwitchCompat) findViewById(R.id.suppressNotifications);
		SwitchCompat enableDevelopmentMode = (SwitchCompat) findViewById(R.id.enableDevelopmentMode);

		Resources resources = getResources();
		
		int printPauseBetweenCopiesValue = preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_COPIES_KEY, resources.getInteger(R.integer.print_pause_between_copies_default));
		Integer printDelayValue = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		Integer printPauseValue = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		Integer printPollIntervalValue = preferences.getInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(R.integer.print_poll_interval_default));
		Integer printPauseBetweenJobsValue = preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default));
		Integer printConnectionErrorsRetryValue = preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default));
		Boolean keepServiceAliveValue = preferences.getBoolean(WubiqActivity.KEEP_SERVICE_ALIVE, false);
		Boolean suppressNotificationsValue = preferences.getBoolean(WubiqActivity.SUPPRESS_NOTIFICATIONS, false);
		Boolean enableDevelopmentModeValue = preferences.getBoolean(WubiqActivity.ENABLE_DEVELOPMENT_MODE, false);

		initializePauseBetweenCopies(printPauseBetweenCopiesValue);
		findViewById(R.id.pauseBetweenCopiesGroup).setVisibility(
				PrintClientUtils.INSTANCE.usesStarMicronics(preferences.getAll()) ? View.VISIBLE : View.GONE);
		printDelay.setText(printDelayValue.toString());
		printPause.setText(printPauseValue.toString());
		printPollInterval.setText(printPollIntervalValue.toString());
		printPauseBetweenJobs.setText(printPauseBetweenJobsValue.toString());
		printConnectionErrorsRetry.setText(printConnectionErrorsRetryValue.toString());
		keepServiceAlive.setChecked(keepServiceAliveValue);
		suppressNotifications.setChecked(suppressNotificationsValue);
		enableDevelopmentMode.setChecked(enableDevelopmentModeValue);
		savePreferences();
		refreshTestResults(null);
	}
	
	/**
	 * Loads the pause between copies: a list of presets plus a custom entry that shows the text field.
	 * @param milliseconds Stored pause; if it is not a preset the custom entry is selected.
	 */
	private void initializePauseBetweenCopies(int milliseconds) {
		Spinner spinner = (Spinner) findViewById(R.id.printPauseBetweenCopiesSpinner);
		final EditText field = (EditText) findViewById(R.id.printPauseBetweenCopiesField);
		final int[] presets = getResources().getIntArray(R.array.print_pause_between_copies_presets);
		List<String> labels = new ArrayList<String>();
		for (int preset : presets) {
			labels.add(preset == 0
					? getString(R.string.pause_minimal)
					: getString(R.string.pause_seconds, SecondsUtils.toSeconds(preset, Locale.getDefault())));
		}
		labels.add(getString(R.string.pause_custom));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		int position = presets.length;
		for (int index = 0; index < presets.length; index++) {
			if (presets[index] == milliseconds) {
				position = index;
			}
		}
		pauseBetweenCopiesPosition = position;
		spinner.setSelection(position);
		field.setText(SecondsUtils.toSeconds(milliseconds, Locale.getDefault()));
		showCustomPause(position == presets.length);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == pauseBetweenCopiesPosition) {
					return;
				}
				pauseBetweenCopiesPosition = position;
				if (position < presets.length) {
					field.setText(SecondsUtils.toSeconds(presets[position], Locale.getDefault()));
					showCustomPause(false);
				} else {
					showCustomPause(true);
					field.requestFocus();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (!hasFocus) {
					field.setText(SecondsUtils.toSeconds(customPauseBetweenCopies(), Locale.getDefault()));
				}
			}
		});
	}

	private void showCustomPause(boolean visible) {
		int visibility = visible ? View.VISIBLE : View.GONE;
		findViewById(R.id.printPauseBetweenCopiesField).setVisibility(visibility);
	}

	/**
	 * @return Milliseconds typed in the custom field, within the allowed range.
	 */
	private int customPauseBetweenCopies() {
		EditText field = (EditText) findViewById(R.id.printPauseBetweenCopiesField);
		Resources resources = getResources();
		return SecondsUtils.toMilliseconds(field.getText().toString(),
				resources.getInteger(R.integer.print_pause_between_copies_default),
				resources.getInteger(R.integer.print_pause_between_copies_max));
	}

	/**
	 * @return Milliseconds of the selected preset, or the custom value when the custom entry is selected.
	 */
	private int pauseBetweenCopies() {
		int[] presets = getResources().getIntArray(R.array.print_pause_between_copies_presets);
		return pauseBetweenCopiesPosition >= 0 && pauseBetweenCopiesPosition < presets.length
				? presets[pauseBetweenCopiesPosition]
				: customPauseBetweenCopies();
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePreferences();
	}
	
	public void setDefaultValues(View view) {
		Resources resources = getResources();
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(WubiqActivity.PRINT_PAUSE_BETWEEN_COPIES_KEY);
		editor.remove(WubiqActivity.PRINT_DELAY_KEY);
		editor.remove(WubiqActivity.PRINT_PAUSE_KEY);
		editor.remove(WubiqActivity.PRINT_POLL_INTERVAL_KEY);
		editor.remove(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY);
		editor.remove(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY);
		editor.remove(WubiqActivity.KEEP_SERVICE_ALIVE);
		editor.remove(WubiqActivity.ENABLE_DEVELOPMENT_MODE);
		editor.apply();
		WubiqActivity.updatePreferences(getApplicationContext());
		initialize();
	}

	public void refreshTestResults(View view) {
        String result = preferences.getString(DeviceForTesting.TEST_DEVICE_RESULT_KEY, "");
        String base64Image = preferences.getString(DeviceForTesting.TEST_DEVICE_RESULT_IMAGE_KEY, "");
        EditText testResults = (EditText) findViewById(R.id.testResult);
        ImageView testResultsImage = (ImageView) findViewById(R.id.testImageView);
        testResults.setText(result);
        if (!"".equals(base64Image)) {
            byte[] bitmapArray = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            testResultsImage.setImageBitmap(bitmap);
        } else {
            testResultsImage.setImageBitmap(null);
        }
    }

    public void clearTestResults(View view) {
        Resources resources = getResources();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DeviceForTesting.TEST_DEVICE_RESULT_KEY, "");
        editor.putString(DeviceForTesting.TEST_DEVICE_RESULT_IMAGE_KEY, "");
        editor.apply();
        refreshTestResults(null);
    }

    private void savePreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		EditText printDelay = (EditText) findViewById(R.id.printDelayField);
		EditText printPause = (EditText) findViewById(R.id.printPauseField);
		EditText printPollInterval = (EditText) findViewById(R.id.printPollIntervalField);
		EditText printPauseBetweenJobs = (EditText) findViewById(R.id.printPauseBetweenJobsField);
		EditText printConnectionErrorsRetry = (EditText) findViewById(R.id.printConnectionErrorRetries);
		SwitchCompat keepServiceAlive = (SwitchCompat) findViewById(R.id.keepServiceAlive);
		SwitchCompat enableDevelopmentMode = (SwitchCompat) findViewById(R.id.enableDevelopmentMode);
		SwitchCompat suppressNotifications = (SwitchCompat) findViewById(R.id.suppressNotifications);

		editor.putInt(WubiqActivity.PRINT_PAUSE_BETWEEN_COPIES_KEY, pauseBetweenCopies());
		editor.putInt(WubiqActivity.PRINT_DELAY_KEY, Integer.parseInt(printDelay.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_PAUSE_KEY, Integer.parseInt(printPause.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, Integer.parseInt(printPollInterval.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, Integer.parseInt(printPauseBetweenJobs.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, Integer.parseInt(printConnectionErrorsRetry.getText().toString()));
		editor.putBoolean(WubiqActivity.KEEP_SERVICE_ALIVE, keepServiceAlive.isChecked());
		editor.putBoolean(WubiqActivity.SUPPRESS_NOTIFICATIONS, suppressNotifications.isChecked());
		editor.putBoolean(WubiqActivity.ENABLE_DEVELOPMENT_MODE, enableDevelopmentMode.isChecked());

		editor.apply();
	}

}
