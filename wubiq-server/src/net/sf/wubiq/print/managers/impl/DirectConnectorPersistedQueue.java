/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import javax.print.PrintException;
import javax.print.PrintService;

import net.sf.wubiq.dao.WubiqPrintJobDao;
import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.PrintServiceUtils;

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
		long returnValue = 0l;
		try {
			String docAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getDocAttributeSet());
			String printRequestAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getPrintRequestAttributeSet());
			String printJobAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getAttributes());
			String docFlavor = PrintServiceUtils.serializeDocFlavor(remotePrintJob.getDocFlavor());
			InputStream inputStream = remotePrintJob.getPrintData();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			IOUtils.INSTANCE.copy(inputStream, outputStream);
			outputStream.close();
			
			WubiqPrintJob job = new WubiqPrintJob();
			job.setQueueId(queueId());
			job.setPrintServiceName(remotePrintJob.getPrintService() != null 
					? remotePrintJob.getPrintService().getName() : remotePrintJob.getPrintServiceName());
			job.setDocAttributes(docAttributes);
			job.setPrintRequestAttributes(printRequestAttributes);
			job.setPrintJobAttributes(printJobAttributes);
			job.setDocFlavor(docFlavor);
			job.setPrintData(outputStream.toByteArray());
			job.setStatus(RemotePrintJobStatus.NOT_PRINTED);
			job.setTime(new Date());
			PersistenceManager.em().persist(job);
			PersistenceManager.commit();
			returnValue = job.getPrintJobId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return returnValue;
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
		return true;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#remotePrintJob(long)
	 */
	@Override
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		try {
			return WubiqPrintJobDao.INSTANCE.remotePrintJob(jobId, true);
		} catch (PrintException e) {
			throw new RuntimeException(e);
		}
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
