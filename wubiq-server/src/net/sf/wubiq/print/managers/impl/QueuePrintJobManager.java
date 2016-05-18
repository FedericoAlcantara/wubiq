/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.print.PrintService;

import net.sf.wubiq.interfaces.INotifiablePrintService;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;


/**
 * Implements a basic print job manager using in-memory queues.
 * @author Federico Alcantara
 *
 */
public class QueuePrintJobManager implements IRemotePrintJobManager {
	private static long lastJobId;
	private static Map<Long, String> printJobs;
	private static Map<String, Map<Long, IRemotePrintJob>> queues;
	
	/**
	 * While you can directly create an instance of this queue, we encourage to use the
	 * {@link net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory#getRemotePrintJobManager(String, net.sf.wubiq.print.managers.RemotePrintJobManagerType)}
	 * method instead.
	 */
	protected QueuePrintJobManager(){
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public void initialize() {
	}

	/**
	 * adds a remote print job to the queue. 
	 */
	@Override
	public long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		lastJobId = RemotePrintJobManagerFactory.nextJobId();
		getPrintJobs().put(lastJobId, queueId);
		getQueue(queueId).put(lastJobId, remotePrintJob);
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
	public boolean removeRemotePrintJob(long jobId) {
		boolean returnValue = true;
		Map<Long, IRemotePrintJob> queue = getQueue(jobId);
		if (queue != null) {
			IRemotePrintJob remotePrintJob = getRemotePrintJob(jobId, true);
			PrintService printService = remotePrintJob.getPrintService();
			if (printService != null) {
				if (printService instanceof INotifiablePrintService) {
					INotifiablePrintService notifiable = (INotifiablePrintService)printService;
					notifiable.printJobFinished(jobId);
				}
			}
			returnValue = queue.remove(jobId) != null;
		}
		getPrintJobs().remove(jobId);
		Runtime.getRuntime().gc();
		return returnValue;
	}
	
	@Override
	public IRemotePrintJob getRemotePrintJob(long jobId, boolean fullPrintJob) {
		IRemotePrintJob returnValue = null;
		String queueId = getPrintJobs().get(jobId);
		if (queueId != null) {
			returnValue = getQueue(queueId).get(jobId);
		}
		return returnValue;
	}

	@Override
	public Collection<Long> getPrintJobs(String queueId,
			RemotePrintJobStatus status) {
		Collection<Long> returnValue = new ArrayList<Long>();
		for (Entry<Long, String> entry : getPrintJobs().entrySet()) {
			if (entry.getValue().equals(queueId)) {
				returnValue.add(entry.getKey());
			}
		}
		return returnValue;
	}

	private Map<Long, String> getPrintJobs() {
		if (printJobs == null) {
			printJobs = new TreeMap<Long, String>();
			lastJobId = 0;
		}
		return printJobs;
	}
	/**
	 * @return Queues of remote print job.
	 */
	private Map<String, Map<Long, IRemotePrintJob>> getQueues() {
		if (queues == null) {
			queues = new HashMap<String, Map<Long, IRemotePrintJob>>();
		}
		return queues;
	}
	
	/**
	 * Finds a given queue.
	 * @param queueId Id of the queue to search for.
	 * @return Queue object. Never null.
	 */
	private Map<Long, IRemotePrintJob> getQueue(String queueId) {
		Map<Long, IRemotePrintJob> returnValue = getQueues().get(queueId);
		if (returnValue == null) {
			returnValue = new TreeMap<Long, IRemotePrintJob>();
			getQueues().put(queueId, returnValue);
		}
		return returnValue;
	}
	
	/**
	 * Finds a given queue.
	 * @param jobId Id of the associated with the queue.
	 * @return Queue object. Null if no job id is associatd.
	 */
	private Map<Long, IRemotePrintJob> getQueue(long jobId) {
		Map<Long, IRemotePrintJob> returnValue = null;
		String queueId = getPrintJobs().get(jobId);
		if (queueId != null) {
			returnValue = getQueue(queueId);
		}
		return returnValue;
	}	

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintServicePendingJobs(java.lang.String, javax.print.PrintService)
	 */
	public int getPrintServicePendingJobs(String queueId, PrintService printService) {
		return 0;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#calculatePrintJobs(java.lang.String, javax.print.PrintService, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public int calculatePrintJobs(String queueId, PrintService printService,
			RemotePrintJobStatus status) {
		return 0;
	}
	
	@Override
	public void startPrintJob(long jobId) {
	}
}
