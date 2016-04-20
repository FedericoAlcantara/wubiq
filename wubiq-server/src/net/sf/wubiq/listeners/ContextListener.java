/**
 * 
 */
package net.sf.wubiq.listeners;

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.servlets.ServletsStatus;
import net.sf.wubiq.utils.ServerProperties;
import net.sf.wubiq.utils.ServerWebUtils;

/**
 * @author Federico Alcantara
 *
 */
public class ContextListener implements ServletContextListener {
	private static boolean persistenceActive = false;
	private static List<String> serverIps = null;
	private static String computerName = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent context) {
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		ServerProperties.INSTANCE.setRealPath(context.getServletContext().getRealPath(""));
		persistenceActive = PersistenceManager.isPersistenceEnabled();
		if (persistenceActive) {
			PersistenceManager.createSchemas();
			serverIps = ServerWebUtils.INSTANCE.serverIps();
			computerName = ServerWebUtils.INSTANCE.computerName();
		}
		ServletsStatus.setReady();
	}
	
	/**
	 * List of server's own ip addresses.
	 * @return List of ip addresses.
	 */
	public static List<String> serverIps() {
		return serverIps;
	}
	
	/**
	 * Returns the computer name where this process is running.
	 * @return Computer name or null if not found.
	 */
	public static String computerName() {
		return computerName;
	}
}
