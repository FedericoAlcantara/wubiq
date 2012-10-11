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
	private PrintingStatus printingStatus; 
	
	/**
	 * Outputs to a star micronics portable printer.
	 * @param deviceInfo Device information
	 * @param deviceAddress Device address (mac address)
	 * @param printData Data to print
	 * @param printDelay Delays to apply between data chunks
	 * @param printPause Pause after print job is finished.
	 * @return true if everything is okey.
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

	@Override
	public void run() {
		print();
	}

	/**
	 * Process the printing.
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
