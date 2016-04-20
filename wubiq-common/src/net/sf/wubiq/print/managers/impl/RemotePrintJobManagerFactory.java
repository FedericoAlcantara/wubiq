/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;

import org.apache.commons.lang3.exception.ExceptionUtils;
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
	private static boolean ready = false;

	private RemotePrintJobManagerFactory(){
	}
	
	/**
	 * Finds the associated manager for the given printer.
	 * Waits for the Remote print manager to be ready.
	 * @param uuid Unique printer id.
	 * @return Singleton object.
	 */
	public synchronized static IRemotePrintJobManager getRemotePrintJobManager(String uuid) {
		int count = 0;
		while (!ready) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error(ExceptionUtils.getMessage(e), e);
			}
			if (count++ > 3) {
				break;
			}
		}
		return directInstance;
	}
	
	public synchronized static long nextJobId() {
		jobId++;
		return jobId;
	}
	
	public static void registerRemotePrintJobManager(IDirectConnectPrintJobManager manager) {
		directInstance = manager;
		ready = true;
	}
}
