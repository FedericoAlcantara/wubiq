/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.Collection;

import javax.print.PrintService;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Handles direct communication with printers.
 * @author Federico Alcantara
 *
 */
public class DirectConnectorQueue extends DirectConnectorQueueBase {
	private long onProcess;
	
	/**
	 * Creates the unique instance of DirectConnectorQueue. For each queue (Print Service) a new instance is created.
	 * @param queueId
	 */
	protected DirectConnectorQueue(String queueId) {
		super(queueId);
		onProcess = -1l;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#addPrintJob(long, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Deprecated
	@Override
	public synchronized void addPrintJob(long jobId, IRemotePrintJob remotePrintJob) {
		jobBucket(jobId).printJob = remotePrintJob;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#addPrintJob(net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public synchronized long addPrintJob(IRemotePrintJob remotePrintJob) {
		long jobId = RemotePrintJobManagerFactory.nextJobId();
		jobBucket(jobId).printJob = remotePrintJob;
		return jobId;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#removePrintJob(long)
	 */
	@Override
	public synchronized boolean removePrintJob(long jobId) {
		if (jobId == onProcess) {
			resetProcess();
		}
		return super.removePrintJob(jobId);
	}
	
	/**
	 * @deprecated
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#remotePrintJob(long)
	 */
	@Override
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		return jobBucket(jobId).printJob;
	}
	
	@Override
	public IRemotePrintJob remotePrintJob(long jobId, boolean full) {
		IRemotePrintJob returnValue = jobBucket(jobId).printJob;
		if (returnValue != null && full) {
			returnValue.setStatus(RemotePrintJobStatus.PRINTING);
		}
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#printJobs()
	 */
	@Override
	public synchronized Collection<Long> printJobs() {
		return jobBuckets.keySet();
	}
		
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#pendingPrintJobs(javax.print.PrintService)
	 */
	@Override
	public synchronized int pendingPrintJobs(PrintService printService) {
		int returnValue = 0;
		for (Long jobId : printJobs()) {
			IRemotePrintJob printJob = remotePrintJob(jobId, false);
			if (printJob != null) {
				if (printJob.getPrintService().equals(printService)) {
					returnValue++;
				}
			} 
		}
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#calculatePrintJobs(javax.print.PrintService, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public synchronized int calculatePrintJobs(PrintService printService,
			RemotePrintJobStatus status) {
		int returnValue = 0;
		for (Long jobId : printJobs()) {
			IRemotePrintJob printJob = remotePrintJob(jobId, false);
			if (printJob != null && printJob.getPrintService().equals(printService)) {
				if (status == null ||
						status.equals(printJob.getStatus())) {
					returnValue++;
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Resets the process.
	 */
	private void resetProcess() {
		onProcess = -1l;
	}	
}
