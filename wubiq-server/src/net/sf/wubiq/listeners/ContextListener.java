/**
 * 
 */
package net.sf.wubiq.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.servlets.ServletsStatus;
import net.sf.wubiq.utils.ServerProperties;

/**
 * @author Federico Alcantara
 *
 */
public class ContextListener implements ServletContextListener {
	private boolean persistenceActive = false;
	
	@Override
	public void contextDestroyed(ServletContextEvent context) {
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		PersistenceManager.setDialect(context.getServletContext().getInitParameter("dialect"));
		ServerProperties.INSTANCE.setRealPath(context.getServletContext().getRealPath(""));
		persistenceActive = PersistenceManager.isPersistenceEnabled();
		if (persistenceActive) {
			PersistenceManager.createSchemas();
		}
		ServletsStatus.setReady();
	}
}
