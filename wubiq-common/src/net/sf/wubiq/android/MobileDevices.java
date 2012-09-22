/**
 * 
 */
package net.sf.wubiq.android;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
			registerDataMax();
			registerGenerics();
			registerPortiS();
			registerStarMicronics();
			registerZebra(); 
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

	private void registerDataMax() {
		devices.put("DataMax Apex 2 in", dataMax("2"));
		devices.put("DataMax Apex 3 in", dataMax("3"));
		devices.put("DataMax Apex 4 in", dataMax("4"));
		devices.put("DataMax Andes 3 in", dataMax("3", "Andes"));
	}
	
	private void registerGenerics() {
		devices.put("Generic 2 in", genericBw("2"));
		devices.put("Generic 3 in", genericBw("3"));
		devices.put("Generic 4 in", genericBw("4"));
	}
	
	private void registerPortiS() {
		devices.put("Porti-S 2 in", portiS("2"));
		devices.put("Porti-S 3 in", portiS("3"));
		devices.put("Porti-S 4 in", portiS("4"));
	}
	
	private void registerStarMicronics() {
		devices.put("Star Micronics 2 in", starMicronics("2"));
		devices.put("Star Micronics 3 in", starMicronics("3"));
		devices.put("Star Micronics 4 in", starMicronics("4"));
	}

	private void registerZebra() {
		devices.put("Zebra MZ220 2 in", zebra("2", "Zebra MZ220"));
		devices.put("Zebra MZ320 3 in", zebra("3", "Zebra MZ320"));
	}
	
	private MobileDeviceInfo genericBw(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Map<MobileConversionHint, Object> hints = new HashMap<MobileConversionHint, Object>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Generic -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * device.getResolutionDpi());
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_ESCAPED);
		clientSteps.add(MobileClientConversionStep.OUTPUT_BYTES);
		hints.put(MobileConversionHint.PRINT_DEFINED_BITMAP, new byte[]{0x1b, 0x58, 0x32, 0x18});
		compatibleDevices.add("Generic -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setHints(hints);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

	private MobileDeviceInfo starMicronics(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Star Micronics -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * device.getResolutionDpi());
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

	private MobileDeviceInfo portiS(String width) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Map<MobileConversionHint, Object> hints = new HashMap<MobileConversionHint, Object>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName("Porti-S -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * device.getResolutionDpi());
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_ESCAPED);
		clientSteps.add(MobileClientConversionStep.OUTPUT_BYTES);
		compatibleDevices.add("Porti-S -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setHints(hints);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}
	
	/**
	 * Device definition for apex line of mobile printers.
	 * @param width Width of the device.
	 * @return A instance of Mobile device info.
	 */
	private MobileDeviceInfo dataMax(String width) {
		return dataMax(width, "Apex");
	}
	
	/**
	 * Device definition for apex line of mobile printers.
	 * @param width Width of the device.
	 * @return A instance of Mobile device info.
	 */
	private MobileDeviceInfo dataMax(String width, String name) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Map<MobileConversionHint, Object> hints = new HashMap<MobileConversionHint, Object>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName(name + " -" + width + " in.");
		device.setMaxHorPixels(Integer.parseInt(width) * device.getResolutionDpi());
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_BIT_LINE);
		clientSteps.add(MobileClientConversionStep.OUTPUT_BYTES);
		hints.put(MobileConversionHint.INITIALIZE_PRINTER, new byte[]{});
		hints.put(MobileConversionHint.PRINT_DEFINED_BITMAP, new byte[]{0x1b, 0x56, 0x01, 0x00});
		compatibleDevices.add(name + " -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setHints(hints);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}

	/**
	 * Device definition for zebra line of mobile printers.
	 * @param width Width of the device.
	 * @return A instance of Mobile device info.
	 */
	private MobileDeviceInfo zebra(String width, String name) {
		MobileDeviceInfo device = new MobileDeviceInfo();
		ArrayList<MobileServerConversionStep> serverSteps = new ArrayList<MobileServerConversionStep>();
		ArrayList<MobileClientConversionStep> clientSteps = new ArrayList<MobileClientConversionStep>();
		Map<MobileConversionHint, Object> hints = new HashMap<MobileConversionHint, Object>();
		Collection<String> compatibleDevices = new ArrayList<String>();
		device.setName(name + " -" + width + " in.");
		//device.setResolutionDpi(200);
		device.setMaxHorPixels(Integer.parseInt(width) * device.getResolutionDpi());
		device.setColorCapable(false);
		serverSteps.add(MobileServerConversionStep.PDF_TO_IMAGE);
		serverSteps.add(MobileServerConversionStep.RESIZE);
		//serverSteps.add(MobileServerConversionStep.IMAGE_TO_BITMAP);
		//clientSteps.add(MobileClientConversionStep.OUTPUT_ZEBRA_IMAGE);

		
		serverSteps.add(MobileServerConversionStep.IMAGE_TO_PCX);
		clientSteps.add(MobileClientConversionStep.OUTPUT_ZEBRA_BYTES);
		hints.put(MobileConversionHint.INITIALIZE_PRINTER, "! U1 setvar \"device.languages\" \"CPCL\"\r\n! 0 200 200 {height} 1\r\nPCX 0 0\r\n");
		hints.put(MobileConversionHint.FINALIZE_PRINTER, "\r\nPRINT\r\n");
		hints.put(MobileConversionHint.MAX_IMAGE_HEIGHT, 500);
		
		
		compatibleDevices.add(name + " -" + width + " in.");
		device.setServerSteps(serverSteps);
		device.setClientSteps(clientSteps);
		device.setHints(hints);
		device.setCompatibleDevices(compatibleDevices);
		return device;
	}
}
