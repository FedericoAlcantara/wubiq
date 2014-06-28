/**
 * 
 */
package net.sf.wubiq.servlets;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import net.sf.wubiq.utils.ServerLabels;
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
	public void contextDestroyed(ServletContextEvent event) {
		hsqlServer.shutdown();
	}

	/**
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServerProperties.setRealPath(event.getServletContext().getRealPath(""));
		Level level = getLevel("org");
		Logger.getLogger(this.getClass().getName()).info(ServerLabels.get("server.version", Labels.VERSION));
		Logger.getLogger(this.getClass().getName()).info("http://sourceforge.net/projects/wubiq\n");
		hsqlServer = new Server();
		hsqlServer.setDatabasePath(0, event.getServletContext().getRealPath("/WEB-INF/" + ServerProperties.getHsqldbDatabaseName()));
		hsqlServer.setDatabaseName(0, ServerProperties.getHsqldbDbName());
		if (!Is.emptyString(ServerProperties.getHsqldbPort())) {
			hsqlServer.setPort(Integer.parseInt(ServerProperties.getHsqldbPort()));
		}
		hsqlServer.setSilent(true);
		hsqlServer.setTrace(false);
		hsqlServer.start();
		Logger.getLogger("org").setLevel(level);
		Logger.getLogger("net").setLevel(level);
		ServletsStatus.setReady();
	}

	/** 
	 * Determines the Log level for the given log name.
	 * It searches up into the parents to determine the level in the case of no logger level
	 * found.
	 * @param logName Name of the logger.
	 * @return Level found or default log level : info. Never null.
	 */
	private Level getLevel (String logName) {
		Level returnValue = getLevel(Logger.getLogger(logName));
		if (returnValue == null) {
			returnValue = Level.INFO;
		}
		return returnValue;
	}
	
	/**
	 * Recursive implementation of getLevel.
	 * @param logger Logger to be assessed.
	 * @return Log level or null.
	 */
	private Level getLevel (Logger logger) {
		Level returnValue = null;
		if (logger != null) {
			returnValue = logger.getLevel();
			if (returnValue == null) {
				returnValue = getLevel(logger.getParent());
			}
		}
		return returnValue;
	}
}
