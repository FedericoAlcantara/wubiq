/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.RemotePrintJobManagerType;
import net.sf.wubiq.utils.ServerProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Returns a instance of a print job manager.
 * This will allow user selectable implementation of IRemotePrintJobManager.
 * @author Federico Alcantara
 *
 */
public final class RemotePrintJobManagerFactory {
	private static final Log LOG = LogFactory.getLog(RemotePrintJobManagerFactory.class);
	private static IRemotePrintJobManager instance;
	private static IDirectConnectPrintJobManager directInstance;
	private static Map<String, RemotePrintJobManagerType> managers;
	
	private RemotePrintJobManagerFactory(){
	}
	
	/**
	 * Finds the associated manager for the given printer.
	 * @param uuid Unique printer id.
	 * @return Singleton object.
	 */
	public synchronized static IRemotePrintJobManager getRemotePrintJobManager(String uuid) {
		return getRemotePrintJobManager(managers().get(uuid));
	}
	
	/**
	 * Creates and registers the remote print manager.
	 * @param uuid Associated unique printer Id.
	 * @param managerType Manager type.
	 * @return Instance of remote print job manager.
	 */
	public synchronized static IRemotePrintJobManager getRemotePrintJobManager(String uuid, RemotePrintJobManagerType managerType) {
		IRemotePrintJobManager returnValue = getRemotePrintJobManager(managerType);
		managers().put(uuid, managerType);
		return returnValue;
	}
	
	/**
	 * Returns the singleton object according to the manager type.
	 * @param managerType Manager type.
	 * @return Singleton object of manager type's
	 */
	private static IRemotePrintJobManager getRemotePrintJobManager(RemotePrintJobManagerType managerType) {
		IRemotePrintJobManager returnValue = null;
		if (managerType != null) {
			switch (managerType) {
				case DIRECT_CONNECT:
					returnValue = getDirectConnectPrintJobManager();
					break;
				case SERIALIZED:
					returnValue = getRemotePrintJobManager();
					break;
				default:
					break;
			}
		}
		return returnValue;
	}
	
	/**
	 * Gets a singleton of a direct connect print job manager
	 * @return
	 */
	private static IDirectConnectPrintJobManager getDirectConnectPrintJobManager() {
		if (directInstance == null) {
			directInstance = (IDirectConnectPrintJobManager)getPrintJobManager(ServerProperties.INSTANCE.getRemotePrintJobManager());
		}
		return directInstance;
	}
	
	/**
	 * @return Returns a Singleton print job manager.
	 */
	private static IRemotePrintJobManager getRemotePrintJobManager() {
		if (instance == null) {
			instance = getPrintJobManager(ServerProperties.INSTANCE.getPrintJobManager());
		}
		return instance;
	}

	/**
	 * @return Returns a Singleton print job manager.
	 */
	@SuppressWarnings("rawtypes")
	private static IRemotePrintJobManager getPrintJobManager(String manager) {
		IRemotePrintJobManager newInstance = null;
		try {
			Class managerClass = Class.forName(manager);
			newInstance = (IRemotePrintJobManager)managerClass.newInstance();
			newInstance.initialize();
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (InstantiationException e) {
			LOG.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage(), e);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return newInstance;
	}

	private static Map<String, RemotePrintJobManagerType> managers() {
		if (managers == null) {
			managers = new HashMap<String, RemotePrintJobManagerType>();
		}
		return managers;
	}
}
