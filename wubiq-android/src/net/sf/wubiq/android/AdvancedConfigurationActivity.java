/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Advanced configuration activity.
 * @author Federico Alcantara
 *
 */
public class AdvancedConfigurationActivity extends Activity {
	private SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_configuration);
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

		Resources resources = getResources();
		
		Integer printDelayValue = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		Integer printPauseValue = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		Integer printPollIntervalValue = preferences.getInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(R.integer.print_poll_interval_default));;
		Integer printPauseBetweenJobsValue = preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default));;
		Integer printConnectionErrorsRetryValue = preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default));;
		
		printDelay.setText(printDelayValue.toString());
		printPause.setText(printPauseValue.toString());
		printPollInterval.setText(printPollIntervalValue.toString());
		printPauseBetweenJobs.setText(printPauseBetweenJobsValue.toString());
		printConnectionErrorsRetry.setText(printConnectionErrorsRetryValue.toString());
		savePreferences();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		savePreferences();
	}
	
	public void setDefaultValues(View view) {
		Resources resources = getResources();
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(R.integer.print_delay_default));
		editor.putInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(R.integer.print_pause_default));
		editor.putInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(R.integer.print_poll_interval_default));
		editor.putInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default));
		editor.putInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default));
		editor.commit();
		initialize();
	}
	
	private void savePreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		EditText printDelay = (EditText) findViewById(R.id.printDelayField);
		EditText printPause = (EditText) findViewById(R.id.printPauseField);
		EditText printPollInterval = (EditText) findViewById(R.id.printPollIntervalField);
		EditText printPauseBetweenJobs = (EditText) findViewById(R.id.printPauseBetweenJobsField);
		EditText printConnectionErrorsRetry = (EditText) findViewById(R.id.printConnectionErrorRetries);
		
		editor.putInt(WubiqActivity.PRINT_DELAY_KEY, Integer.parseInt(printDelay.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_PAUSE_KEY, Integer.parseInt(printPause.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, Integer.parseInt(printPollInterval.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, Integer.parseInt(printPauseBetweenJobs.getText().toString()));
		editor.putInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, Integer.parseInt(printConnectionErrorsRetry.getText().toString()));
		editor.commit();
	}

}
