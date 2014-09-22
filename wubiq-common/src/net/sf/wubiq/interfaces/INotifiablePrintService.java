/**
 * 
 */
package net.sf.wubiq.interfaces;



/**
 * Implementation contract of a notifiable print service.
 * @author Federico Alcantara
 *
 */
public interface INotifiablePrintService {
	/**
	 * Indicates that a job has been created in the pool.
	 * @param jobId Unique id of the job.
	 */
	void printJobCreated(long jobId);
	
	/**
	 * Indicates that a job has been printed and consumed.
	 * @param jobId Job just printed.
	 */
	void printJobFinished(long jobId);
}
