/**
 * 
 */
package net.sf.wubiq.print.jobs.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

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
	
	public PrintJobInputStream(String printServiceName, InputStream inputStream, Collection<Attribute>attributes) {
		if (attributes == null) {
			attributes = new ArrayList<Attribute>();
		}
		this.printDocument = inputStream;
		this.attributes = attributes;
		this.printServiceName = printServiceName;
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
	public Object getPrintDocument() {
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

}
