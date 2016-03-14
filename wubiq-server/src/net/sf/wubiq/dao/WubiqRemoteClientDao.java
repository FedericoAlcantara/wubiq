/**
 * 
 */
package net.sf.wubiq.dao;

import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.persistence.PersistenceManager;

/**
 * Helper for remote client persistence.
 * @author Federico Alcantara
 *
 */
public enum WubiqRemoteClientDao {
	INSTANCE;
	
	/**
	 * Finds a instance of a remote client. Returns a ATTACHED instance.
	 * @param uniqueId UUID of the remote client.
	 * @return RemoteClient.
	 */
	private RemoteClient find(String uniqueId) {
		RemoteClient returnValue = null;
		try {
			returnValue = PersistenceManager.em().find(RemoteClient.class, uniqueId);
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Updates or persist the given client.
	 * @param client Client to persist.
	 */
	public void updateRemote(RemoteClient client) {
		try {
			RemoteClient current = find(client.getUniqueId());
			if (current == null) {
				PersistenceManager.em().persist(client);
			} else {
				current.setUniqueId(client.getUniqueId());
				current.setComputerName(client.getComputerName());
				current.setClientVersion(client.getClientVersion());
				current.setInactiveTime(client.getInactiveTime());
				current.setKilled(client.isKilled());
				current.setPaused(client.isPaused());
				current.setRefreshed(client.isRefreshed());
				PersistenceManager.em().merge(current);
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Reads a remote client. Returns a DETACHED instance.
	 * @param uniqueId Unique id of the remote.
	 * @return remote client found or null if not found.
	 */
	public RemoteClient readRemote(String uniqueId) {
		RemoteClient returnValue = null;
		try {
			returnValue = find(uniqueId);
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Removes the given remote from the persistence.
	 * @param uniqueId Unique id.
	 */
	public void removeRemote(String uniqueId) {
		RemoteClient remote = null;
		try {
			remote = find(uniqueId);
			if (remote != null) {
				PersistenceManager.em().remove(remote);
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Removes all clients 
	 */
	public void removeAllKilledRemotes() {
		try {
			PersistenceManager.em().createQuery("DELETE from RemoteClient"
					+ " WHERE "
					+ "killed = true").executeUpdate();
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets a list of the remote clients.
	 * @return List of clients.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> remoteClients() {
		Set<String> returnValue = new TreeSet<String>();
		try {
			returnValue.addAll(PersistenceManager.em().createQuery("SELECT uniqueId FROM RemoteClient"
					+ " WHERE "
					+ "killed is null"
					+ " OR "
					+ "killed = false"
					+ " ORDER BY uniqueId").getResultList());
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
}
