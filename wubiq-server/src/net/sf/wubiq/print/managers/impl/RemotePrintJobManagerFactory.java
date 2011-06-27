/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import net.sf.wubiq.print.managers.IRemotePrintJobManager;

/**
 * Returns a instance of a print job manager.
 * @author Federico Alcantara
 *
 */
public final class RemotePrintJobManagerFactory {
	private RemotePrintJobManagerFactory(){
	}
	
	public static IRemotePrintJobManager getRemotePrintJobManager() {
		return QueuePrintJobManager.getInstance();
	}
}
