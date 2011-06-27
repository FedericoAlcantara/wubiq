/**
 * 
 */
package org.wubiq.print.jobs;

import java.util.Collection;

import javax.print.attribute.Attribute;


/**
 * @author Federico Alcantara
 *
 */
public interface IRemotePrintJob {
	/**
	 * @return Contains the intended printServiceName.
	 */
	String getPrintServiceName();
	
	/**
	 * @return The list of preferences attributes.
	 */
	Collection<Attribute> getAttributes();
	
	/**
	 * @return The object to be printed.
	 */
	Object getPrintObject();
	
	/**
	 * @return Current job status.
	 */
	RemotePrintJobStatus getStatus();
	
	/**
	 * 
	 * @param status
	 */
	void setStatus(RemotePrintJobStatus status);
}
