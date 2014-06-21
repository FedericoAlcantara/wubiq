/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.awt.print.PageFormat;
import java.io.IOException;
import java.io.InputStream;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * Represents the contract of a remote print job
 * @author Federico Alcantara
 *
 */
public interface IRemotePrintJob extends DocPrintJob {
	/**
	 * 
	 * @return The underlying print service name.
	 */
	String getPrintServiceName();
	
	/**
	 * 
	 * @return The original doc flavor for the print data.
	 */
	DocFlavor getDocFlavor() ;
	
	/**
	 * 
	 * @return The original attribute set for the document.
	 */
	DocAttributeSet getDocAttributeSet();
	
	/**
	 * @return The original print request attribute set for the print action.
	 */
	PrintRequestAttributeSet getPrintRequestAttributeSet();
	
	/**
	 * 
	 * @return A Reader for the input data.
	 * @throws IOException
	 */
	InputStream getPrintData() throws IOException;
	
	/**
	 * Just the previously saved object as it is, without conversions or manipulation.
	 * @return Print data object.
	 */
	Object getPrintDataObject();
	
	/**
	 * Sets a new status for the print job
	 * @param status the status to set.
	 */
	void setStatus(RemotePrintJobStatus status);
	
	/**
	 * Return the current page format of the remote print object.
	 * @return PageFormat instance.
	 */
	PageFormat getPageFormat();
}
