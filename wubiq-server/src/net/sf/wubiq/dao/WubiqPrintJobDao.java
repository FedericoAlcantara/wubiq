package net.sf.wubiq.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.enums.PrintJobDataType;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerLabels;

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
	 * Adds a print job.
	 * @param queueId Id of the queue.
	 * @param remotePrintJob Remote printJob.
	 * @return
	 */
	public long addPrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		long returnValue = 0;
		try {
			WubiqPrintJob job = new WubiqPrintJob();

			String docAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getDocAttributeSet());
			String printRequestAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getPrintRequestAttributeSet());
			String printJobAttributes = PrintServiceUtils.serializeAttributes(remotePrintJob.getAttributes());
			String docFlavor = PrintServiceUtils.serializeDocFlavor(remotePrintJob.getDocFlavor());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Object data = remotePrintJob.getTransformed() != null
					? remotePrintJob.getTransformed() : remotePrintJob.getPrintDataObject();
			if (data instanceof InputStream) {
				IOUtils.INSTANCE.copy((InputStream)data, outputStream);
				job.setPrintJobDataType(PrintJobDataType.INPUT_STREAM);
			} else if (data instanceof Serializable){
				ObjectOutputStream serialOutput = new ObjectOutputStream(outputStream);
				serialOutput.writeObject(data);
				job.setPrintJobDataType(PrintJobDataType.SERIALIZABLE);
			} else {
				throw new RuntimeException(ServerLabels.get("server.error_could_not_persist_job"));
			}
			outputStream.flush();
			outputStream.close();
			job.setQueueId(queueId);
			job.setPrintServiceName(remotePrintJob.getPrintService() != null 
					? remotePrintJob.getPrintService().getName() : remotePrintJob.getPrintServiceName());
			job.setDocAttributes(docAttributes);
			job.setPrintRequestAttributes(printRequestAttributes);
			job.setPrintJobAttributes(printJobAttributes);
			job.setDocFlavor(docFlavor);
			job.setPrintData(outputStream.toByteArray());
			job.setStatus(RemotePrintJobStatus.NOT_PRINTED);
			job.setTime(new Date());
			job.setCommunicationType(remotePrintJob.getCommunicationType());
			job.setAppliedCommunicationType(remotePrintJob.getAppliedCommunicationType());
			PersistenceManager.em().persist(job);
			PersistenceManager.commit();
			returnValue = job.getPrintJobId();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Remote print job.
	 * @param jobId Id of the job.
	 * @param fullPrintJob Full print job.
	 * @return Print job instance or null.
	 * @throws PrintException
	 */
	public IRemotePrintJob remotePrintJob(long jobId, boolean fullPrintJob) {
		IRemotePrintJob returnValue = null;
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
					returnValue.setDocAttributeSet(docAttributeSet);
					returnValue.setPrintRequestAttributeSet(printRequestAttributeSet);
					returnValue.setPrintJobAttributeSet(printJobAttributeSet);
					returnValue.setDocFlavor(docFlavor);
					if (fullPrintJob) {
						Object object = null;
						ByteArrayInputStream inputStream = new ByteArrayInputStream(job.getPrintData());
						if (PrintJobDataType.INPUT_STREAM.equals(job.getPrintJobDataType())) {
							object = inputStream;
						} else if (PrintJobDataType.SERIALIZABLE.equals(job.getPrintJobDataType())) {
							ObjectInputStream input = new ObjectInputStream(inputStream);
							object = input.readObject();
							input.close();
						}
						returnValue.setPrintDataObject(object);
						job.setStatus(RemotePrintJobStatus.PRINTING);
						PersistenceManager.em().merge(job);
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
			returnValue = ((Long) query.getSingleResult()).intValue();
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