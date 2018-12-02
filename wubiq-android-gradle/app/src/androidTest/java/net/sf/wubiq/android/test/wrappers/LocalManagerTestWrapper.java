package net.sf.wubiq.android.test.wrappers;

import java.io.Serializable;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.wubiq.android.test.AndroidTestProperties;
import net.sf.wubiq.clients.AbstractLocalPrintManager;
import net.sf.wubiq.clients.BluetoothPrintManager;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.utils.Is;

import org.jsoup.Jsoup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

public class LocalManagerTestWrapper extends BluetoothPrintManager implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = LocalManagerTestWrapper.class.getSimpleName();
	
	private static Set<String> connections;
	
	private TestData testData;

	private boolean stopProcessing;

	
	public LocalManagerTestWrapper(Context context, Resources resources,
			SharedPreferences preferences) {
		super(context, resources, preferences);
	}
	
	@Override
	public boolean isKilled() {
		return super.isKilled();
	}
	
	@Override
	public void bringAlive() {
		super.bringAlive();
	}
	
	@Override
	public void registerPrintServices() throws ConnectException {
		super.registerPrintServices();
		getTestData().setRegisteredServices(true);
	}	
		
	@Override
	public Map<String, String> getPrintServicesName() {
		return super.getPrintServicesName();
	}
	
	@Override
	public String[] getPendingJobs() throws ConnectException {
		try {
			getTestData().setPendingJobs(super.getPendingJobs());
		} catch (Exception e) {
			getTestData().setErrors(true);
			throw new ConnectException(e.getMessage());
		}
		return getTestData().getPendingJobs();
	}
	
	@Override
	protected void processPendingJob(String jobId, String printServiceName) throws ConnectException {
		getTestData().setJobId(jobId);
		if (stopProcessing) {
			return;
		}
		try {
			super.processPendingJob(jobId, printServiceName);
		} catch (ConnectException e) {
			getTestData().setErrors(true);
			throw e;
		} catch (Exception e) {
			getTestData().setErrors(true);
			throw new ConnectException(e.getMessage());
		}
	}
	
	@Override
	public void killManager() {
		super.killManager();
	}
	
	public String askServer(String command, String... parameters)
			throws ConnectException {
		return super.askServer(command, parameters);
	}
	
	public String askTestServer(String command, String... parameters)
			throws ConnectException {
		String returnValue = super.askServer(command, parameters);
		return returnValue;
	}

	@Override
	public Object pollServer(String command, String... parameters)
			throws ConnectException {
		preferredURL = null;
		Object returnValue = null;
		try {
			returnValue = super.pollServer(command, parameters);
		} catch (ConnectException e) {
			getTestData().setErrors(true);
			throw e;
		} catch (Exception e) {
			getTestData().setErrors(true);
			throw new ConnectException(e.getMessage());
		}
		return returnValue;
	}
	
	@Override
	public void setApplicationName(String applicationName) {
		super.setApplicationName(applicationName);
	}

	@Override
	public void setServletName(String servletName) {
		super.setServletName(servletName);
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
	
	/**
	 * Adds connections to this test manager.
	 * @param connectionsString
	 */
	public synchronized void addConnections(String connectionsString) {
		if (!connectionsString.contains("/localhost")) {
			AbstractLocalPrintManager.addConnectionsString(this, connectionsString);
		}
	}
	
	/**
	 * Initializes
	 */
	public void initializeDefaults() {
		AbstractLocalPrintManager.initializeDefault(this);
		setUuid(AndroidTestProperties.get(ConfigurationKeys.PROPERTY_UUID, ""));
		setGroups(AndroidTestProperties.get(ConfigurationKeys.PROPERTY_GROUPS, ""));
	}
	
	/**
	 * @see net.sf.wubiq.clients.AbstractLocalPrintManager#getConnections()
	 */
	@Override
	public synchronized Set<String> getConnections() {
		if (connections == null) {
			connections = new HashSet<String>();
			for (String connection : super.getConnections()) {
				if (!connection.contains("localhost")) {
					connections.add(connection);
				}
			}
			/** ports for testing the connections **/
			String[] ports = {":8080", ""};
			try {
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				outer:
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface networkInterface = networkInterfaces.nextElement();
					Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress address = addresses.nextElement();
						if (address.getAddress().length == 4 &&
								!"127.0.0.1".equals(address.getHostAddress().trim())) { // IPv4
							String hostAddress = address.getHostAddress().trim();
							// We know that a android is not a server so this address must 
							// be part of the emulator host (if running on emulator)
							int ignore = Integer.parseInt(hostAddress.substring(hostAddress.lastIndexOf('.') + 1));
							hostAddress = hostAddress.substring(0, hostAddress.lastIndexOf('.')) + ".";
							for (String port : ports) {
								// Let's try each connection in the network
								for (int suffix = 1; suffix < 256; suffix++) {
									if (suffix != ignore) {
										try {
											String addressToCheck = "http://" 
													+ hostAddress
													+ suffix
													+ port;
											Jsoup.connect(addressToCheck
													+ "/" + getApplicationName()
													+ "/wubiq-print-test.do?"
													+ ParameterKeys.COMMAND
													+ ParameterKeys.PARAMETER_SEPARATOR
													+ CommandKeys.SHOW_PRINT_SERVICES).get();
											connections.add(addressToCheck);
											break outer;
										} catch (Throwable e) {
											doLog(e.getMessage());
										}
									}
								}
							}
						}
					}
				}
			} catch (SocketException e) {
				doLog(e.getMessage());
			}
		}
		Iterator<String> connectionsIt = connections.iterator();
		List<String> toBeDeleted = new ArrayList<String>();
		while (connectionsIt.hasNext()) {
			String connection = connectionsIt.next();
			if (Is.emptyString(connection) || connection.contains("/localhost")) {
				toBeDeleted.add(connection);
			}
		}
		for (String toDelete : toBeDeleted) {
			connections.remove(toDelete);
		}
		return connections;
	}
	
	@Override
	public Set<URL> getUrls() {
		return super.getUrls();
	}
	
	@Override
	public String getEncodedParameters(String command, String... parameters) {
		return super.getEncodedParameters(command, parameters);
	}

	/**
	 * @return the testData.
	 */
	public TestData getTestData() {
		if (testData == null) {
			testData = new TestData();
		}
		return testData;
	}

	@Override
	public void closePrintJob(String jobId) {
		super.closePrintJob(jobId);
	}
	
	/**
	 * Reset the test data.
	 */
	public void resetTestData() {
		this.testData = null;
	}
	
	/**
	 * Allows the process of print jobs or not.
	 * @param status Status to set. True no print jobs are processed, false (default) all print jobs are processed.
	 */
	public void stopProcessing(boolean status) {
		this.stopProcessing = status;
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
