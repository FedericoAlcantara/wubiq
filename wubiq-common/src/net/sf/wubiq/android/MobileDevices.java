/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implemented device list
 * @author Federico Alcantara
 *
 */
public enum MobileDevices {
	INSTANCE;
	private final int DEFAULT_DPI = 192;
	
	/**
	 * Order is important.
	 */
	private Map<String, MobileDeviceInfo> devices;
	
	/**
	 * 
	 * @return List of devices info
	 */
	public Map<String, MobileDeviceInfo> getDevices() {
		if (devices == null) {
			devices = new LinkedHashMap<String, MobileDeviceInfo>();
			registerGenerics();
			registerStarMicronics();
		}
		return devices;
	}

	/**
	 * 
	 * @return Device name list
	 */
	public List<String> getDeviceNames() {
		return new ArrayList<String>(getDevices().keySet());
	}
		
	private void registerGenerics() {
		devices.put("Generic - 2 in.", genericBw("2"));
		devices.put("Generic - 3 in.", genericBw("3"));
		devices.put("Generic - 4 in.", genericBw("4"));
	}
	
	private void registerStarMicronics() {
		devices.put("Star Micronics - 2 in.", starMicronics("2"));
		devices.put("Star Micronics - 3 in.", starMicronics("3"));
		devices.put("Star Micronics - 4 in.", starMicronics("4"));
	}
	
	private MobileDeviceInfo genericBw(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileConversionStep> serverSteps = new ArrayList<MobileConversionStep>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Generic -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * DEFAULT_DPI);
		device.setColorCapable(false);
		serverSteps.add(MobileConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileConversionStep.RESIZE);
		serverSteps.add(MobileConversionStep.IMAGE_TO_ESCAPED);
		compatibleDevices.add("Generic -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

	private MobileDeviceInfo starMicronics(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileConversionStep> serverSteps = new ArrayList<MobileConversionStep>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Star Micronics -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * DEFAULT_DPI);
		device.setColorCapable(false);
		serverSteps.add(MobileConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileConversionStep.RESIZE);
		serverSteps.add(MobileConversionStep.IMAGE_TO_ESCAPED);
		compatibleDevices.add("SM-S" + width + "00");
		if (width.equals("3")) {
			compatibleDevices.add("SM-T"+ width + "00");
		}
		device.setServerSteps(serverSteps);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

}
