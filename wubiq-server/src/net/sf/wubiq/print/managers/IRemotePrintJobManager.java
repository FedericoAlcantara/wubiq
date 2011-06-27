/**
 * 
 */
package net.sf.wubiq.print.managers;

import java.util.Collection;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Defines the behavior of Remote print job manager.
 * @author Federico Alcantara.
 *
 */
public interface IRemotePrintJobManager {
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
	 * @return IRemotePrintJob object or null if not found.
	 */
	IRemotePrintJob getRemotePrintJob(long jobId);
	
	/**
	 * Gather the list of print job ids for the given status. If status is null then all are
	 * retrieved.
	 * @param queueId Unique id of the queue to be read.
	 * @param status Status to be search for. Null indicates all.
	 * @return A collection of job ids. Never null.
	 */
	Collection<Long> getPrintJobs(String queueId, RemotePrintJobStatus status);
}
