/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import net.sf.wubiq.print.managers.IRemotePrintJobManager;

/**
 * Returns a instance of a print job manager.
 * This will allow user selectable implementation of IRemotePrintJobManager.
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
