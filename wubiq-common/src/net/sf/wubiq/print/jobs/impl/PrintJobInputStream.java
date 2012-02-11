/**
 * 
 */
package net.sf.wubiq.print.jobs.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.print.DocFlavor;
import javax.print.attribute.Attribute;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Implements IRemotePrintJob by using input stream as report transport.
 * This implementation relies on memory space for saving the print job document.
 * @author Federico Alcantara
 *
 */
public class PrintJobInputStream implements IRemotePrintJob {
	private Collection<Attribute> attributes;
	private InputStream printDocument;
	private RemotePrintJobStatus status;
	private String printServiceName;
	private DocFlavor docFlavor;
	private boolean converted;
	private float pageHeight;
	private float pageWidth;
	
	public PrintJobInputStream(String printServiceName, InputStream inputStream, Collection<Attribute>attributes,
			DocFlavor docFlavor) {
		if (attributes == null) {
			attributes = new ArrayList<Attribute>();
		}
		this.printDocument = inputStream;
		this.attributes = attributes;
		this.printServiceName = printServiceName;
		this.docFlavor = docFlavor;
	}
	
	@Override
	public String getPrintServiceName() {
		return printServiceName;
	}
	
	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#getAttributes()
	 */
	@Override
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#getPrintDocument()
	 */
	@Override
	public InputStream getPrintDocument() {
		return printDocument;
	}

	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#getStatus()
	 */
	@Override
	public RemotePrintJobStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(RemotePrintJobStatus status) {
		this.status = status;
	}

	/**
	 * @return the docFlavor
	 */
	public DocFlavor getDocFlavor() {
		return docFlavor;
	}

	/**
	 * @param docFlavor the docFlavor to set
	 */
	public void setDocFlavor(DocFlavor docFlavor) {
		this.docFlavor = docFlavor;
	}

	/**
	 * @return the converted
	 */
	@Override
	public boolean isConverted() {
		return converted;
	}

	/**
	 * @param converted the converted to set
	 */
	@Override
	public void setConverted(boolean converted) {
		this.converted = converted;
	}

	/**
	 * @return the pageHeight
	 */
	public float getPageHeight() {
		return pageHeight;
	}

	/**
	 * @param pageHeight the pageHeight to set
	 */
	public void setPageHeight(float pageHeight) {
		this.pageHeight = pageHeight;
	}

	/**
	 * @return the pageWidth
	 */
	public float getPageWidth() {
		return pageWidth;
	}

	/**
	 * @param pageWidth the pageWidth to set
	 */
	public void setPageWidth(float pageWidth) {
		this.pageWidth = pageWidth;
	}

}
