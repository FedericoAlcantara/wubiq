/**
 * 
 */
package net.sf.wubiq.listeners;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.servlets.ServletsStatus;
import net.sf.wubiq.utils.ServerProperties;
import net.sf.wubiq.utils.ServerWebUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class ContextListener implements ServletContextListener {
	private static List<String> serverIps = null;
	private static String computerName = null;
	private static final Log LOG = LogFactory.getLog(ContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent context) {
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		ServerProperties.INSTANCE.setRealPath(context.getServletContext().getRealPath(""));
		boolean persistenceActive = PersistenceManager.isPersistenceEnabled();
		if (persistenceActive) {
			PersistenceManager.createSchemas();
			serverIps = ServerWebUtils.INSTANCE.serverIps();
			computerName = ServerWebUtils.INSTANCE.computerName();
		}
		// Notify common elements.
		ConfigurationKeys.setPersistenceActive(persistenceActive);
		registerPrintJobManager();
		ServletsStatus.setReady();
	}
	
	/**
	 * List of server's own ip addresses.
	 * @return List of ip addresses.
	 */
	public static List<String> serverIps() {
		if (serverIps == null) {
			serverIps = ServerWebUtils.INSTANCE.serverIps();
		}
		return serverIps;
	}
	
	/**
	 * Returns the computer name where this process is running.
	 * @return Computer name or null if not found.
	 */
	public static String computerName() {
		return computerName;
	}
	
	/**
	 * Creates a Singleton print job manager.
	 */
	@SuppressWarnings("rawtypes")
	private static void registerPrintJobManager() {
		IDirectConnectPrintJobManager directInstance;
		String manager = ServerProperties.INSTANCE.getRemotePrintJobManager();
		try {
			Class managerClass = Class.forName(manager);
			directInstance = (IDirectConnectPrintJobManager)managerClass.newInstance();
			directInstance.initialize();
			RemotePrintJobManagerFactory.registerRemotePrintJobManager(directInstance);
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

}
