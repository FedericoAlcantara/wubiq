/**
 * 
 */
package net.sf.wubiq.android.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.android.AdvancedConfigurationActivity;
import net.sf.wubiq.android.ConfigureBluetoothActivity;
import net.sf.wubiq.android.ConfigureServerActivity;
import net.sf.wubiq.android.MobileDevices;
import net.sf.wubiq.android.PrintClientUtils;
import net.sf.wubiq.android.R;
import net.sf.wubiq.android.WubiqActivity;
import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.test.wrappers.LocalManagerTestWrapper;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.utils.Is;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.test.TouchUtils;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * Tests android currentActivity of wubiq-android
 * @author Federico Alcantara
 *
 */
public class WubiqActivityTest extends WubiqBaseTest {
	private final long TIMEOUT = 30;
	private final int WAIT_TIME_SECONDS = 3;
	private static Map<String, String> md5Map;
	private static Map<String, String> md5MapPageable;
	
	private Activity mainActivity;
	private Instrumentation.ActivityMonitor mainActivityMonitor;
	
	
	SharedPreferences preferences;
	
	int testDeviceIndex = -1;
	TextView testDevice;
	Spinner testDeviceSelected;
	
	private String groups;

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	@MediumTest
	public void testWubiqActivity() throws Exception {
		mainActivityMonitor = instrumentation.addMonitor(WubiqActivity.class.getName(), null, false);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(instrumentation.getTargetContext(), WubiqActivity.class.getName());
		instrumentation.startActivitySync(intent);
		mainActivity = getInstrumentation().waitForMonitorWithTimeout(mainActivityMonitor, TIMEOUT);
		preferences = mainActivity.getSharedPreferences(WubiqActivity.PREFERENCES, Activity.MODE_PRIVATE);
		resetValues();
		manager = new LocalManagerTestWrapper(instrumentation.getTargetContext(), instrumentation.getTargetContext().getResources(), preferences);
		try {
			Editor editor = preferences.edit();
			editor.putBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, true);
			editor.commit();
			mainActivity.finish();
			instrumentation.removeMonitor(mainActivityMonitor);
			mainActivityMonitor = instrumentation.addMonitor(WubiqActivity.class.getName(), null, false);
			instrumentation.startActivitySync(intent);
			mainActivity = getInstrumentation().waitForMonitorWithTimeout(mainActivityMonitor, TIMEOUT);
			configureServerConnection();
			configureBluetooth();
			configureAdvanced();
	
			// Validates preferences were saved properly
			// Configure server
			assertEquals("UUID must be the same", uuid, preferences.getString(WubiqActivity.UUID_KEY, null));
			assertEquals("GROUPS must be the same", AndroidTestProperties.get(ConfigurationKeys.PROPERTY_GROUPS, "----"), preferences.getString(WubiqActivity.GROUPS_KEY, null));
			Set<String> connections = new TreeSet<String>();
			for (String connection : preferences.getString(WubiqActivity.CONNECTIONS_KEY, "").split(",;")) {
				if (!Is.emptyString(connection.trim())) {
					connections.add(connection);
				}
			}
			assertEquals("CONNECTIONS must be the same", manager.getConnections(), connections);
			assertEquals("SUPPRESS NOTIFICATIONS must be selected", true, preferences.getBoolean(WubiqActivity.SUPPRESS_NOTIFICATIONS_KEY, false));
			assertEquals("HOST nust be equal to http://localhost", "http://localhost", preferences.getString(WubiqActivity.HOST_KEY, null));
			assertEquals("POST must be 8090", "8090", preferences.getString(WubiqActivity.PORT_KEY, null));
			// Bluetooth
			assertEquals("Device must be Star ", "Star Micronics 3 in", preferences.getString(WubiqActivity.DEVICE_PREFIX + DeviceForTesting.TEST_DEVICE_ADDRESS, ""));
			// Advanced			
			assertEquals("Print delay must be 1", 1, preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, -1));
			assertEquals("Print pause must be 2", 2, preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, -1));
			assertEquals("Print poll interval must be 3", 3, preferences.getInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, -1));
			assertEquals("Print pause between jobs must be 4", 4, preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, -1));
			assertEquals("Print connection errors retry must be 5", 5, preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, -1));

			// Test service connectivity
			validateServicePrintCapabilities();
			
			mainActivity.finish();
		} finally {
			resetValues();
			Editor editor = preferences.edit();
			editor.remove(PropertyKeys.WUBIQ_DEVELOPMENT_MODE);
			editor.commit();
		}
	}
	
	private void configureServerConnection(){
		// Prepare monitor for configure Server Activity
		Instrumentation.ActivityMonitor localMonitor = instrumentation.addMonitor(ConfigureServerActivity.class.getName(), null, false);
		Button activityButton = (Button)mainActivity.findViewById(net.sf.wubiq.android.R.id.configureServer);
		TouchUtils.clickView(this, activityButton);

		Activity localActivity = getInstrumentation().waitForMonitorWithTimeout(localMonitor, TIMEOUT);
		assertNotNull(localActivity);
		
		EditText uuidField = (EditText) localActivity.findViewById(net.sf.wubiq.android.R.id.clientUUIDField);
		EditText groupsField = (EditText) localActivity.findViewById(net.sf.wubiq.android.R.id.groupsField);
		EditText connectionsField = (EditText) localActivity.findViewById(net.sf.wubiq.android.R.id.connectionsField);
		CheckBox suppressNotificationsField = (CheckBox) localActivity.findViewById(net.sf.wubiq.android.R.id.suppressNotifications);
		
		// Assert default values;
		assertNotSame("Uuid should not be blank", "", uuidField.getText().toString());
		assertEquals("Groups should be blank", "", groupsField.getText().toString());
		assertEquals("Connections should be http://localhost:8080", "http://localhost:8080", connectionsField.getText().toString());
		assertFalse("Suppress notifications must be unchecked", suppressNotificationsField.isChecked());
		
		// Sets the new values;
		groups = AndroidTestProperties.get(ConfigurationKeys.PROPERTY_GROUPS, "");
		setText(uuidField, uuid);
		setText(groupsField, groups);
		// Connection field
		clearField(connectionsField);
		Iterator<String> connections = getConnections().iterator();
		int index = 0;
		while (connections.hasNext()) {
			String connection = connections.next();
			typeText(connection);
			if (index++ > 0) {
				sendKeys(KeyEvent.KEYCODE_ENTER);
			}
		} 
		setChecked(suppressNotificationsField, true);
		sendKeys(KeyEvent.KEYCODE_BACK);
		
		pause(WAIT_TIME_SECONDS);
		
		localActivity.finish();
		instrumentation.removeMonitor(localMonitor);
		pause(WAIT_TIME_SECONDS);
	}
		
	@SuppressWarnings("unchecked")
	private void configureBluetooth() {
		// Prepare monitor for configure Bluetooth Activity
		Instrumentation.ActivityMonitor localMonitor = instrumentation.addMonitor(ConfigureBluetoothActivity.class.getName(), null, false);
		Button activityButton = (Button)mainActivity.findViewById(net.sf.wubiq.android.R.id.configureBluetooth);
		TouchUtils.clickView(this, activityButton);

		Activity localActivity = getInstrumentation().waitForMonitorWithTimeout(localMonitor, TIMEOUT);
		assertNotNull(localActivity);
		
		GridView devices = (GridView) localActivity.findViewById(R.id.devices);
		// Validate test device is enabled
		boolean testDeviceFound = false;
		testDeviceIndex = -1;
		ListAdapter adapter = devices.getAdapter();
		for (int position = 0; position < adapter.getCount(); position++) {
			int index = position / 2; 
			View view = adapter.getView(position, null, null);
			if (position % 2 == 0) {
				assertTrue("Must be of Type TextView", view instanceof TextView);
				if ((DeviceForTesting.TEST_DEVICE_NAME 
						+ " "
						+ DeviceForTesting.TEST_DEVICE_ADDRESS).equals(
								((TextView)view).getText().toString())
								) {
						testDeviceFound = true;
						testDeviceIndex = index;
						testDevice = (TextView) view;
				}
			} else {
				assertTrue("Must be of Type Spinner", view instanceof Spinner);
				if (index == testDeviceIndex) {
					testDeviceSelected = (Spinner) view;
					;
					TouchUtils.clickView(this, testDeviceSelected);
					sendRepeatedKeys(13, KeyEvent.KEYCODE_DPAD_DOWN);
					sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
				}
				ArrayAdapter<String> adapt = (ArrayAdapter<String>) ((Spinner) view).getAdapter();
				assertEquals("Spinner should have " + MobileDevices.INSTANCE.getDeviceNames().size(),
						MobileDevices.INSTANCE.getDeviceNames().size(),
						adapt.getCount());
			}
		}
		assertTrue("Test device should be found", testDeviceFound);
		
		localActivity.finish();
		instrumentation.removeMonitor(localMonitor);
		pause(WAIT_TIME_SECONDS);
	}
	
	private void configureAdvanced() {
		Instrumentation.ActivityMonitor localMonitor = instrumentation.addMonitor(AdvancedConfigurationActivity.class.getName(), null, false);
		Button activityButton = (Button)mainActivity.findViewById(net.sf.wubiq.android.R.id.advancedConfiguration);
		TouchUtils.clickView(this, activityButton);
		
		Activity localActivity = getInstrumentation().waitForMonitorWithTimeout(localMonitor, TIMEOUT);
		assertNotNull(localActivity);
		
		EditText printDelay = (EditText) localActivity.findViewById(R.id.printDelayField);
		EditText printPause = (EditText) localActivity.findViewById(R.id.printPauseField);
		EditText printPollInterval = (EditText) localActivity.findViewById(R.id.printPollIntervalField);
		EditText printPauseBetweenJobs = (EditText) localActivity.findViewById(R.id.printPauseBetweenJobsField);
		EditText printConnectionErrorsRetry = (EditText) localActivity.findViewById(R.id.printConnectionErrorRetries);
		Resources resources = getInstrumentation().getTargetContext().getResources();
		
		// Assert default values
		int printDelayDefault = preferences.getInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_delay_default));
		int printPauseDefault = preferences.getInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_pause_default));
		int printPollIntervalDefault = preferences.getInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_poll_interval_default));
		int printPauseBetweenJobsDefault = preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_pause_between_jobs_default));
		int printConnectionErrorsRetryDefault = preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_connection_errors_retries_default));
		assertEquals("Print delay must be " + printDelayDefault, printDelayDefault, Integer.parseInt(printDelay.getText().toString()));
		assertEquals("Print pause must be " + printPauseDefault, printPauseDefault, Integer.parseInt(printPause.getText().toString()));
		assertEquals("Print poll interval must be " + printPollIntervalDefault, printPollIntervalDefault, Integer.parseInt(printPollInterval.getText().toString()));
		assertEquals("Print pause between jobs must be " + printPauseBetweenJobsDefault, printPauseBetweenJobsDefault, Integer.parseInt(printPauseBetweenJobs.getText().toString()));
		assertEquals("Print connection errors retry must be " + printConnectionErrorsRetryDefault, printConnectionErrorsRetryDefault, Integer.parseInt(printConnectionErrorsRetry.getText().toString()));

		// Change its value;
		clearField(printDelay);
		clearField(printDelay);
		clearField(printDelay);
		setText(printDelay, "1");
		setText(printPause, "2");
		setText(printPollInterval, "3");
		setText(printPauseBetweenJobs, "4");
		setText(printConnectionErrorsRetry, "5");
		
		localActivity.finish();
		instrumentation.removeMonitor(localMonitor);
		pause(WAIT_TIME_SECONDS);
	}
	
	private void validateServicePrintCapabilities() throws Exception {
		Resources resources = getInstrumentation().getTargetContext().getResources();
		Editor editor = preferences.edit();
		editor.putInt(WubiqActivity.PRINT_DELAY_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_delay_default));
		editor.putInt(WubiqActivity.PRINT_PAUSE_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_pause_default));
		editor.putInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_poll_interval_default));
		editor.putInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_pause_between_jobs_default));
		editor.putInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(net.sf.wubiq.android.R.integer.print_connection_errors_retries_default));
		editor.remove(WubiqActivity.SUPPRESS_NOTIFICATIONS_KEY);
		editor.commit();

		Editor edit = preferences.edit();
		edit.remove(DeviceForTesting.TEST_DEVICE_RESULT_KEY);
		edit.commit();

		String failedDevices = printToDevices("", md5Map());
		String failedDevicesPageable = printToDevices(
				ParameterKeys.PRINT_TEST_DIRECT_PAGEABLE
				+ ParameterKeys.PARAMETER_SEPARATOR
				+ "true", md5MapPageable());
		
		assertEquals("No devices should have failed", "", failedDevices.toString());
		assertEquals("No devices should have failed on pageable", "", failedDevicesPageable.toString());
	}
	
	/**
	 * Print to all described devices.
	 * @param pageableParameter Parameter for pageable, if blank prints pdf.
	 * @param md5 Md5 map for signature verification.
	 * @return Failure string.
	 */
	private String printToDevices(String pageableParameter, Map<String, String> md5) {
		Button startButton = (Button)mainActivity.findViewById(net.sf.wubiq.android.R.id.startServiceButton);
		Button stopButton = (Button)mainActivity.findViewById(net.sf.wubiq.android.R.id.stopServiceButton);
		StringBuffer failedDevices = new StringBuffer("");
		StringBuffer signatures = new StringBuffer("");
		for (String deviceName : MobileDevices.INSTANCE.getDevices().keySet()) {
			if ("--".equals(deviceName)) {
				continue;
			}
			TouchUtils.clickView(this, stopButton);
			pause(WAIT_TIME_SECONDS);
			String deviceKey = WubiqActivity.DEVICE_PREFIX + DeviceForTesting.TEST_DEVICE_ADDRESS;
			String serviceName = PrintClientUtils.INSTANCE.serializeServiceName(DeviceForTesting.TEST_DEVICE_NAME,
					DeviceForTesting.TEST_DEVICE_ADDRESS,
					deviceName);
			String printServiceParameter = ParameterKeys.PRINT_SERVICE_NAME
					+ ParameterKeys.PARAMETER_SEPARATOR
					+ serviceName;
			getWubiqPage(CommandKeys.KILL_MANAGER, "");
			pause(WAIT_TIME_SECONDS);
			
			// Set the selection in the preferences
			Editor edit = preferences.edit();
			edit.remove(DeviceForTesting.TEST_DEVICE_RESULT_KEY);
			edit.putString(deviceKey, deviceName);
			edit.commit();
			
			int serviceCount = countPrintServices(uuid);
			assertEquals("No service like that name should be registered", 0, serviceCount);
			TouchUtils.clickView(this, startButton);
			int timeCount = 60;
			int currentServiceCount = serviceCount;
			do { 
				currentServiceCount = countPrintServices(uuid);
				if (currentServiceCount > serviceCount) {
					break;
				}
				pause(1);
				if (timeCount-- <= 0) {
					break;
				}
			} while (currentServiceCount > serviceCount);			
			assertTrue("We must have another service for device '" + deviceName + "', at least", countPrintServices(uuid) > serviceCount);
			getNewTestPage(CommandKeys.PRINT_TEST_PAGE, printServiceParameter, pageableParameter);
			String result = "";
			timeCount = 30;
			do { 
				result = preferences.getString(DeviceForTesting.TEST_DEVICE_RESULT_KEY, "");
				if (!Is.emptyString(result)) {
					break;
				}
				pause(1);
				if (timeCount-- <= 0) {
					break;
				}
			} while (Is.emptyString(result));
			
			if (Is.emptyString(result) 
					//|| !result.equals(md5.get(deviceName))
					) {
				if (failedDevices.length() > 0) {
					failedDevices.append(", ");
				}
				failedDevices.append(deviceName);
			} else {
				if (signatures.length() > 0) {
					signatures.append("\n");
				}
				signatures.append(deviceName)
					.append(',')
					.append(result);
			}
			pause(10);
		}
		System.out.println(signatures.toString());
		return failedDevices.toString();
	}
	
	private void resetValues() {
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}
	
	private Map<String, String> md5Map() {
		if (md5Map == null) {
			md5Map = new HashMap<String, String>();
			md5Map.put("DataMax Apex 2 in", "648a2ef2e3a87404904242200a40eddb");
			md5Map.put("DataMax Apex 3 in", "ab88b313b773f25477e331291b6e1d18");
			md5Map.put("DataMax Apex 4 in", "67ef56dac80188628aeebbb9dc6e72f7");
			md5Map.put("DataMax Andes 3 in", "ab88b313b773f25477e331291b6e1d18");
			md5Map.put("Generic 2 in", "2ec6717c55fbea93345c9def5f14185e");
			md5Map.put("Generic 3 in", "9c9667d8a6a7e1eee63b5b4f1643c7a9");
			md5Map.put("Generic 4 in", "935537b3e3a49a41162629af4d43ea68");
			md5Map.put("Porti S 2 in", "2ec6717c55fbea93345c9def5f14185e");
			md5Map.put("Porti S 3 in", "9c9667d8a6a7e1eee63b5b4f1643c7a9");
			md5Map.put("Porti S 4 in", "935537b3e3a49a41162629af4d43ea68");
			md5Map.put("Star Micronics 2 in", "2ec6717c55fbea93345c9def5f14185e");
			md5Map.put("Star Micronics 3 in", "9c9667d8a6a7e1eee63b5b4f1643c7a9");
			md5Map.put("Star Micronics 4 in", "935537b3e3a49a41162629af4d43ea68");
			md5Map.put("Zebra MZ220 2 in", "b55281dace311087a3e372b834b54cfc");
			md5Map.put("Zebra MZ320 3 in", "3b96a0478deb8a5b8494e523d7ce153c");
		}
		return md5Map;
	}
	
	
	private Map<String, String> md5MapPageable() {
		if (md5MapPageable == null) {
			md5MapPageable = new HashMap<String, String>();
			md5MapPageable.put("DataMax Apex 2 in", "2843600f2535cd302ae585c8c9bdada3");
			md5MapPageable.put("DataMax Apex 3 in", "75be7e5d492a18eead44d8794aa770cc");
			md5MapPageable.put("DataMax Apex 4 in", "a06af7463be223dab7d13c827c30946e");
			md5MapPageable.put("DataMax Andes 3 in", "75be7e5d492a18eead44d8794aa770cc");
			md5MapPageable.put("Generic 2 in", "69aa252692f07530cc48cef3b8934df9");
			md5MapPageable.put("Generic 3 in", "8b7ec401b1ccd18424600a4fa1b9cb48");
			md5MapPageable.put("Generic 4 in", "11bb6152ff5d51272a19d9ebb14aba9e");
			md5MapPageable.put("Porti S 2 in", "69aa252692f07530cc48cef3b8934df9");
			md5MapPageable.put("Porti S 3 in", "8b7ec401b1ccd18424600a4fa1b9cb48");
			md5MapPageable.put("Porti S 4 in", "11bb6152ff5d51272a19d9ebb14aba9e");
			md5MapPageable.put("Star Micronics 2 in", "69aa252692f07530cc48cef3b8934df9");
			md5MapPageable.put("Star Micronics 3 in", "8b7ec401b1ccd18424600a4fa1b9cb48");
			md5MapPageable.put("Star Micronics 4 in", "11bb6152ff5d51272a19d9ebb14aba9e");
			md5MapPageable.put("Zebra MZ220 2 in", "f18e7dd690385e7f878e18950231d4c6");
			md5MapPageable.put("Zebra MZ320 3 in", "1f3423d5389c7641ed5a28ae3daabbed");
		}
		return md5MapPageable;
	}
}
