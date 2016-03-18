/**
 * 
 */
package net.sf.wubiq.dao;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.wubiq.data.WubiqServer;
import net.sf.wubiq.listeners.ContextListener;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.utils.ServerWebUtils;

/**
 * @author Federico Alcantara
 *
 */
public enum WubiqServerDao {
	INSTANCE;
	
	
	/**
	 * Finds the first reachable server which has the given job id. Returns an ATTACHED server.
	 * @param jobId Id of the job.
	 * @return Found server or null.
	 */
	@SuppressWarnings("unchecked")
	private WubiqServer find(Long jobId) {
		WubiqServer returnValue = null;
		Collection<WubiqServer> servers = new ArrayList<WubiqServer>();
		servers = PersistenceManager.em().createQuery("SELECT s FROM WubiqServer s"
				+ " WHERE "
				+ "s.jobId = :jobId")
				.setParameter("jobId", jobId)
				.getResultList();
		for (WubiqServer server : servers) {
			if (ServerWebUtils.INSTANCE.canConnect(server.getIp())) {
				returnValue = server;
				break;
			}
		}
		return returnValue;
	}
	
	/**
	 * Finds a server which has the given job id. Returns an ATTACHED server.
	 * @param jobId Id of the job.
	 * @param ip Ip of the server.
	 * @return Found server or null.
	 */
	@SuppressWarnings("unchecked")
	private WubiqServer find(Long jobId, String ip) {
		WubiqServer returnValue = null;
		Collection<WubiqServer> servers = new ArrayList<WubiqServer>();
		servers = PersistenceManager.em().createQuery("SELECT s FROM WubiqServer s"
				+ " WHERE "
				+ "s.jobId = :jobId"
				+ " AND "
				+ "s.ip = :ip")
				.setParameter("jobId", jobId)
				.setParameter("ip", ip)
				.getResultList();
		for (WubiqServer server : servers) {
			returnValue = server;
				break;
		}
		return returnValue;
	}

	
	/**
	 * Tries to find a reachable associated server.
	 * @param jobId Job id.
	 * @return The associated server Ip or null if none found.
	 */
	public String associatedServer(Long jobId) {
		try {
			String returnValue = null;
			WubiqServer server = find(jobId);
			if (server != null) {
				returnValue = server.getIp();
			}
			PersistenceManager.commit();
			return returnValue;
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Register job into persistence.
	 * @param jobId Job id to register.
	 */
	public void addPrintJob(Long jobId) {
		try {
			for (String ip : ContextListener.serverIps()) {
				WubiqServer server = find(jobId, ip);
				if (server == null) {
					server = new WubiqServer();
					server.setJobId(jobId);
					server.setIp(ip);
					server.setName(ContextListener.computerName());
					PersistenceManager.em().persist(server);
				}
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deletes the job / ip relationship.
	 * @param jobId Id of the job.
	 */
	public void removePrintJob(Long jobId) {
		try {
			PersistenceManager.em().createQuery("DELETE FROM WubiqServer"
					+ " WHERE "
					+ "jobId = :jobId")
					.setParameter("jobId", jobId)
					.executeUpdate();
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
	}
}
