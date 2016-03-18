/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
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
	private static IDirectConnectPrintJobManager directInstance;
	private static long jobId = 0;
	
	private RemotePrintJobManagerFactory(){
	}
	
	/**
	 * Finds the associated manager for the given printer.
	 * @param uuid Unique printer id.
	 * @return Singleton object.
	 */
	public synchronized static IRemotePrintJobManager getRemotePrintJobManager(String uuid) {
		if (directInstance == null) {
			directInstance = (IDirectConnectPrintJobManager)getPrintJobManager(ServerProperties.INSTANCE.getRemotePrintJobManager());
		}
		return directInstance;
	}
	
	public synchronized static long nextJobId() {
		jobId++;
		return jobId;
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
}
