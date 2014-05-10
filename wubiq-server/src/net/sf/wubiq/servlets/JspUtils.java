/**
 * 
 */
package net.sf.wubiq.servlets;

import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.utils.Is;

/**
 * @author Federico Alcantara
 *
 */
public class JspUtils {
	
	/**
	 * Returns the count of pending jobs.
	 * @param queueId Queue id.
	 * @return Count of pending jobs.
	 */
	public int getPendingJob(String queueId) {
		int returnValue = 0;
		if (!Is.emptyString(queueId)) {
			IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
			if (manager != null) {
				returnValue = manager.getPrintJobs(queueId, RemotePrintJobStatus.NOT_PRINTED).size();
			}
		}
		return returnValue;
	}
}
