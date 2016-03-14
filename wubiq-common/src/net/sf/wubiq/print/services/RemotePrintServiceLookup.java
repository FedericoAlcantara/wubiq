/**
 * 
 */
package net.sf.wubiq.print.services;

import java.io.Serializable;
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

import net.sf.wubiq.dao.WubiqPrintServiceDao;

/**
 * Implements a RemotePrintServiceLookup aware of the remote print services that are online.
 * @author Federico Alcantara
 *
 */
public class RemotePrintServiceLookup extends PrintServiceLookup implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static Map<String, Map<String, RemotePrintService>> allRemotePrintServices;
	private static RemotePrintService remoteDefaultPrintService; 
	private static Map<String, Boolean> mobilePrintServices;
	
	private static Boolean persistenceActive;
	
	public RemotePrintServiceLookup(boolean persistenceActive) {
		RemotePrintServiceLookup.persistenceActive = persistenceActive;
	}
	
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
		List<RemotePrintService> remotePrintServices = getRemotePrintServices();
		PrintService[] returnValue = remotePrintServices.toArray(new PrintService[remotePrintServices.size()]);
		return returnValue;
	}

	/**
	 * @see javax.print.PrintServiceLookup#getPrintServices(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public PrintService[] getPrintServices(DocFlavor flavor,
			AttributeSet attributes) {
		List<RemotePrintService> remotePrintServices = getRemotePrintServices();
		PrintService[] returnValue = remotePrintServices.toArray(new PrintService[remotePrintServices.size()]);
		return returnValue;
	}

	/**
	 * Just to make it compliant (no errors) when pooled from PrintServiceUtils.
	 */
	public void refreshServices() {
		
	}

	/**
	 * Overrides default behavior and register services in custom form.
	 * Should not be called directly instead use RemoteClientManager registration method.
	 * @param printService Service to be registered.
	 * @return Always true.
	 */
	public static boolean registerService(RemotePrintService printService) {
		return registerRemoteService(printService);
	}

	/**
	 * Overrides default behavior and register services in custom form.
	 * @param printService Service to be registered.
	 * @return Always true.
	 */
	public static boolean registerRemoteService(RemotePrintService printService) {
		if (persistenceActive) {
			WubiqPrintServiceDao.INSTANCE.registerRemotePrintService(printService);
		} else {
			String uuid = printService.getUuid();
			Map<String, RemotePrintService> uuidServices = getRemotePrintServices(uuid);
			uuidServices.put(printService.getName(), printService);
			if (((RemotePrintService)printService).isMobile()) {
				getMobilePrintServices().put(uuid, true);
			}
		}
		return true;
	}

	/**
	 * Gather all active remote print services.
	 * @return A List of remote print services. Never null.
	 */
	private static List<RemotePrintService> getRemotePrintServices() {
		List<RemotePrintService> returnValue = new ArrayList<RemotePrintService>();
		if (persistenceActive) {
			returnValue.addAll(WubiqPrintServiceDao.INSTANCE.remoteAllPrintServices());
		} else {
			for (Entry<String, Map<String, RemotePrintService>> remotePrintServices : getAllRemotePrintServices().entrySet()) {
				returnValue.addAll(remotePrintServices.getValue().values());
			}
		}
		return returnValue;
	}
	
	/**
	 * Gather all active remote print services.
	 * @return A List of remote print services. Never null.
	 */
	private static Map<String, RemotePrintService> getRemotePrintServices(String uuid) {
		if (persistenceActive) {
			return WubiqPrintServiceDao.INSTANCE.remotePrintServices(uuid);
		}
		Map<String, RemotePrintService> returnValue  = getAllRemotePrintServices().get(uuid);
		if (returnValue == null) {
			returnValue = new HashMap<String, RemotePrintService>();
			getAllRemotePrintServices().put(uuid, returnValue);
		}
		return returnValue;
	}
	/**
	 * Removes all print services for a given id.
	 * @param uuid Unique id of the local print manager.
	 */
	public static void removePrintServices(String uuid) {
		if (persistenceActive) {
			WubiqPrintServiceDao.INSTANCE.removePrintServices(uuid);
		} else {
			getAllRemotePrintServices().remove(uuid);
		}
	}		
	
	/**
	 * @return All remote print services.
	 */
	private static Map<String, Map<String, RemotePrintService>> getAllRemotePrintServices() {
		if (allRemotePrintServices == null) {
			allRemotePrintServices = new HashMap<String, Map<String, RemotePrintService>>();
		}
		return allRemotePrintServices;
	}

	/**
	 * @return Default remote print service.
	 */
	private static RemotePrintService getRemoteDefaultPrintService() {
		return remoteDefaultPrintService;
	}

	/**
	 * @param remoteDefaultPrintService the remoteDefaultPrintService to set
	 */
	public static void setRemoteDefaultPrintService(RemotePrintService remoteDefaultPrintService) {
		RemotePrintServiceLookup.remoteDefaultPrintService = remoteDefaultPrintService;
	}

	/**
	 * @param uuid Unique uuid.
	 * @return True if the pending tasks should be handle as mobile processes.
	 */
	public static boolean isMobile(String uuid) {
		if (persistenceActive) {
			return WubiqPrintServiceDao.INSTANCE.isMobile(uuid);
		} else {
			Boolean returnValue = getMobilePrintServices().get(uuid);
			return returnValue != null && returnValue;
		}
	}
	
	/**
	 * Finds the print service associated with the remote print service name for the remote computer.
	 * @param uuid Unique remote computer identifier.
	 * @param remotePrintServiceName Name of the print service to look for.
	 * @return PrintService found or null.
	 */
	public static RemotePrintService find(String uuid, String remotePrintServiceName) {
		RemotePrintService returnValue = null;
		String formattedPrintServiceName = remotePrintServiceName.replace("\\", "\\\\");
		for (Entry<String, RemotePrintService> entry: getRemotePrintServices(uuid).entrySet()) {
			String printServiceName = entry.getKey();
			if (printServiceName.contains("@")) {
				printServiceName = printServiceName.substring(0, printServiceName.indexOf("@")).trim();
			}
			if (printServiceName.equals(formattedPrintServiceName)) {
				returnValue = entry.getValue();
				break;
			}
		}
		return returnValue;
	}
	
	/**
	 * List all mobile print services.
	 * @return Get mobile print services.
	 */
	private static Map<String, Boolean> getMobilePrintServices() {
		if (mobilePrintServices == null) {
			mobilePrintServices = new HashMap<String, Boolean>();
		}
		return mobilePrintServices;
	}
	
}
