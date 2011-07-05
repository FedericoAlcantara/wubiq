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
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public void initialize() {
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#addRemotePrintJob(java.lang.String, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		lastJobId++;
		getPrintJobs().put(lastJobId, queueId);
		getQueue(queueId).put(lastJobId, remotePrintJob);
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
			returnValue = queue.remove(jobId) != null;
		}
		getPrintJobs().remove(jobId);
		Runtime.getRuntime().gc();
		return returnValue;
	}
	
	@Override
	public IRemotePrintJob getRemotePrintJob(long jobId) {
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

}
