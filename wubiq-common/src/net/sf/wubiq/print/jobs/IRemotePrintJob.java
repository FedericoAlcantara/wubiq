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
import javax.print.attribute.PrintJobAttributeSet;
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
	DocFlavor getOriginalDocFlavor();
	
	/**
	 * Sets a new original doc flavor.
	 * @param originalDocFlavor
	 */
	void setOriginalDocFlavor(DocFlavor originalDocFlavor);
	
	/**
	 * 
	 * @return The doc flavor for the data to be printed.
	 */
	DocFlavor getDocFlavor();
	
	/**
	 * Sets a new doc flavor.
	 * @param docFlavor
	 */
	void setDocFlavor(DocFlavor docFlavor);
	
	/**
	 * 
	 * @return The original attribute set for the document.
	 */
	DocAttributeSet getDocAttributeSet();
	
	/**
	 * Sets a new doc attribute set.
	 * @param docAttributeSet
	 */
	void setDocAttributeSet(DocAttributeSet docAttributeSet);
	
	/**
	 * @return The original print request attribute set for the print action.
	 */
	PrintRequestAttributeSet getPrintRequestAttributeSet();
	
	/**
	 * Sets a new print request attribute set.
	 * @param attributeSet
	 */
	void setPrintRequestAttributeSet(PrintRequestAttributeSet printRequestAttributeSet);
	
	
	/**
	 * @return Current print job attribute set.
	 */
	PrintJobAttributeSet getAttributes();
	
	/**
	 * Sets a new print job attribute set.
	 * @param printJobAttributeSet
	 */
	void setPrintJobAttributeSet(PrintJobAttributeSet printJobAttributeSet);
	
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
	 * Sets a new print data object.
	 * @param printDataObject Print data object to set.
	 */
	void setPrintDataObject(Object printDataObject);
	
	/**
	 * Sets a new status for the print job
	 * @param status the status to set.
	 */
	void setStatus(RemotePrintJobStatus status);

	
	/**
	 * @return True if this print job uses direct connect.
	 */
	Boolean getUsesDirectConnect();
	
	/**
	 * @param usesDirectConnect Indicates if uses direct connect.
	 */
	void setUsesDirectConnect(Boolean usesDirectConnect);
	
	/**
	 * For compatibility for printers supporting only serialized pageable.
	 * Or in the event that data was pageable and persisted.
	 * @return True if the printer ONLY uses pageables.
	 */
	Boolean getSupportsOnlyPageable();
	
	/**
	 * Sets the status of the uses Pageable.
	 * @param supportsOnlyPageable New state.
	 */
	void setSupportsOnlyPageable(Boolean supportsOnlyPageable);
	
	/**
	 * Return the current page format of the remote print object.
	 * @return PageFormat instance.
	 */
	PageFormat getPageFormat();
	
}
