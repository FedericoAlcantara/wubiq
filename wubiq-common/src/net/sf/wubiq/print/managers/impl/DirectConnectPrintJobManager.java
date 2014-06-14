/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Manages direct connection with clients.
 * @author Federico Alcantara
 *
 */
public class DirectConnectPrintJobManager implements IDirectConnectPrintJobManager {
	private Map<String, DirectConnectorQueue> connectors;
	private Map<Long, String> associatedQueue;
	private long lastJobId;
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		connectors = new ConcurrentHashMap<String, DirectConnectorQueue>();
		associatedQueue = new ConcurrentHashMap<Long, String>();
		lastJobId = 0l;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#addRemotePrintJob(java.lang.String, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public synchronized long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		IDirectConnectorQueue queue = directConnector(queueId);
		lastJobId++;
		queue.addPrintJob(lastJobId, remotePrintJob);
		associatedQueue.put(lastJobId, queueId);
		return lastJobId;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#removeRemotePrintJob(long)
	 */
	@Override
	public boolean removeRemotePrintJob(long jobId) {
		DirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			queue.removePrintJob(jobId);
		}
		return true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getRemotePrintJob(long, boolean)
	 */
	@Override
	public IRemotePrintJob getRemotePrintJob(long jobId, boolean fullPrintJob) {
		DirectConnectorQueue queue = associatedQueue(jobId);
		return queue.remotePrintJob(jobId);

	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintJobs(java.lang.String, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public Collection<Long> getPrintJobs(String queueId,
			RemotePrintJobStatus status) {
		IDirectConnectorQueue queue = directConnector(queueId);
		return queue.printJobs();
	}

	/**
	 * Returns the direct connector queue manager.
	 * @param jobId Id of the associated job.
	 * @return
	 */
	private synchronized DirectConnectorQueue associatedQueue(long jobId) {
		String queueId = associatedQueue.get(jobId);
		DirectConnectorQueue queue = null;
		if (queueId != null) {
			queue = connectors.get(queueId);
		}
		return queue;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectPrintJobManager#directConnector(java.lang.String)
	 */
	@Override
	public IDirectConnectorQueue directConnector(String queueId) {
		DirectConnectorQueue queue = connectors.get(queueId);
		if (queue == null) {
			queue = new DirectConnectorQueue(queueId);
			connectors.put(queueId, queue);
		}
		return queue;
	}
}
