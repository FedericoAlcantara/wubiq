package net.sf.wubiq.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.clients.AbstractLocalPrintManager;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.clients.LocalPrintManager;

public class LocalManagerTestWrapper extends LocalPrintManager implements Serializable {
	private static final long serialVersionUID = 1L;

	private TestData testData;
	
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
	public Map<String, PrintService> getPrintServicesName() {
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
	protected void processPendingJob(String jobId) throws ConnectException {
		getTestData().setJobId(jobId);
		try {
			super.processPendingJob(jobId);
		} catch (ConnectException e) {
			getTestData().setErrors(true);
			throw e;
		} catch (Exception e) {
			getTestData().setErrors(true);
			throw new ConnectException(e.getMessage());
		}
	}
	@Override
	protected boolean forceSerializedBySystem() {
		return getTestData().isForceSerializedBySystem();
	}
	
	@Override
	protected DirectPrintManager createDirectPrintManager(String jobIdString,
			PrintService printService,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet, boolean debugMode, int debugLevel,
			boolean serverSupportsCompression) {
		return new DirectManagerTestWrapper(this, jobIdString, printService,
				printRequestAttributeSet, printJobAttributeSet, docAttributeSet,
				debugMode, debugLevel, serverSupportsCompression);
	}
	
	@Override
	protected DirectPrintManager createDirectPrintManager(String jobIdString,
			PrintService printService,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet, boolean debugMode, int debugLevel,
			boolean serverSupportsCompression, DocFlavor docFlavor,
			InputStream printData) {
		return new DirectManagerTestWrapper(this, jobIdString, printService,
				printRequestAttributeSet, printJobAttributeSet, docAttributeSet,
				debugMode, debugLevel, serverSupportsCompression, docFlavor, printData);
	}
	
	@Override
	protected void runManager(DirectPrintManager manager,
			String printServiceName, String jobId) {
		manager.run(); // To avoid a thread;
	}
	
	@Override
	protected void print(String jobId, PrintService printService,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet, DocFlavor docFlavor,
			InputStream printData) throws IOException {
		try {
			super.print(jobId, printService, printRequestAttributeSet,
					printJobAttributeSet, docAttributeSet, docFlavor, printData);
			getTestData().setLocalDocFlavor(docFlavor);
			getTestData().setLocalManagerCalled(true);
		} catch (IOException e) {
			getTestData().setErrors(true);
			throw e;
		} catch (Exception e) {
			getTestData().setErrors(true);
			throw new IOException(e.getMessage());
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
	public void addConnections(String connectionsString) {
		AbstractLocalPrintManager.addConnectionsString(this, connectionsString);
	}
	
	/**
	 * Initializes
	 */
	public void initializeDefaults() {
		AbstractLocalPrintManager.initializeDefault(this);
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
	public synchronized TestData getTestData() {
		if (testData == null) {
			testData = new TestData();
		}
		return testData;
	}

	/**
	 * Reset the test data.
	 */
	public synchronized void resetTestData() {
		this.testData = null;
	}
}
