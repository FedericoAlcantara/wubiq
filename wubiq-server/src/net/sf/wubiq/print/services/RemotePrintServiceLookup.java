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
 * @author Federico Alcantara
 *
 */
public class RemotePrintServiceLookup extends PrintServiceLookup {
	private static Map<String, Map<String, PrintService>> allRemotePrintServices;
	private static PrintService remoteDefaultPrintService; 
	
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
		return getRemotePrintServices().toArray(new PrintService[0]);
	}

	/**
	 * @see javax.print.PrintServiceLookup#getPrintServices(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public PrintService[] getPrintServices(DocFlavor flavor,
			AttributeSet attributes) {
		if (flavor.equals(DocFlavor.INPUT_STREAM.PDF)) {
			return getRemotePrintServices().toArray(new PrintService[0]);
		}
		return null;
	}

	/**
	 * Overrides default behavior and register services in custom form.
	 * @param printService Service to be registered.
	 * @return Always true.
	 */
	public static boolean registerService(PrintService printService) {
		Map<String, PrintService> uuidServices = getRemotePrintServices(((RemotePrintService)printService).getUuid());
		uuidServices.put(printService.getName(), printService);
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
	 * @return the allRemotePrintServices
	 */
	private static Map<String, Map<String, PrintService>> getAllRemotePrintServices() {
		if (allRemotePrintServices == null) {
			allRemotePrintServices = new HashMap<String, Map<String, PrintService>>();
		}
		return allRemotePrintServices;
	}

	/**
	 * @return the remoteDefaultPrintService
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

	
}
