/**
 * 
 */
package net.sf.wubiq.clients;


import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.sf.wubiq.android.PrintClientUtils;
import net.sf.wubiq.android.PrintManagerService;
import net.sf.wubiq.android.R;
import net.sf.wubiq.android.WubiqActivity;
import net.sf.wubiq.android.devices.DeviceForTesting;
import net.sf.wubiq.android.enums.NotificationIds;
import net.sf.wubiq.android.utils.BluetoothUtils;
import net.sf.wubiq.android.utils.NotificationUtils;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

/**
 * Manage the document printing between the server and the client.
 * @author Federico Alcantara
 *
 */
public class BluetoothPrintManager extends AbstractLocalPrintManager {
	private final String TAG = BluetoothPrintManager.class.getSimpleName();
	protected Resources resources;
	protected SharedPreferences preferences;
	protected Context context;
	private Map<String, String> compressionMap;
	private Map<String, String> printServicesName;
	private int printingErrors = 0;
	private boolean needsRefresh = true;
	private List<String> currentPendingJobs;

	/**
	 * Create a new instance of the bluetooth print manager.
	 * @param context Context.
	 * @param resources Resources.
	 * @param preferences Preferences.
	 */
	public BluetoothPrintManager(Context context, Resources resources, SharedPreferences preferences) {
		this.resources = resources;
		this.preferences = preferences;
		this.context = context;
		printServicesName = new HashMap<String, String>();
		currentPendingJobs = new ArrayList<String>();

		initializeDefault(this);
		setCheckPendingJobInterval(preferences.getInt(WubiqActivity.PRINT_POLL_INTERVAL_KEY, resources.getInteger(R.integer.print_poll_interval_default)));
		setPrintingJobInterval(preferences.getInt(WubiqActivity.PRINT_PAUSE_BETWEEN_JOBS_KEY, resources.getInteger(R.integer.print_pause_between_jobs_default)));
		setConnectionErrorRetries(preferences.getInt(WubiqActivity.PRINT_CONNECTION_ERRORS_RETRY_KEY, resources.getInteger(R.integer.print_connection_errors_retries_default)));
		String host = preferences.getString(WubiqActivity.HOST_KEY, resources.getString(R.string.server_host_default));
		String port = preferences.getString(WubiqActivity.PORT_KEY, resources.getString(R.string.server_port_default));
		String connectionsString = preferences.getString(WubiqActivity.CONNECTIONS_KEY, resources.getString(R.string.server_connection_default));
		addConnectionsString(this, hostPortConnection(host, port));
		addConnectionsString(this, connectionsString);
	}
	
