package net.sf.wubiq.dao;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Federico Alcantara
 *
 */
public enum WubiqPrintJobDao {
	INSTANCE;
	private final Log LOG = LogFactory.getLog(WubiqPrintJobDao.class);
	
	/**
	 * Finds a given print job.
	 * @param jobId Print job to find.
	 * @return Found object or null.
	 */
	private WubiqPrintJob find(long jobId) {
		WubiqPrintJob returnValue = PersistenceManager.em().find(WubiqPrintJob.class, jobId);
		return returnValue;
	}
	
	/**
	 * Removes a given print job.
	 * @param jobId Print job to remove.
	 */
	public void remove(long jobId) {
		try {
			WubiqPrintJob job = find(jobId);
			if (job != null) {
				PersistenceManager.em().remove(job);
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
		}
	}
	
	/**
	 * Remote print job.
	 * @param jobId Id of the job.
	 * @param fullPrintJob Full print job.
	 * @return Print job instance or null.
	 * @throws PrintException
	 */
	public IRemotePrintJob remotePrintJob(long jobId, boolean fullPrintJob) throws PrintException {
		RemotePrintJob returnValue = null;
		try {
			WubiqPrintJob job = find(jobId);
			if (job != null) {
				PrintService printService = PrintServiceUtils.findPrinter(job.getPrintServiceName());
				if (printService != null) {
					returnValue = new RemotePrintJob(printService);
					DocAttributeSet docAttributeSet = (DocAttributeSet) PrintServiceUtils.convertToDocAttributeSet(job.getDocAttributes());
					PrintRequestAttributeSet printRequestAttributeSet = (PrintRequestAttributeSet) PrintServiceUtils.convertToPrintRequestAttributeSet(job.getPrintRequestAttributes());
					PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(job.getPrintJobAttributes());
					DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(job.getDocFlavor());
					returnValue.setAttributes(printJobAttributeSet);
					returnValue.setDocFlavor(docFlavor);
					if (fullPrintJob) {
						job.setStatus(RemotePrintJobStatus.PRINTING);
						PersistenceManager.em().merge(job);
						ByteArrayInputStream inputStream = new ByteArrayInputStream(job.getPrintData());
						Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, docAttributeSet);
						returnValue.update(doc, printRequestAttributeSet);
					}
				}
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Reads all remote print jobs.
	 * @param queueId Id of the queue.
	 * @param status Status to be read. If null reads all print jobs regardless of its status.
	 * @return List of remote printjobs.
	 */
	@SuppressWarnings("unchecked")
	public Set<Long> remotePrintJobs(String queueId, RemotePrintJobStatus status) {
		Set<Long> returnValue = new TreeSet<Long>();
		try {
			StringBuffer sql = new StringBuffer("SELECT printJobId FROM WubiqPrintJob"
					+ " WHERE queueId = :queueId");
			if (status != null) {
				sql.append(" AND ")
						.append("status = :status");
			}
			sql.append(" ORDER by printJobId");
			Query query = PersistenceManager.em().createQuery(sql.toString());
			query.setParameter("queueId", queueId);
			if (status != null) {
				query.setParameter("status", status);
			}
			returnValue.addAll(query.getResultList());
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Reads all remote print jobs.
	 * @param queueId Id of the queue.
	 * @param status Status to be read. If null reads all print jobs regardless of its status.
	 * @return List of remote printjobs.
	 */
	public Integer pendingPrintJobs(String queueId, PrintService printService) {
		Integer returnValue = 0;
		try {
			StringBuffer sql = new StringBuffer("SELECT count(printJobId) FROM WubiqPrintJob"
					+ " WHERE queueId = :queueId");
			sql.append(" AND ")
					.append("status = :status")
					.append(" AND ")
					.append("printServiceName = :printServiceName");
					
			Query query = PersistenceManager.em().createQuery(sql.toString());
			query.setParameter("queueId", queueId);
			query.setParameter("status", RemotePrintJobStatus.NOT_PRINTED);
			query.setParameter("printServiceName", printService != null ? printService.getName() : "");
			returnValue = (Integer) query.getSingleResult();
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Finds the associated queue id of the print job.
	 * @param jobId Associated queue id.
	 * @return Queue id or null if not found.
	 */
	public String associatedQueue(long jobId) {
		String queueId = null;
		try {
			queueId = (String)PersistenceManager.em().createQuery("SELECT queueId FROM WubiqPrintJob"
					+ " WHERE "
					+ "printJobId = :jobId")
					.setParameter("jobId", jobId)
					.getSingleResult();
			PersistenceManager.commit();
		} catch (NoResultException e) {
			PersistenceManager.rollback();
			LOG.debug(ExceptionUtils.getMessage(e));
			queueId = null;
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return queueId;
	}
}