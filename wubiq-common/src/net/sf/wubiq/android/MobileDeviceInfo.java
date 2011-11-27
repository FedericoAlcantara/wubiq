/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines the characteristics of a device
 * including processing steps and compatibility
 * @author Federico Alcantara
 *
 */
public class MobileDeviceInfo {
	private String name;
	private Collection<String> compatibleDevices;
	private Integer maxHorPixels;
	private boolean colorCapable;
	private Map<MobileConversionHint, Object> hints;
	
	/**
	 * Order is important in this object
	 */
	private ArrayList<MobileServerConversionStep> serverSteps ;

	/**
	 * Order is important in this object
	 */
	private ArrayList<MobileClientConversionStep> clientSteps ;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the compatibleDevices
	 */
	public Collection<String> getCompatibleDevices() {
		if (compatibleDevices == null) {
			compatibleDevices = new ArrayList<String>();
		}
		return compatibleDevices;
	}

	/**
	 * @param compatibleDevices the compatibleDevices to set
	 */
	public void setCompatibleDevices(Collection<String> compatibleDevices) {
		this.compatibleDevices = compatibleDevices;
	}

	/**
	 * @return the maxHorPixels
	 */
	public Integer getMaxHorPixels() {
		return maxHorPixels;
	}

	/**
	 * @param maxHorPixels the maxHorPixels to set
	 */
	public void setMaxHorPixels(Integer maxHorPixels) {
		this.maxHorPixels = maxHorPixels;
	}

	/**
	 * @return the colorCapable
	 */
	public boolean isColorCapable() {
		return colorCapable;
	}

	/**
	 * @param colorCapable the colorCapable to set
	 */
	public void setColorCapable(boolean colorCapable) {
		this.colorCapable = colorCapable;
	}

	/**
	 * @return the hints
	 */
	public Map<MobileConversionHint, Object> getHints() {
		if (hints == null) {
			hints = new HashMap<MobileConversionHint, Object>();
		}
		return hints;
	}

	/**
	 * @param hints the hints to set
	 */
	public void setHints(Map<MobileConversionHint, Object> hints) {
		this.hints = hints;
	}

	/**
	 * @return the serverSteps
	 */
	public ArrayList<MobileServerConversionStep> getServerSteps() {
		if (serverSteps == null) {
			serverSteps = new ArrayList<MobileServerConversionStep>();
		}
		return serverSteps;
	}

	/**
	 * @param serverSteps the serverSteps to set
	 */
	public void setServerSteps(ArrayList<MobileServerConversionStep> serverSteps) {
		this.serverSteps = serverSteps;
	}

	/**
	 * @return the clientSteps
	 */
	public ArrayList<MobileClientConversionStep> getClientSteps() {
		if (clientSteps == null) {
			clientSteps = new ArrayList<MobileClientConversionStep>();
		}
		return clientSteps;
	}

	/**
	 * @param clientSteps the clientSteps to set
	 */
	public void setClientSteps(ArrayList<MobileClientConversionStep> clientSteps) {
		this.clientSteps = clientSteps;
	}
}
