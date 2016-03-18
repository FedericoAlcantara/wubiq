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

import net.sf.wubiq.enums.RemotePrintJobCommunicationType;

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
	 * Gets a transformed (normally a pageable or printable serialized) version of print data object.
	 * @return Transformed as input stream.
	 */
	InputStream getTransformed();
	
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
	
	/**
	 * @return Type of expected communication for the print job.
	 */
	RemotePrintJobCommunicationType getCommunicationType();

	/**
	 * @return Type of realized communication for the print job.
	 */
	RemotePrintJobCommunicationType getAppliedCommunicationType();

	/**
	 * Sets the type of communication applied.
	 * @param appliedCommunicationType New communication applied.
	 */
	void setAppliedCommunicationType(RemotePrintJobCommunicationType appliedCommunicationType);
}
