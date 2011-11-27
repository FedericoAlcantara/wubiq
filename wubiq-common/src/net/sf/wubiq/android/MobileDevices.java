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
			registerBlank();
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
		
	private void registerBlank() {
		MobileDeviceInfo device = new MobileDeviceInfo();
		device.setName("--");
		devices.put("--", device);
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
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Generic -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * DEFAULT_DPI);
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_ESCAPED);
		clientSteps.add(MobileClientConversionStep.OUTPUT_SM_BYTES);
		compatibleDevices.add("Generic -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

	private MobileDeviceInfo starMicronics(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Star Micronics -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * DEFAULT_DPI);
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_ESCAPED);
		clientSteps.add(MobileClientConversionStep.OUTPUT_SM_BYTES);
		compatibleDevices.add("SM-S" + width + "00");
		if (width.equals("3")) {
			compatibleDevices.add("SM-T"+ width + "00");
		}
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

}
