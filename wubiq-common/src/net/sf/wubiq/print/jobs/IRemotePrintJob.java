/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.io.InputStream;
import java.util.Collection;

import javax.print.DocFlavor;
import javax.print.attribute.Attribute;


/**
 * Defines the contract for remote print jobs.
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
	 * @return The document to be printed.
	 */
	InputStream getPrintDocument();
	
	/**
	 * @return Current job status.
	 */
	RemotePrintJobStatus getStatus();
	
	/**
	 * 
	 * @param status
	 */
	void setStatus(RemotePrintJobStatus status);
	
	/**
	 * 
	 */
	DocFlavor getDocFlavor();
	
	void setConverted(boolean converted);
	
	/**
	 * 
	 */
	boolean isConverted();
}
