/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.util.Collection;

import javax.print.PrintService;

import net.sf.wubiq.dao.WubiqPrintJobDao;
import net.sf.wubiq.dao.WubiqServerDao;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Handles direct communication with printers.
 * @author Federico Alcantara
 *
 */
public class DirectConnectorPersistedQueue extends DirectConnectorQueueBase {
	private long onProcess;
	
	/**
	 * Creates the unique instance of DirectConnectorQueue. For each queue (Print Service) a new instance is created.
	 * @param queueId
	 */
	protected DirectConnectorPersistedQueue(String queueId) {
		super(queueId);
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#addPrintJob(long, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public void addPrintJob(long jobId, IRemotePrintJob remotePrintJob) {
		throw new UnsupportedOperationException("Deprecated method");
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#addPrintJob(long, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public synchronized long addPrintJob(IRemotePrintJob remotePrintJob) {
		long jobId = 
				WubiqPrintJobDao.INSTANCE.addPrintJob(queueId(), remotePrintJob);
		WubiqServerDao.INSTANCE.addPrintJob(jobId);
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
		WubiqPrintJobDao.INSTANCE.remove(jobId);
		WubiqServerDao.INSTANCE.removePrintJob(jobId);
		super.removePrintJob(jobId);
		return true;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#remotePrintJob(long)
	 */
	@Override
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		IRemotePrintJob printJob = jobBucket(jobId).printJob;
		if (printJob == null) {
			printJob = WubiqPrintJobDao.INSTANCE.remotePrintJob(jobId, true);
		}
		return printJob;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#printJobs()
	 */
	@Override
	public synchronized Collection<Long> printJobs() {
		return WubiqPrintJobDao.INSTANCE.remotePrintJobs(queueId(), RemotePrintJobStatus.NOT_PRINTED);
	}
	
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#pendingPrintJobs()
	 */
	@Override
	public synchronized int pendingPrintJobs(PrintService printService) {
		return WubiqPrintJobDao.INSTANCE.pendingPrintJobs(queueId(), printService);
	}
	
	/**
	 * Resets the process.
	 */
	private void resetProcess() {
		onProcess = -1l;
	}	
}
