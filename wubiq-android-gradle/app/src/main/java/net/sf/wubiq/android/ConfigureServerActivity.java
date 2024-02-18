/**
 * 
 */
package net.sf.wubiq.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;

import java.util.Date;

/**
 * Preferences for connecting to a wubiq-server
 * @author Federico Alcantara
 *
 */
public class ConfigureServerActivity extends Activity {
	SharedPreferences preferences;
	
	/**
	 * Called upon activity creation.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_server);
		preferences = getSharedPreferences(WubiqActivity.PREFERENCES, MODE_PRIVATE);
		initialize();
	}

	/**
	 * Loads view with new or previously stored preferences.
	 */
	private void initialize() {
		EditText uuid = (EditText) findViewById(R.id.clientUUIDField);
		EditText groups = (EditText) findViewById(R.id.groupsField);
		EditText connections = (EditText) findViewById(R.id.connectionsField);
		Resources resources = getResources();
		String suggestedUuid = Build.MODEL + "-" + new Date().getTime();
		uuid.setText(preferences.getString(WubiqActivity.UUID_KEY, suggestedUuid));
		groups.setText(preferences.getString(WubiqActivity.GROUPS_KEY, ""));
		connections.setText(preferences.getString(WubiqActivity.CONNECTIONS_KEY, resources.getString(R.string.server_connection_default)));
		savePreferences();
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePreferences();
	}
	
	private void savePreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		EditText uuid = (EditText) findViewById(R.id.clientUUIDField);
		EditText groups = (EditText) findViewById(R.id.groupsField);
		EditText connections = (EditText) findViewById(R.id.connectionsField);
		editor.putString(WubiqActivity.UUID_KEY, uuid.getText().toString());
		editor.putString(WubiqActivity.GROUPS_KEY, groups.getText().toString());
		editor.putString(WubiqActivity.CONNECTIONS_KEY, connections.getText().toString());
		editor.commit();
	}
	
}
