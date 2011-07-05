/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

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
	private static IRemotePrintJobManager instance;
	
	private RemotePrintJobManagerFactory(){
	}
	
	/**
	 * @return Returns a Singleton print job manager.
	 */
	@SuppressWarnings("rawtypes")
	public static IRemotePrintJobManager getRemotePrintJobManager() {
		if (instance == null) {
			String manager = ServerProperties.getPrintJobManager();
			try {
				Class managerClass = Class.forName(manager);
				instance = (IRemotePrintJobManager)managerClass.newInstance();
				instance.initialize();
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
			} catch (InstantiationException e) {
				LOG.error(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				LOG.error(e.getMessage(), e);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return instance;
	}
}
