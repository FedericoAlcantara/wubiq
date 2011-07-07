/**
 * 
 */
package net.sf.wubiq.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.PrintServiceLookup;
import javax.servlet.http.HttpServletRequest;

import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;

/**
 * Manages remote print clients.
 * @author Federico Alcantara
 *
 */
public class RemoteClientManager {
	private Map<String, RemoteClient> remotes;
	private static RemotePrintServiceLookup remoteLookup;
	
	public static RemoteClientManager getRemoteClientManager(HttpServletRequest request) {
		RemoteClientManager remoteClientManager 
			= (RemoteClientManager)request.getSession(true).getServletContext().getAttribute(WebKeys.REMOTE_CLIENT_MANAGER);
		if (remoteClientManager == null) {
			remoteClientManager = new RemoteClientManager();
			request.getSession().getServletContext().setAttribute(WebKeys.REMOTE_CLIENT_MANAGER, remoteClientManager);
		}
		return remoteClientManager;
	}

	/**
	 * Gets the remote client and updates its last accessed time.
	 * @param uuid Unique identifier to look for
	 * @return
	 */
	public RemoteClient getRemoteClient(String uuid) {
		validateRemoteLookup();
		RemoteClient returnValue = getRemotes().get(uuid);
		if (returnValue != null && !returnValue.isKilled()) {
			returnValue.setLastAccessedTime(new Date().getTime());
		}
		return returnValue;
	}

	/**
	 * Adds a remote client to the manager.
	 * @param uuid Unique identifier.
	 * @param client Client information and status.
	 */
	public void addRemote(String uuid, RemoteClient client) {
		getRemotes().put(uuid, client);
	}
	
	/**
	 * Finds the remote print service lookup. If it doesn't exists then one is created
	 * and registered to the print service lookup.
	 * @return The found or newly created remote print service lookup. 
	 */
	public RemotePrintServiceLookup validateRemoteLookup() {
		if (remoteLookup == null) {
			remoteLookup = new RemotePrintServiceLookup();
			PrintServiceLookup.registerServiceProvider(remoteLookup);
		}
		return remoteLookup;
	}

	/**
	 * Updates the remotes, removing inactive ones.
	 */
	public void updateRemotes() {
		validateRemoteLookup();
		Collection<String> uuidToRemoves = new ArrayList<String>();
		for (Entry<String, RemoteClient> infoEntry : getRemotes().entrySet()) {
			if (!infoEntry.getValue().isRemoteDead()) {
				uuidToRemoves.add(infoEntry.getKey());
			}
		}
		for (String uuidToRemove : uuidToRemoves) {
			RemotePrintServiceLookup.removePrintServices(uuidToRemove);
			getRemotes().remove(uuidToRemove);
		}
	}

	/**
	 * @return Returns the list of active uuids.
	 */
	public Collection<String> getUuids() {
		Collection<String> returnValue = new ArrayList<String>();
		for (Entry<String, RemoteClient> entry : getRemotes().entrySet()) {
			if (entry.getValue().isRemoteActive()) {
				returnValue.add(entry.getKey());
			}
		}
		return returnValue;
	}
	
	/**
	 * @return The remote map.
	 */
	private Map<String, RemoteClient> getRemotes() {
		if (remotes == null) {
			remotes = new HashMap<String, RemoteClient>();
		}
		return remotes;
	}
	
	
}
