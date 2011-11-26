/**
 * 
 */
package net.sf.wubiq.android;

import java.util.UUID;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Preferences for connecting to a wubiq-server
 * @author Federico Alcantara
 *
 */
public class ConfigureServerActivity extends Activity {
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configure_server);
		preferences = getSharedPreferences(AndroidActivity.PREFERENCES, MODE_PRIVATE);
		initialize();
	}

	private void initialize() {
		EditText host = (EditText) findViewById(R.id.hostField);
		EditText port = (EditText) findViewById(R.id.portField);
		EditText uuid = (EditText) findViewById(R.id.clientUUIDField);
		Resources resources = getResources();
		host.setText(preferences.getString(AndroidActivity.HOST_KEY, resources.getString(R.string.server_host_default)));
		port.setText(preferences.getString(AndroidActivity.PORT_KEY, resources.getString(R.string.server_port_default)));
		uuid.setText(preferences.getString(AndroidActivity.UUID_KEY, UUID.randomUUID().toString()));
		savePreferences();
	}

	@Override
	protected void onPause() {
		super.onPause();
		savePreferences();
	}
	
	private void savePreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		EditText host = (EditText) findViewById(R.id.hostField);
		EditText port = (EditText) findViewById(R.id.portField);
		EditText uuid = (EditText) findViewById(R.id.clientUUIDField);
		editor.putString(AndroidActivity.HOST_KEY, host.getText().toString());
		editor.putString(AndroidActivity.PORT_KEY, port.getText().toString());
		editor.putString(AndroidActivity.UUID_KEY, uuid.getText().toString());
		editor.commit();
	}
}
