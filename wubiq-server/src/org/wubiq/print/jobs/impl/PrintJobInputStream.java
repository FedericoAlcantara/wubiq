/**
 * 
 */
package org.wubiq.print.jobs.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.print.attribute.Attribute;

import org.wubiq.print.jobs.IRemotePrintJob;
import org.wubiq.print.jobs.RemotePrintJobStatus;

/**
 * Implements IRemotePrintJob by using input stream as report transport.
 * @author Federico Alcantara
 *
 */
public class PrintJobInputStream implements IRemotePrintJob {
	private Collection<Attribute> attributes;
	private InputStream printObject;
	private RemotePrintJobStatus status;
	private String printServiceName;
	
	public PrintJobInputStream(String printServiceName, InputStream inputStream, Collection<Attribute>attributes) {
		if (attributes == null) {
			attributes = new ArrayList<Attribute>();
		}
		this.printObject = inputStream;
		this.attributes = attributes;
		this.printServiceName = printServiceName;
	}
	
	@Override
	public String getPrintServiceName() {
		return printServiceName;
	}
	
	/**
	 * @see org.wubiq.print.jobs.IRemotePrintJob#getAttributes()
	 */
	@Override
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @see org.wubiq.print.jobs.IRemotePrintJob#getPrintObject()
	 */
	@Override
	public Object getPrintObject() {
		return printObject;
	}

	/**
	 * @see org.wubiq.print.jobs.IRemotePrintJob#getStatus()
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
