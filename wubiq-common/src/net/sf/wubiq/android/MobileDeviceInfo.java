/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.Collection;
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
	private ArrayList<MobileConversionStep> serverSteps ;

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
	public ArrayList<MobileConversionStep> getServerSteps() {
		return serverSteps;
	}

	/**
	 * @param serverSteps the serverSteps to set
	 */
	public void setServerSteps(ArrayList<MobileConversionStep> serverSteps) {
		this.serverSteps = serverSteps;
	}
}
