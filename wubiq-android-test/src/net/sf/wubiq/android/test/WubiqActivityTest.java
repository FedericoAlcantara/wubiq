/**
 * 
 */
package net.sf.wubiq.android.test;

import net.sf.wubiq.android.ConfigureBluetoothActivity;
import net.sf.wubiq.android.ConfigureServerActivity;
import net.sf.wubiq.android.WubiqActivity;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;


/**
 * Tests android currentActivity of wubiq-android
 * @author Federico Alcantara
 *
 */
public class WubiqActivityTest extends InstrumentationTestCase {
	private Activity currentActivity;
	private Button configureServer;
	private Button configureBluetooth;
	private Instrumentation instrumentation;
	private Instrumentation.ActivityMonitor monitor;
	SharedPreferences preferences;
	private EditText host;
	private EditText port;
	private EditText uuid;
	private EditText groups;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@MediumTest
	public void testWubiqActivity() {
		instrumentation = getInstrumentation();
		monitor = instrumentation.addMonitor(WubiqActivity.class.getName(), null, false);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), WubiqActivity.class.getName());
		instrumentation.startActivitySync(intent);
		currentActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5);
		resetValues();
		startConfigureServer();

		// Prepare monitor for main Activity
		instrumentation.removeMonitor(monitor);
		monitor = instrumentation.addMonitor(WubiqActivity.class.getName(), null, false);
		
		// Get back to main activity
		currentActivity.finish();
		
		currentActivity = getInstrumentation().waitForMonitor(monitor);
		assertNotNull(currentActivity);
		String pHost = preferences.getString(WubiqActivity.HOST_KEY, null);
		String pPort = preferences.getString(WubiqActivity.PORT_KEY, null);
		String pUuid = preferences.getString(WubiqActivity.UUID_KEY, null);

		assertEquals("Must be equal to http://127.0.0.1", "http://127.0.0.1", pHost);
		assertEquals("Must be 8090", "8090", pPort);
		assertEquals("Must be same UUID", uuid.getText().toString(), pUuid);
		
		startConfigureBluetooth();
		
		// Prepare monitor for main Activity
		instrumentation.removeMonitor(monitor);
		monitor = instrumentation.addMonitor(WubiqActivity.class.getName(), null, false);
		
		// Get back to main activity
		currentActivity.finish();
		
		currentActivity = getInstrumentation().waitForMonitor(monitor);
		assertNotNull(currentActivity);
		resetValues();
	}
	
	private void startConfigureServer(){
		// Prepare monitor for configure Server Activity
		instrumentation.removeMonitor(monitor);
		monitor = instrumentation.addMonitor(ConfigureServerActivity.class.getName(), null, false);

		configureServer = (Button)currentActivity.findViewById(net.sf.wubiq.android.R.id.configureServer);
		TouchUtils.clickView(this, configureServer);

		currentActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5);
		assertNotNull(currentActivity);
		
		host = (EditText) currentActivity.findViewById(net.sf.wubiq.android.R.id.hostField);
		port = (EditText) currentActivity.findViewById(net.sf.wubiq.android.R.id.portField);
		uuid = (EditText) currentActivity.findViewById(net.sf.wubiq.android.R.id.clientUUIDField);
		groups = (EditText) currentActivity.findViewById(net.sf.wubiq.android.R.id.groupsField);
		assertEquals("Should be http://localhost", "http://localhost", host.getText().toString());
		assertEquals("Should be 8080", "8080", port.getText().toString());
		assertNotSame("Should not be blank", "", uuid.getText().toString());
		assertEquals("Should be Blanck", "", groups.getText().toString());
		TouchUtils.clickView(this, host);
		sendRepeatedKeys(9,KeyEvent.KEYCODE_DEL);
		sendKeys(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_PERIOD);
		sendKeys(KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_PERIOD);
		sendKeys(KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_PERIOD);
		sendKeys(KeyEvent.KEYCODE_1);
		
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendRepeatedKeys(4,KeyEvent.KEYCODE_DEL);
		instrumentation.sendStringSync("8090");
	}
	
	private void startConfigureBluetooth() {
		// Prepare monitor for configure Bluetooth Activity
		instrumentation.removeMonitor(monitor);
		monitor = instrumentation.addMonitor(ConfigureBluetoothActivity.class.getName(), null, false);

		configureBluetooth = (Button)currentActivity.findViewById(net.sf.wubiq.android.R.id.configureBluetooth);
		TouchUtils.clickView(this, configureBluetooth);
		
		currentActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5);
		assertNotNull(currentActivity);
	}
	
	private void resetValues() {
		preferences = currentActivity.getSharedPreferences(WubiqActivity.PREFERENCES, Activity.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(WubiqActivity.HOST_KEY, "http://localhost");
		editor.putString(WubiqActivity.PORT_KEY, "8080");
		editor.remove(WubiqActivity.UUID_KEY);
		editor.commit();
	}
}
