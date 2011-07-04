/**
 * 
 */
package net.sf.wubiq.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.ServerProperties;

import org.hsqldb.Server;


/**
 * Starts and stops Hsql server.
 * @author Federico Alcantara
 *
 */
public class HsqldbContextListener implements ServletContextListener {
	private Server hsqlServer;
	
	/**
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		hsqlServer.shutdown();
	}

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		hsqlServer = new Server();
		hsqlServer.setDatabasePath(0, event.getServletContext().getRealPath("/WEB-INF/" + ServerProperties.getHsqldbDatabaseName()));
		hsqlServer.setDatabaseName(0, ServerProperties.getHsqldbDbName());
		if (!Is.emptyString(ServerProperties.getHsqldbPort())) {
			hsqlServer.setPort(Integer.parseInt(ServerProperties.getHsqldbPort()));
		}
		hsqlServer.setSilent(true);
		hsqlServer.start();
	}

}
