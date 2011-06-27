/**
 * 
 */
package org.wubiq.print.managers.impl;

import org.wubiq.print.managers.IRemotePrintJobManager;

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
