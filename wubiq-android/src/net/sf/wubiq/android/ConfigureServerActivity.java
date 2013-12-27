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
import android.widget.TableLayout;

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
		TableLayout table = (TableLayout) findViewById(R.id.parametersTable);
		EditText host = (EditText) table.findViewById(R.id.hostField);
		EditText port = (EditText) table.findViewById(R.id.portField);
		EditText uuid = (EditText) table.findViewById(R.id.clientUUIDField);
		EditText connections = (EditText) table.findViewById(R.id.connectionsField);
		Resources resources = getResources();
		host.setText(preferences.getString(WubiqActivity.HOST_KEY, resources.getString(R.string.server_host_default)));
		port.setText(preferences.getString(WubiqActivity.PORT_KEY, resources.getString(R.string.server_port_default)));
		uuid.setText(preferences.getString(WubiqActivity.UUID_KEY, UUID.randomUUID().toString()));
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
		EditText host = (EditText) findViewById(R.id.hostField);
		EditText port = (EditText) findViewById(R.id.portField);
		EditText uuid = (EditText) findViewById(R.id.clientUUIDField);
		EditText connections = (EditText) findViewById(R.id.connectionsField);
		editor.putString(WubiqActivity.HOST_KEY, host.getText().toString());
		editor.putString(WubiqActivity.PORT_KEY, port.getText().toString());
		editor.putString(WubiqActivity.UUID_KEY, uuid.getText().toString());
		editor.putString(WubiqActivity.CONNECTIONS_KEY, connections.getText().toString());
		editor.commit();
	}
	
}
