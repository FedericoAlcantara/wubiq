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
	
	/**
	 * Sets the converted state
	 * @param converted
	 */
	void setConverted(boolean converted);
	
	/**
	 * If true indicates that the value was converted. 
	 */
	boolean isConverted();
	
	/**
	 * Sets the height of the page
	 * @param pageHeight Page height
	 */
	void setPageHeight(float pageHeight);
	
	/**
	 * 
	 * @return Current height of the page
	 */
	float getPageHeight();

	/**
	 * Sets the width of the page
	 * @param pageWidth Page width
	 */
	void setPageWidth(float pageWidth);
	
	/**
	 * 
	 * @return Current width of the page
	 */
	float getPageWidth();
}
