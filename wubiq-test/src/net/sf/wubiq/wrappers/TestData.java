/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.io.Serializable;

import javax.print.DocFlavor;


/**
 * Holds the data which is gathered during test running.
 * @author Federico Alcantara
 *
 */
public class TestData implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean registeredServices;
	private String[] pendingJobs;
	private String jobId;
	private boolean localManagerCalled;
	private DocFlavor localDocFlavor;
	private boolean directManagerCalled;
	private boolean directPrintPrintable;
	private boolean directPrintPageable;
	private int directPageableNumberOfPages;
	private int directPrintableGraphicsCommandCount;
	private boolean forceSerializedBySystem;
	
	private boolean errors;
	
	public TestData() {
		pendingJobs = new String[]{};
		jobId = null;
		localManagerCalled = false;
		directManagerCalled = false;
		errors = false;
	}

	/**
	 * @return the registeredServices
	 */
	public boolean isRegisteredServices() {
		return registeredServices;
	}

	/**
	 * @param registeredServices the registeredServices to set
	 */
	public void setRegisteredServices(boolean registeredServices) {
		this.registeredServices = registeredServices;
	}

	/**
	 * @return the pendingJobs
	 */
	public String[] getPendingJobs() {
		return pendingJobs;
	}

	/**
	 * @param pendingJobs the pendingJobs to set
	 */
	public void setPendingJobs(String[] pendingJobs) {
		this.pendingJobs = pendingJobs;
	}

	/**
	 * @return the jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return the localManagerCalled
	 */
	public boolean isLocalManagerCalled() {
		return localManagerCalled;
	}

	/**
	 * @param localManagerCalled the localManagerCalled to set
	 */
	public void setLocalManagerCalled(boolean directPrintExecuted) {
		this.localManagerCalled = directPrintExecuted;
	}

	/**
	 * @return the localDocFlavor
	 */
	public DocFlavor getLocalDocFlavor() {
		return localDocFlavor;
	}

	/**
	 * @param localDocFlavor the localDocFlavor to set
	 */
	public void setLocalDocFlavor(DocFlavor localDocFlavor) {
		this.localDocFlavor = localDocFlavor;
	}

	/**
	 * @return the directManagerCalled
	 */
	public boolean isDirectManagerCalled() {
		return directManagerCalled;
	}

	/**
	 * @param directManagerCalled the directManagerCalled to set
	 */
	public void setDirectManagerCalled(boolean directManagerCalled) {
		this.directManagerCalled = directManagerCalled;
	}

	/**
	 * @return the directPrintPrintable
	 */
	public boolean isDirectPrintPrintable() {
		return directPrintPrintable;
	}

	/**
	 * @param directPrintPrintable the directPrintPrintable to set
	 */
	public void setDirectPrintPrintable(boolean directPrintPrintable) {
		this.directPrintPrintable = directPrintPrintable;
	}

	/**
	 * @return the directPrintPageable
	 */
	public boolean isDirectPrintPageable() {
		return directPrintPageable;
	}

	/**
	 * @param directPrintPageable the directPrintPageable to set
	 */
	public void setDirectPrintPageable(boolean directPageable) {
		this.directPrintPageable = directPageable;
	}

	/**
	 * @return the directPageableNumberOfPages
	 */
	public int getDirectPageableNumberOfPages() {
		return directPageableNumberOfPages;
	}

	/**
	 * @param directPageableNumberOfPages the directPageableNumberOfPages to set
	 */
	public void setDirectPageableNumberOfPages(int directPageableNumberOfPages) {
		this.directPageableNumberOfPages = directPageableNumberOfPages;
	}

	/**
	 * @return the directPrintableGraphicsCommandCount
	 */
	public int getDirectPrintableGraphicsCommandCount() {
		return directPrintableGraphicsCommandCount;
	}

	/**
	 * @param directPrintableGraphicsCommandCount the directPrintableGraphicsCommandCount to set
	 */
	public void setDirectPrintableGraphicsCommandCount(
			int directPrintableGraphicsCommandCount) {
		this.directPrintableGraphicsCommandCount = directPrintableGraphicsCommandCount;
	}

	/**
	 * @return the errors
	 */
	public boolean isErrors() {
		return errors;
	}

	/**
	 * @param errors the errors to set
	 */
	public void setErrors(boolean errors) {
		this.errors = errors;
	}

	/**
	 * @return the forceSerializedBySystem
	 */
	public boolean isForceSerializedBySystem() {
		return forceSerializedBySystem;
	}

	/**
	 * @param forceSerializedBySystem the forceSerializedBySystem to set
	 */
	public void setForceSerializedBySystem(boolean forceSystemSerialization) {
		this.forceSerializedBySystem = forceSystemSerialization;
	}

}
