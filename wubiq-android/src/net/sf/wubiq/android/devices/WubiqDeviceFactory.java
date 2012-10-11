/**
 * 
 */
package net.sf.wubiq.android.devices;

import net.sf.wubiq.android.MobileClientConversionStep;

/**
 * @author Federico Alcantara
 *
 */
public enum WubiqDeviceFactory {
	INSTANCE;
	public BaseWubiqDevice getInstance(MobileClientConversionStep step) {
		BaseWubiqDevice returnValue = null;
		if (step.equals(MobileClientConversionStep.OUTPUT_SM_BYTES)) {
			returnValue = new DeviceStarMicronics();
		} else if (step.equals(MobileClientConversionStep.OUTPUT_BYTES)) {
			returnValue = new DeviceGeneric();
		} else if (step.equals(MobileClientConversionStep.OUTPUT_ZEBRA_BYTES)) {
			returnValue = new DeviceZebra();
		}
		return returnValue;
	}
}
