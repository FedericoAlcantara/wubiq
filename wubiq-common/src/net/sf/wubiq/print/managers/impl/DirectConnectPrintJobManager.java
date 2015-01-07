/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;

import net.sf.wubiq.interfaces.INotifiablePrintService;
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
	
	/**
	 * While you can directly create an instance of this queue, we encourage to use the
	 * {@link net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory#getRemotePrintJobManager(String, net.sf.wubiq.print.managers.RemotePrintJobManagerType)}
	 * method instead.
	 */
	protected DirectConnectPrintJobManager(){
		
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public synchronized void initialize() throws Exception {
		connectors = new HashMap<String, DirectConnectorQueue>();
		associatedQueue = new HashMap<Long, String>();
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#addRemotePrintJob(java.lang.String, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public synchronized long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		IDirectConnectorQueue queue = directConnector(queueId);
		long lastJobId = RemotePrintJobManagerFactory.nextJobId();
		queue.addPrintJob(lastJobId, remotePrintJob);
		associatedQueue.put(lastJobId, queueId);
		PrintService printService = remotePrintJob.getPrintService();
		if (printService != null) {
			if (printService instanceof INotifiablePrintService) {
				INotifiablePrintService notifiable = (INotifiablePrintService)printService;
				notifiable.printJobCreated(lastJobId);
			}
		}
		return lastJobId;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#removeRemotePrintJob(long)
	 */
	@Override
	public synchronized boolean removeRemotePrintJob(long jobId) {
		DirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			IRemotePrintJob remotePrintJob = getRemotePrintJob(jobId, true);
			PrintService printService = remotePrintJob.getPrintService();
			if (printService != null) {
				if (printService instanceof INotifiablePrintService) {
					INotifiablePrintService notifiable = (INotifiablePrintService)printService;
					notifiable.printJobFinished(jobId);
				}
			}
			queue.removePrintJob(jobId);
		}
		return true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getRemotePrintJob(long, boolean)
	 */
	@Override
	public synchronized IRemotePrintJob getRemotePrintJob(long jobId, boolean fullPrintJob) {
		DirectConnectorQueue queue = associatedQueue(jobId);
		return queue.remotePrintJob(jobId);

	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintJobs(java.lang.String, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public synchronized Collection<Long> getPrintJobs(String queueId,
			RemotePrintJobStatus status) {
		IDirectConnectorQueue queue = directConnector(queueId);
		return queue.printJobs();
	}

	/**
	 * Returns the direct connector queue manager.
	 * @param jobId Id of the associated job.
	 * @return Queue handler for print jobs.
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
	public synchronized IDirectConnectorQueue directConnector(String queueId) {
		DirectConnectorQueue queue = connectors.get(queueId);
		if (queue == null) {
			queue = new DirectConnectorQueue(queueId);
			connectors.put(queueId, queue);
		}
		return queue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintServicePendingJobs(java.lang.String, javax.print.PrintService)
	 */
	public int getPrintServicePendingJobs(String queueId, PrintService printService) {
		int returnValue = 0;
		IDirectConnectorQueue queue = directConnector(queueId);
		for (Long jobId : queue.printJobs()) {
			IRemotePrintJob printJob = queue.remotePrintJob(jobId);
			if (printJob != null) {
				if (printJob.getPrintService().equals(printService)) {
					returnValue++;
				}
			} else {
				queue.removePrintJob(jobId);
			}
		}
		return returnValue;
	}
}
