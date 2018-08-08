/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.print.PrintService;

import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.dao.WubiqPrintJobDao;
import net.sf.wubiq.interfaces.INotifiablePrintService;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.PrintServiceUtils;

/**
 * Manages direct connection with clients.
 * @author Federico Alcantara
 *
 */
public class DirectConnectPrintJobManager implements IDirectConnectPrintJobManager {
	private Map<String, IDirectConnectorQueue> connectors;
	private Map<Long, String> associatedQueue;
	
	/**
	 * While you can directly create an instance of this queue, we encourage to use the
	 * {@link net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory#getRemotePrintJobManager(String)}
	 * method instead.
	 */
	public DirectConnectPrintJobManager() {
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public synchronized void initialize() throws Exception {
		connectors = new HashMap<String, IDirectConnectorQueue>();
		associatedQueue = new HashMap<Long, String>();
	}

	/**
	 * @return the directConnect
	 */
	public boolean isDirectConnect(long jobId) {
		IRemotePrintJob printJob = getRemotePrintJob(jobId, false);
		return printJob.getUsesDirectConnect();
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#addRemotePrintJob(java.lang.String, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public synchronized long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		IDirectConnectorQueue queue = directConnector(queueId);
		long lastJobId = queue.addPrintJob(remotePrintJob);
		if (!ConfigurationKeys.isPersistenceActive()) {
			associatedQueue.put(lastJobId, queueId);
		}
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
		IRemotePrintJob remotePrintJob = getRemotePrintJob(jobId, true);
		IDirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			if (remotePrintJob != null) {
				PrintService printService = remotePrintJob.getPrintService();
				queue.removePrintJob(jobId);
				if (printService != null) {
					if (printService instanceof INotifiablePrintService) {
						INotifiablePrintService notifiable = (INotifiablePrintService)printService;
						notifiable.printJobFinished(jobId);
					}
				}
			}
		}
		return true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectPrintJobManager#hasLocalPrintJob(java.lang.Long)
	 */
	@Override
	public boolean hasLocalPrintJob(Long jobId) {
		IDirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			return queue.hasLocalPrintJob(jobId);
		}
		return false;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getRemotePrintJob(long, boolean)
	 */
	@Override
	public synchronized IRemotePrintJob getRemotePrintJob(long jobId, boolean fullPrintJob) {
		IRemotePrintJob returnValue = null;
		IDirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			returnValue = queue.remotePrintJob(jobId, fullPrintJob);
			if (returnValue == null) {
				queue.removePrintJob(jobId);
			} else {
				if (fullPrintJob) {
					// If we haven't tell the client the type of communication we used for the print job data transfer.
					PrintService printService = returnValue.getPrintService();
					if (printService != null) {
						if (printService instanceof INotifiablePrintService) {
							INotifiablePrintService notifiable = (INotifiablePrintService)printService;
							notifiable.printJobStarted(jobId);
						}
					}
				}
			}
		}
		return returnValue;

	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintJobs(java.lang.String, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public synchronized Collection<Long> getPrintJobs(String queueId,
			RemotePrintJobStatus status) {
		Collection<Long> returnValue = new ArrayList<Long>();
		IDirectConnectorQueue queue = directConnector(queueId);
		for (Long jobId : queue.printJobs()) {
			returnValue.add(jobId);
		}
		return returnValue;
	}

	/**
	 * Returns the direct connector queue manager.
	 * Uses the local queue as a preferred way.
	 * @param jobId Id of the associated job.
	 * @return Queue handler for print jobs.
	 */
	private synchronized IDirectConnectorQueue associatedQueue(long jobId) {
		String queueId = null;
		if (ConfigurationKeys.isPersistenceActive()) {
			queueId = WubiqPrintJobDao.INSTANCE.associatedQueue(jobId);
		} else {
			queueId = associatedQueue.get(jobId);
		}
		return directConnector(queueId);
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectPrintJobManager#directConnector(java.lang.String)
	 */
	@Override
	public synchronized IDirectConnectorQueue directConnector(String queueId) {
		IDirectConnectorQueue queue = null;
		if (!Is.emptyString(queueId)) {
			queue = connectors.get(queueId);
			if (queue == null) {
				if (ConfigurationKeys.isPersistenceActive()) {
					queue = new DirectConnectorPersistedQueue(queueId);
				} else {
					queue = new DirectConnectorQueue(queueId);
				}
				connectors.put(queueId, queue);
			}
		}
		return queue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintServicePendingJobs(java.lang.String, javax.print.PrintService)
	 */
	public synchronized int getPrintServicePendingJobs(String queueId, PrintService printService) {
		int returnValue = 0;
		IDirectConnectorQueue queue = directConnector(queueId);
		if (queue != null) {
			returnValue = queue.pendingPrintJobs(printService);
		}
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectPrintJobManager#calculatePrintJobs(java.lang.String, javax.print.PrintService, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public int calculatePrintJobs(String queueId, PrintService printService,
			RemotePrintJobStatus status) {
		int returnValue = 0;
		IDirectConnectorQueue queue = directConnector(queueId);
		if (queue != null) {
			returnValue = queue.calculatePrintJobs(printService, status);
		}
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#startPrintJob(long)
	 */
	@Override
	public synchronized void startPrintJob(long jobId) {
		IDirectConnectorQueue queue = associatedQueue(jobId);
		if (queue != null) {
			IRemotePrintJob remotePrintJob = getRemotePrintJob(jobId, true);
			if (remotePrintJob != null) {
				PrintService printService = remotePrintJob.getPrintService();
				if (printService != null) {
					if (printService instanceof INotifiablePrintService) {
						INotifiablePrintService notifiable = (INotifiablePrintService)printService;
						notifiable.printJobStarted(jobId);
					}
				}
			}
		}		
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectPrintJobManager#isPrinting(javax.print.PrintService)
	 */
	public synchronized boolean isPrinting(PrintService printService) {
		if (PrintServiceUtils.isRemotePrintService(printService)) {
			return ((RemotePrintService)printService).isPrinting();
		} else {
			return false;
		}
	}
	
	/* **************************************************************
	 * Developers routines should not be enabled during production.
	 * Useful for enabling testing.
	 * **************************************************************
	 */
	/**
	 * Just clears the jobs in memory for the given queue, but keeps the persisted image intact.
	 * @param queueId Id of the queue.
	 */
	public synchronized void clearInMemoryPrintJobs(String queueId) {
		if ("true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_DEVELOPMENT_MODE))) {
			IDirectConnectorQueue queue = directConnector(queueId);
			if (queue != null
					&& queue instanceof DirectConnectorPersistedQueue) {
				((DirectConnectorPersistedQueue) queue).clearInMemoryPrintJobs();
			}
		}
	}
}
