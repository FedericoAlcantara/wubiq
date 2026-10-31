package net.sf.wubiq.android.devices;

import net.sf.wubiq.android.MobileDeviceInfo;
import net.sf.wubiq.android.enums.PrintingStatus;

/**
 * Defines the implementation needed for a wubiq enabled device.
 * @author Federico Alcantara
 *
 */
public abstract class BaseWubiqDevice extends Thread {
	private MobileDeviceInfo mobileDeviceInfo;
	private String deviceAddress;
	private byte[] printData;
	private int printDelay;
	private int printPause;
	private int copies = 1;
	private int pauseBetweenCopies;
	private PrintingStatus printingStatus;
	
	/**
	 * Outputs to a star micronics portable printer.
	 * @param mobileDeviceInfo Device information
	 * @param deviceAddress Device address (mac address)
	 * @param printData Data to print
	 * @param printDelay Delays to apply between data chunks
	 * @param printPause Pause after print job is finished.
	 */
	public void initialize(MobileDeviceInfo mobileDeviceInfo, String deviceAddress,
			byte[] printData, int printDelay, int printPause) {
		this.mobileDeviceInfo = mobileDeviceInfo;
		this.deviceAddress = deviceAddress;
		this.printData = printData;
		this.printDelay = printDelay;
		this.printPause = printPause;
		this.printingStatus = PrintingStatus.PRINTING;
	}

	/**
	 * Prepares the device to print the data once per copy.
	 * @param mobileDeviceInfo Device information
	 * @param deviceAddress Device address (mac address)
	 * @param printData Data to print (one copy)
	 * @param printDelay Delays to apply between data chunks
	 * @param printPause Pause after print job is finished.
	 * @param copies Number of copies to print, at least 1.
	 * @param pauseBetweenCopies Milliseconds to wait, with the connection open, between one copy and the next; only used by devices that call waitBetweenCopies().
	 */
	public void initialize(MobileDeviceInfo mobileDeviceInfo, String deviceAddress,
			byte[] printData, int printDelay, int printPause, int copies, int pauseBetweenCopies) {
		initialize(mobileDeviceInfo, deviceAddress, printData, printDelay, printPause);
		this.copies = Math.max(1, copies);
		this.pauseBetweenCopies = Math.max(0, pauseBetweenCopies);
	}
	
	@Override
	public void run() {
		print();
	}

	/**
	 * Waits the pause between copies. Call it between sending one copy and the next, on the same connection.
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 */
	protected void waitBetweenCopies() throws InterruptedException {
		if (pauseBetweenCopies > 0) {
			Thread.sleep(pauseBetweenCopies);
		}
	}

	/**
	 * @return Number of copies to print, at least 1.
	 */
	public int getCopies() {
		return copies;
	}

	/**
	 * Process the printing: opens the connection once and sends every copy through it.
	 * @return True if printing was okey, false otherwise.
	 */
	protected abstract boolean print();

	/**
	 * @return the mobileDeviceInfo
	 */
	public MobileDeviceInfo getMobileDeviceInfo() {
		return mobileDeviceInfo;
	}

	/**
	 * @param mobileDeviceInfo the mobileDeviceInfo to set
	 */
	public void setMobileDeviceInfo(MobileDeviceInfo mobileDeviceInfo) {
		this.mobileDeviceInfo = mobileDeviceInfo;
	}

	/**
	 * @return the deviceAddress
	 */
	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * @param deviceAddress the deviceAddress to set
	 */
	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	/**
	 * @return the printData
	 */
	public byte[] getPrintData() {
		return printData;
	}

	/**
	 * @param printData the printData to set
	 */
	public void setPrintData(byte[] printData) {
		this.printData = printData;
	}

	/**
	 * @return the printDelay
	 */
	public int getPrintDelay() {
		return printDelay;
	}

	/**
	 * @param printDelay the printDelay to set
	 */
	public void setPrintDelay(int printDelay) {
		this.printDelay = printDelay;
	}

	/**
	 * @return the printPause
	 */
	public int getPrintPause() {
		return printPause;
	}

	/**
	 * @param printPause the printPause to set
	 */
	public void setPrintPause(int printPause) {
		this.printPause = printPause;
	}

	/**
	 * @return the printingStatus
	 */
	public PrintingStatus getPrintingStatus() {
		return printingStatus;
	}

	/**
	 * @param printingStatus the printingStatus to set
	 */
	public void setPrintingStatus(PrintingStatus printingStatus) {
		this.printingStatus = printingStatus;
	}

}
