/**
 * 
 */
package net.sf.wubiq.print.managers;

import java.util.Collection;

import javax.print.PrintService;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Defines the contract of Remote print job manager.
 * Implementations of remote print job manager decides how print jobs are stored and removed.
 * @author Federico Alcantara.
 *
 */
public interface IRemotePrintJobManager {
	/**
	 * Initializes the print job manager.
	 */
	void initialize() throws Exception;
	
	/**
	 * Add remote print job to a queue. The implementation of the queue
	 * is not specified, nor its behavior (LIFO, FIFO, any other).
	 * @param queueId Id of the queue that will have the print job. It can be interpreted
	 * as a print service unique identifier.
	 * @param remotePrintJob Remote print job to manage.
	 * @return Print job unique id.
	 */
	long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob);
	
	/**
	 * Makes the print job no more available for further usage.
	 * @param jobId Unique id of the job to remove.
	 * @return True if the printJob  was successfully removed.
	 */
	boolean removeRemotePrintJob(long jobId);
	
	/**
	 * Return the instance of a print job.
	 * @param jobId Id of the job to find.
	 * @param fullPrintJob if true, just read remote print object must be returned.
	 * @return IRemotePrintJob object or null if not found.
	 */
	IRemotePrintJob getRemotePrintJob(long jobId, boolean fullPrintJob);
	
	/**
	 * Gather the list of print job ids for the given status. If status is null then all are
	 * retrieved.
	 * @param queueId Unique id of the queue to be read.
	 * @param status Status to be search for. Null indicates all.
	 * @return A collection of job ids. Never null.
	 */
	Collection<Long> getPrintJobs(String queueId, RemotePrintJobStatus status);
	
	/**
	 * Get print service pending jobs.
	 * @param queueId Unique id of the queue to be read.
	 * @param printService Print service to poll.
	 * @return Number of pending jobs for the printer.
	 */
	int getPrintServicePendingJobs(String queueId, PrintService printService);
	
	/**
	 * Calculates the total print jobs.
	 * @param queueId Id of the associated queue.
	 * @param printService Print service to calculate the total print jobs.
	 * @param status Status of the requested print job. If null ALL print jobs should be counted.
	 * @return Total number of print jobs found or null.
	 */
	int calculatePrintJobs(String queueId, PrintService printService, RemotePrintJobStatus status);

	/**
	 * Starts a print job.
	 * @param jobId Job Id to start.
	 */
	void startPrintJob(long jobId);
	
}