	/**
	 * Bluetooth print service registration.
	 */
	@Override
	protected void registerPrintServices() throws ConnectException {
		registerComputerName();
		// Gather printServices.
		doLog("Register Print Services for Wubiq Android: " + Labels.VERSION);
		BluetoothAdapter adapter = BluetoothUtils.getAdapter(context);
		printServicesName.clear(); // Release all services
		if (preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false)) {
			for (int index = 1; index <= DeviceForTesting.TEST_DEVICE_COUNT; index++) {
				String suffix = index != 1 ? "_" + index : "";
				String address = DeviceForTesting.TEST_DEVICE_ADDRESS + suffix;
				registerPrintService(DeviceForTesting.TEST_DEVICE_NAME + suffix,
						address);
			}
		}
		if (adapter != null) {
			for (BluetoothDevice device : adapter.getBondedDevices()) {
				registerPrintService(device.getName(), device.getAddress());
			}
		}
	}
	
	/**
	 * Registers a print service.
	 * @param deviceName Name of the device.
	 * @param deviceAddress Address of the device.
	 * @throws ConnectException Thrown if can't be registered to the server.
	 */
	private void registerPrintService(String deviceName, String deviceAddress) throws ConnectException {
		String deviceKey = WubiqActivity.DEVICE_PREFIX + deviceAddress;
		String selection = preferences.getString(deviceKey, null);
		if (selection != null && !selection.equals("--")) {
			StringBuffer printServiceRegister = new StringBuffer(PrintClientUtils.INSTANCE.serializeServiceName(deviceName, deviceAddress, selection));
			printServicesName.put(printServiceRegister.toString(), deviceAddress);
			printServiceRegister.insert(0, ParameterKeys.PARAMETER_SEPARATOR);
			printServiceRegister.insert(0, ParameterKeys.PRINT_SERVICE_NAME);
			StringBuffer categories = new StringBuffer(serializePrintServiceCategories());
			categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
					.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
			askServer(CommandKeys.REGISTER_MOBILE_PRINT_SERVICE, printServiceRegister.toString(), categories.toString());
			needsRefresh = false;
		}		
	}
	
	/**
	 * @see net.sf.wubiq.clients.AbstractLocalPrintManager#needsRefresh()
	 */
	@Override
	protected boolean needsRefresh() {
		if (!needsRefresh) {
			needsRefresh = printServicesName.isEmpty()
					|| preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false);
		}
		return needsRefresh;
	}
	
	/**
	 * @see net.sf.wubiq.clients.AbstractLocalPrintManager#getPendingJobs()
	 */
	@Override
	protected String[] getPendingJobs() throws ConnectException {
		if (currentPendingJobs.isEmpty()) {
			String[] serverPendingJobs = super.getPendingJobs();
			for (String pendingJob : serverPendingJobs) {
				if (!Is.emptyString(pendingJob)) {
					currentPendingJobs.add(pendingJob);
				}
			}
		}
		String[] returnValue = new String[currentPendingJobs.size()];
		returnValue = currentPendingJobs.toArray(returnValue);
		NotificationUtils.INSTANCE.cancelNotification(context,
				NotificationIds.CONNECTION_ERROR_ID);
		PrintManagerService.connectionErrors = 0;
		if (returnValue != null && returnValue.length > 0) {
			NotificationUtils.INSTANCE.notify(context, NotificationIds.PRINTING_INFO_ID,
					returnValue.length,
					context.getString(R.string.info_printing));
		} else {
			NotificationUtils.INSTANCE.cancelNotification(context, NotificationIds.PRINTING_INFO_ID);
		}
		return returnValue;
	}
	
	/**
	 * Print pending jobs to the client.
	 */
	@Override
	protected void processPendingJob(String jobId, String printServiceName) throws ConnectException {
		String parameter = printJobString(jobId);
		doLog("Process Pending Job:" + jobId);
		InputStream stream = null;
		boolean closePrintJob = false;

		try {			
			if (printServiceName != null && !printServiceName.equals("") &&
					printServicesName.containsKey(printServiceName)) {
				boolean process = BluetoothUtils.device(context, printServicesName.get(printServiceName)) != null;
				if (!process 
						&& preferences.getBoolean(PropertyKeys.WUBIQ_DEVELOPMENT_MODE, false)) {
					String prefix = DeviceForTesting.TEST_DEVICE_NAME
							+ ParameterKeys.ATTRIBUTE_SET_SEPARATOR
							+ DeviceForTesting.TEST_DEVICE_ADDRESS
							+ ParameterKeys.ATTRIBUTE_SET_SEPARATOR;
					if (printServiceName.startsWith(prefix)) {
						process = true;
					}
				}
				if (process) {
					doLog("Job(" + jobId + ") printServiceName:" + printServiceName);
					String attributesData = askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter);
					doLog("Job(" + jobId + ") attributesData:" + attributesData);
					stream = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter);
					doLog("Job(" + jobId + ") stream:" + stream);
					doLog("Job(" + jobId + ") print pdf");
					closePrintJob = PrintClientUtils.INSTANCE.print(context, printServicesName.get(printServiceName), stream, resources, preferences);
				}
			}
			if (closePrintJob) {
				closePrintJob(jobId);
				BluetoothUtils.cancelError(context);
				NotificationUtils.INSTANCE.cancelNotification(context, NotificationIds.PRINTING_ERROR_ID);
				printingErrors = 0;
				currentPendingJobs.remove(jobId);
			} else {
				BluetoothUtils.notifyError(context);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage() != null ? e.getMessage() : "Unspecified error:" + e);
			NotificationUtils.INSTANCE.notify(context, 
					NotificationIds.PRINTING_ERROR_ID, printingErrors++,
					e.getMessage());
		} finally {
			NotificationUtils.INSTANCE.cancelNotification(context, NotificationIds.PRINTING_INFO_ID);
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				doLog(e.getMessage());
			}
		}
	}
	
	/**
	 * Closes a print job.
	 * @param jobId Job id of print job to be closed.
	 * @throws ConnectException
	 */
	protected void closePrintJob(String jobId) {
		try {
			doLog("Job(" + jobId + ") printed.");
			askServer(CommandKeys.CLOSE_PRINT_JOB, printJobString(jobId));
			doLog("Job(" + jobId + ") close print job.");
		} catch (Exception e) {
			doLog("Job(" + jobId + ") error on closing:" + e.getMessage());
		}
	}

	/**
	 * String for accessing a print job.
	 * @param jobId Print Job id.
	 * @return String for print job access.
	 */
	protected String printJobString(String jobId) {
		StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(jobId);
		return parameter.toString();
	}
	
		
	@Override
	public String getUuid() {
		return preferences.getString(WubiqActivity.UUID_KEY, UUID.randomUUID().toString());
	}
	
	/** 
	 * Public for testing purposes.
	 */
	@Override
	public String askServer(String command, String... parameters)
			throws ConnectException {
		try {
			return super.askServer(command, parameters);
		} catch (ConnectException e) {
			needsRefresh = true;
			throw e;
		}
	}


	/**
	 * Serializes the categories of the device.
	 * @return List of categories in serialized form.
	 */
	private String serializePrintServiceCategories() {
		StringBuffer categories = new StringBuffer("" +
				"javax.print.attribute.standard.Copies=javax.print.attribute.standard.CopiesSupported:S:1,9999;" +
				"javax.print.attribute.standard.Finishings=javax.print.attribute.standard.Finishings:E:NONE;" +
				"javax.print.attribute.standard.JobSheets=javax.print.attribute.standard.JobSheets:E:NONE/javax.print.attribute.standard.JobSheets:E:STANDARD;" +
				"javax.print.attribute.standard.Media=sun.print.CustomMediaSizeName:E:CUSTOM/" +
					"javax.print.attribute.standard.MediaSizeName:E:NA_LETTER/" +
					"javax.print.attribute.standard.MediaSizeName:E:NA_LEGAL/" +
					"javax.print.attribute.standard.MediaSizeName:E:NA_8X10;" +
				"javax.print.attribute.standard.NumberUp=javax.print.attribute.standard.NumberUp:I:1/" +
					"javax.print.attribute.standard.NumberUp:I:2/" +
					"javax.print.attribute.standard.NumberUp:I:4/" +
					"javax.print.attribute.standard.NumberUp:I:6/" +
					"javax.print.attribute.standard.NumberUp:I:9/" +
					"javax.print.attribute.standard.NumberUp:I:16;" +
				"javax.print.attribute.standard.OrientationRequested=javax.print.attribute.standard.OrientationRequested:E:PORTRAIT/" +
					"javax.print.attribute.standard.OrientationRequested:E:LANDSCAPE/" +
					"javax.print.attribute.standard.OrientationRequested:E:REVERSE_LANDSCAPE/" +
					"javax.print.attribute.standard.OrientationRequested:E:REVERSE_PORTRAIT;" +
				"javax.print.attribute.standard.PageRanges=javax.print.attribute.standard.PageRanges:S:1,2147483647;" +
				"javax.print.attribute.standard.Sides=javax.print.attribute.standard.Sides:E:ONE_SIDED;" +
				"javax.print.attribute.standard.MediaPrintableArea=javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,209.55,279.4/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,209.55,355.6/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,177.8,266.7/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,93.486,146.756/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,109.714,116.064/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,58.561,64.911/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,113.594,119.944/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,69.85,127.0/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,82.55,127.0/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,95.25,152.4/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,15.875,107.95,168.275/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,120.65,177.8/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,120.65,203.2/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,146.05,203.2/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,196.85,254.0/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,133.35,215.9/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,196.85,304.8/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:3.528,11.642,206.375,297.039/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,141.817,209.903/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,98.425,148.167/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,67.733,104.775/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,45.861,74.083/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,30.692,52.211/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,19.403,37.042/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,208.492,304.8/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,169.333,249.767/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,118.533,175.683/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,81.492,124.883/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,55.386,87.842/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,37.394,61.736/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,24.342,43.744/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,176.389,256.469/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,121.356,182.739/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,84.314,127.706/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,57.15,90.664/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,38.453,63.5/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,25.4,44.803/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,155.575,228.953/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,118.533,323.85/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,107.597,161.925/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,155.575,113.947/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,103.364,219.781/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,74.436,161.925/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,155.575,80.786/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,74.436,113.947/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,107.597,80.786/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,50.447,80.786/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,74.436,56.797/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,33.514,56.797/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,50.447,39.864/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,21.519,39.864/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,33.514,27.869/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,209.55,330.2/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,182.386,245.886/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,194.381,257.881/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,116.417,185.914/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,122.414,197.908/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,131.586,215.9/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,149.578,233.892/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,104.422,177.8/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,104.422,180.975/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,93.486,149.931/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,93.486,148.167/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,141.817,200.025/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,113.594,234.95/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,83.608,204.964/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,198.614,89.958/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,98.425,241.3/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,104.775,146.05/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,92.075,190.5/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,184.15,98.425/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,95.25,136.525/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,95.25,177.8/" +
					"javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,82.903,118.886/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,48.331,86.078/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,93.486,199.672/javax.print.attribute.standard.MediaPrintableArea:A:6.35,11.642,209.55,329.847;" +
				"javax.print.attribute.standard.Destination=javax.print.attribute.standard.Destination:;" +
				"javax.print.attribute.standard.Chromaticity=javax.print.attribute.standard.Chromaticity:E:MONOCHROME/javax.print.attribute.standard.Chromaticity:E:COLOR");
		return compressAttributes(categories.toString());
	}
	
	/**
	 * Replaces all attributes and categories for a compressed representation.
	 * @param attributeList List of attributes to compress.
	 * @return Compressed attributes.
	 */
	protected String compressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getKey(), entry.getValue());
		}
		return returnValue;
	}
	
	/**
	 * Converts a previously compressed attributes list to the original form.
	 * @param attributeList Attribute list.
	 * @return Decompressed files.
	 */
	protected String deCompressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getValue(), entry.getKey());
		}
		return returnValue;
	}

	/**
	 * Map for compression.
	 * @return Map with pair for translation.
	 */
	private  Map<String, String>getCompressionMap() {
		if (compressionMap == null) {
			compressionMap = new LinkedHashMap<String, String>();
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.MediaPrintableArea", "xMPAx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.MediaSizeName", "xMSNx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.NumberUp", "xNUPx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.OrientationRequested", "xORQx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Sides", "xSIDx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.PageRanges", "xPRAx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.JobSheets", "xJSHx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Finishings", "xFINx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.CopiesSupported", "xCSUx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Chromaticity", "xCHRx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Destination", "xDSTx");
			compressionMap.put("sun\\.print\\.CustomMediaSizeName", "xCMSx");
		}
		return compressionMap;
	}

	/**
	 * List of print services names.
	 * @return Print services names.
	 */
	protected Map<String, String> getPrintServicesName() {
		return printServicesName;
	}

	@Override
	protected void doLog(Object message) {
		Log.v(TAG, message.toString());
	}
	
	@Override
	protected void doLog(Object message, int logLevel) {
		switch(logLevel) {
			case 0:
			case 1:
			case 2:
				Log.i(TAG, message.toString());
				break;
			case 3:
				Log.d(TAG, message.toString());
				break;
			default:
				Log.v(TAG, message.toString());
				
		}
	}
}
