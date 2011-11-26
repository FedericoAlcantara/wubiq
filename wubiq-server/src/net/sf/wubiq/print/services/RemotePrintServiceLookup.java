/**
 * 
 */
package net.sf.wubiq.print.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.DocFlavor;
import javax.print.MultiDocPrintService;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.AttributeSet;

/**
 * Implements a RemotePrintServiceLookup aware of the remote print services that are online.
 * @author Federico Alcantara
 *
 */
public class RemotePrintServiceLookup extends PrintServiceLookup {
	private static Map<String, Map<String, PrintService>> allRemotePrintServices;
	private static PrintService remoteDefaultPrintService; 
	private static Map<String, Boolean> mobilePrintServices;
	
	/**
	 * @see javax.print.PrintServiceLookup#getDefaultPrintService()
	 */
	@Override
	public PrintService getDefaultPrintService() {
		return getRemoteDefaultPrintService();
	}

	/**
	 * @see javax.print.PrintServiceLookup#getMultiDocPrintServices(javax.print.DocFlavor[], javax.print.attribute.AttributeSet)
	 */
	@Override
	public MultiDocPrintService[] getMultiDocPrintServices(DocFlavor[] flavors,
			AttributeSet attributes) {
		return null;
	}

	/**
	 * @see javax.print.PrintServiceLookup#getPrintServices()
	 */
	@Override
	public PrintService[] getPrintServices() {
		return getRemotePrintServices().toArray(new PrintService[getRemotePrintServices().size()]);
	}

	/**
	 * @see javax.print.PrintServiceLookup#getPrintServices(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public PrintService[] getPrintServices(DocFlavor flavor,
			AttributeSet attributes) {
		return getRemotePrintServices().toArray(new PrintService[getRemotePrintServices().size()]);
	}

	/**
	 * Overrides default behavior and register services in custom form.
	 * @param printService Service to be registered.
	 * @return Always true.
	 */
	public static boolean registerService(PrintService printService) {
		String uuid = ((RemotePrintService)printService).getUuid();
		Map<String, PrintService> uuidServices = getRemotePrintServices(uuid);
		uuidServices.put(printService.getName(), printService);
		if (((RemotePrintService)printService).isMobile()) {
			getMobilePrintServices().put(uuid, true);
		}
		return true;
	}
	
	/**
	 * Gather all active remote print services.
	 * @return A List of remote print services. Never null.
	 */
	public static List<PrintService> getRemotePrintServices() {
		List<PrintService> returnValue = new ArrayList<PrintService>();
		for (Entry<String, Map<String, PrintService>> remotePrintServices : getAllRemotePrintServices().entrySet()) {
			returnValue.addAll(remotePrintServices.getValue().values());
		}
		return returnValue;
	}

	/**
	 * Gather all active remote print services.
	 * @return A List of remote print services. Never null.
	 */
	public static Map<String, PrintService> getRemotePrintServices(String uuid) {
		Map<String, PrintService> returnValue  = getAllRemotePrintServices().get(uuid);
		if (returnValue == null) {
			returnValue = new HashMap<String, PrintService>();
			getAllRemotePrintServices().put(uuid, returnValue);
		}
		return returnValue;
	}
	/**
	 * Removes all print services for a given id.
	 * @param uuid Unique id of the local print manager.
	 */
	public static void removePrintServices(String uuid) {
		getAllRemotePrintServices().remove(uuid);
	}		

	/**
	 * @return All remote print services.
	 */
	private static Map<String, Map<String, PrintService>> getAllRemotePrintServices() {
		if (allRemotePrintServices == null) {
			allRemotePrintServices = new HashMap<String, Map<String, PrintService>>();
		}
		return allRemotePrintServices;
	}

	/**
	 * @return Default remote print service.
	 */
	private static PrintService getRemoteDefaultPrintService() {
		return remoteDefaultPrintService;
	}

	/**
	 * @param remoteDefaultPrintService the remoteDefaultPrintService to set
	 */
	public static void setRemoteDefaultPrintService(PrintService remoteDefaultPrintService) {
		RemotePrintServiceLookup.remoteDefaultPrintService = remoteDefaultPrintService;
	}

	/**
	 * @param uuid Unique uuid.
	 * @return True if the pending tasks should be handle as mobile processes.
	 */
	public static boolean isMobile(String uuid) {
		Boolean returnValue = getMobilePrintServices().get(uuid);
		return returnValue != null && returnValue;
	}
	
	private static Map<String, Boolean> getMobilePrintServices() {
		if (mobilePrintServices == null) {
			mobilePrintServices = new HashMap<String, Boolean>();
		}
		return mobilePrintServices;
	}
	
}
