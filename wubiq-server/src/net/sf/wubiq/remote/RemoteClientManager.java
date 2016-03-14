/**
 * 
 */
package net.sf.wubiq.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.print.PrintServiceLookup;
import javax.servlet.http.HttpServletRequest;

import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.dao.WubiqRemoteClientDao;
import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;

/**
 * Manages remote print clients. Each instance of RemoteClient represents a connected client.
 * @author Federico Alcantara
 *
 */
public class RemoteClientManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private transient Map<String, RemoteClient> remotes;
	private transient Map<String, Date> remotesAccessedTimes;
	
	private static RemotePrintServiceLookup remoteLookup;
	private static transient Boolean persistenceActive = null;
	
	public RemoteClientManager() {
		if (persistenceActive == null) {
			persistenceActive = PersistenceManager.isPersistenceEnabled();
		}
	}
	
	/**
	 * Finds or create the appropriate Remote client manager.
	 * @param request
	 * @return
	 */
	public static RemoteClientManager getRemoteClientManager(HttpServletRequest request) {
		RemoteClientManager remoteClientManager = null;
		remoteClientManager 
			= (RemoteClientManager)request.getSession(true).getServletContext().getAttribute(WebKeys.REMOTE_CLIENT_MANAGER);
		if (remoteClientManager == null) {
			remoteClientManager = new RemoteClientManager();
			request.getSession().getServletContext().setAttribute(WebKeys.REMOTE_CLIENT_MANAGER, remoteClientManager);
		}
		return remoteClientManager;
	}

	/**
	 * Gets the remote client and updates its last accessed time.
	 * @param uuid Unique identifier to look for.
	 * @return The client or null.
	 */
	public RemoteClient getRemoteClient(String uuid) {
		return getRemoteClient(uuid, false);
	}
	
	/**
	 * Gets the remote client and updates its last accessed time.
	 * @param uuid Unique identifier to look for.
	 * @param doNotUpdateAccessedTime If true the accessed time is not updated.
	 * @return The client or null.
	 */
	public RemoteClient getRemoteClient(String uuid, boolean doNotUpdateAccessedTime) {
		RemoteClient returnValue = null;
		validateRemoteLookup();
		if (persistenceActive) {
			returnValue = WubiqRemoteClientDao.INSTANCE.readRemote(uuid);
		} else {
			returnValue = getRemotes().get(uuid);
		}
		if (!doNotUpdateAccessedTime) {
			if (returnValue != null && !returnValue.isKilled()) {
				getRemotesAccessedTimes().put(uuid, new Date());
			}
		}
		return returnValue;
	}

	/**
	 * Adds a remote client to the manager.
	 * @param uuid Unique identifier.
	 * @param client Client information and status.
	 */
	public void addRemote(String uuid, RemoteClient client) {
		client.setUniqueId(uuid);
		if (persistenceActive) {
			WubiqRemoteClientDao.INSTANCE.updateRemote(client);
		} else {
			getRemotes().put(uuid, client);
		}
	}
	
	/**
	 * Kills a remote client. Removes the manager and the print services.
	 * @param uuid Unique id of the remote client.
	 */
	public void killRemote(String uuid) {
		RemoteClient client = getRemoteClient(uuid, true);
		if (client != null) {
			client.setKilled(true);
			addRemote(uuid, client);
		}
		RemotePrintServiceLookup.removePrintServices(uuid);
	}
	
	/**
	 * Pauses a remote.
	 * @param uuid Unique id of the remote client.
	 */
	public void pauseRemote(String uuid) {
		changeRemotePausedStatus(uuid, true);
	}

	/**
	 * Resume a remote
	 * @param uuid Unique id of the remote client.
	 */
	public void resumeRemote(String uuid) {
		changeRemotePausedStatus(uuid, false);
	}
	
	/**
	 * Changes the remote pause status.
	 * @param uuid Unique id of the remote client.
	 * @param paused Paused new state.
	 */
	private void changeRemotePausedStatus(String uuid, boolean paused) {
		RemoteClient client = getRemoteClient(uuid, true);
		if (client != null) {
			client.setPaused(paused);
			addRemote(uuid, client);
		}
	}
	
	/**
	 * Registers the print service.
	 * @param uuid Unique id of the remote service.
	 * @param printService Print service to add to the remote.
	 */
	public void registerPrintService(String uuid, RemotePrintService printService) {
		validateRemoteLookup();
		RemotePrintServiceLookup.registerRemoteService(printService);
	}
	
	/**
	 * Finds the remote print service lookup. If it doesn't exists then one is created
	 * and registered to the print service lookup.
	 * @return The found or newly created remote print service lookup. 
	 */
	private RemotePrintServiceLookup validateRemoteLookup() {
		if (remoteLookup == null) {
			remoteLookup = new RemotePrintServiceLookup(persistenceActive);
			PrintServiceLookup.registerServiceProvider(remoteLookup);
		}
		return remoteLookup;
	}
	
	/**
	 * @return Returns the list of active uuids.
	 */
	public Collection<String> getUuids() {
		Collection<String> returnValue = new ArrayList<String>();
		if (persistenceActive) {
			returnValue.addAll(WubiqRemoteClientDao.INSTANCE.remoteClients());
		} else {
			returnValue.addAll(getRemotes().keySet());
		}
		return returnValue;
	}
	
	/**
	 * Returns true if the remote is active.
	 * @param uuid Unique id of the remote to test.
	 * @return True if the remote is active or false otherwise.
	 */
	public boolean isRemoteActive(String uuid) {
		Date remoteTime = getRemotesAccessedTimes().get(uuid);
		if (remoteTime != null 
				&& (new Date().getTime() - remoteTime.getTime()) < 20000) {
			RemoteClient client = getRemoteClient(uuid, true);
			if (client != null && !client.isKilled()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns remote last accessed date.
	 * @param uuid Unique id of the remote to test.
	 * @return Last accessed date.
	 */
	public Date remoteLastAccessed(String uuid) {
		Date remoteTime = getRemotesAccessedTimes().get(uuid);
		if (remoteTime == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(1900, 00, 01);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 1);
			remoteTime = calendar.getTime();
			getRemotesAccessedTimes().put(uuid, remoteTime);
		}
		return remoteTime;
	}

	/**
	 * Remote clients.
	 * @return The remote map.
	 */
	private Map<String, RemoteClient> getRemotes() {
		if (remotes == null) {
			remotes = new HashMap<String, RemoteClient>();
		}
		return remotes;
	}
	
	/**
	 * @return The times for the remotes.
	 */
	private Map<String, Date> getRemotesAccessedTimes() {
		if (remotesAccessedTimes == null) {
			remotesAccessedTimes = new HashMap<String, Date>();
		}
		return remotesAccessedTimes;
	}
	
	public boolean isPersistenceActive() {
		return persistenceActive;
	}
}
