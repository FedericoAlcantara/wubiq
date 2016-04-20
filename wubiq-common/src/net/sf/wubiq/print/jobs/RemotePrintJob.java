/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.awt.print.PageFormat;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.PageableUtils;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class RemotePrintJob implements IRemotePrintJob {
	private static final Log LOG = LogFactory.getLog(RemotePrintJob.class);
	private PrintJobAttributeSet printJobAttributeSet;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private DocAttributeSet docAttributeSet;
	private DocFlavor docFlavor;
	private PrintService printService;
	private String printServiceName;
	private String printServiceClientName;
	private RemotePrintJobStatus status;
	private PageFormat pageFormat;
	private Object printData;
	private DocFlavor originalDocFlavor;
	private Boolean usesDirectConnect;
	private Boolean supportsOnlyPageable;
	private String remotePrintServiceName;
	
	public RemotePrintJob() {
	}

	public RemotePrintJob(PrintService printService) {
		this.printService = printService;
		status = RemotePrintJobStatus.NOT_PRINTED;
		Method getRemoteName;
		try {
			getRemoteName = printService.getClass().getDeclaredMethod("getRemoteName", new Class[]{});
			printServiceName = (String)getRemoteName.invoke(printService, new Object[]{});
			if (printService.getName().contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
				printServiceClientName = printService.getName().split(WebKeys.REMOTE_SERVICE_SEPARATOR)[1];
			} else {
				printServiceClientName = printService.getName();
			}
		} catch (SecurityException e) {
			LOG.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			LOG.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public RemotePrintJob(String printServiceName) {
		this.remotePrintServiceName = printServiceName;
		if (printServiceName.contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
			this.printServiceName = printServiceName.split(WebKeys.REMOTE_SERVICE_SEPARATOR)[0];
			this.printServiceClientName = printServiceName.split(WebKeys.REMOTE_SERVICE_SEPARATOR)[1];
		} else {
			this.printServiceName = printServiceName;
			this.printServiceClientName = printServiceName;
		}
	}
	
	/**
	 * @see javax.print.DocPrintJob#addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener, javax.print.attribute.PrintJobAttributeSet)
	 */
	@Override
	public void addPrintJobAttributeListener(PrintJobAttributeListener listener,
			PrintJobAttributeSet attributeSet) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#addPrintJobListener(javax.print.event.PrintJobListener)
	 */
	@Override
	public void addPrintJobListener(PrintJobListener listener) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#getAttributes()
	 */
	@Override
	public PrintJobAttributeSet getAttributes() {
		return printJobAttributeSet;
	}

	/**
	 * @see javax.print.DocPrintJob#getPrintService()
	 */
	@Override
	public PrintService getPrintService() {
		return printService;
	}

	/**
	 * @see javax.print.DocPrintJob#print(javax.print.Doc, javax.print.attribute.PrintRequestAttributeSet)
	 */
	@Override
	public synchronized void print(Doc doc, PrintRequestAttributeSet printRequestAttributeSet)
			throws PrintException {
		boolean isDirectCommunicationEnabled = false;
		try {
			Method isDirectCommunicationEnabledMethod = printService.getClass().getDeclaredMethod("isDirectCommunicationEnabled", new Class[]{});
			isDirectCommunicationEnabled = (Boolean)isDirectCommunicationEnabledMethod.invoke(printService, new Object[]{});
		} catch (Exception e) {
			isDirectCommunicationEnabled = false;
		}
		if (!PrintServiceUtils.isSameVersion(printService)) {
			LOG.warn(printServiceClientName + " is connecting with different version client, please use the appropriate wubiq-client to ensure stable and optimal performance");
		}
		String uuid = null;
		try {
			Method getUuid = printService.getClass().getDeclaredMethod("getUuid", new Class[]{});
			uuid = (String)getUuid.invoke(printService, new Object[]{});
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		if (!Is.emptyString(uuid)) {
			boolean usesDirectConnect = isDirectCommunicationEnabled;
			// Direct connect should be disable if the document is not a SERVICE_FORMATTED
			// and the printer does not support the type of document.
			// Direct connect is designed for pageables / printable.
			/*
			if (isDirectCommunicationEnabled) {
				if (!(doc.getDocFlavor() instanceof DocFlavor.SERVICE_FORMATTED)) {
					if (PrintServiceUtils.supportDocFlavor(printService, doc.getDocFlavor())) {
						usesDirectConnect = false;
					}
				}
			}
			*/
			printRemote(uuid, doc, printRequestAttributeSet, usesDirectConnect);
		}
	}

	/**
	 * Prints using the modern communication.
	 * @param uuid Unique print service id.
	 * @param doc Simple doc object.
	 * @param printRequestAttributeSet Print request attribute set.
	 * @param printAsPageable If true job is printed converting it to Pageable / Printable.
	 * @throws PrintException
	 */
	private void printRemote(String uuid, Doc doc, PrintRequestAttributeSet printRequestAttributeSet, 
			boolean usesDirectConnect) 
			throws PrintException {
		try {
			IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager(uuid);

			this.printRequestAttributeSet = printRequestAttributeSet;
			this.docAttributeSet = doc.getAttributes();
			this.docFlavor = doc.getDocFlavor();
			this.originalDocFlavor = doc.getDocFlavor();
			this.printData = doc.getPrintData();
			this.usesDirectConnect = usesDirectConnect;
			this.supportsOnlyPageable = false; // if false printer is capable of handling other types of sources.
			if (usesDirectConnect) {
				if (!(doc.getDocFlavor() instanceof DocFlavor.SERVICE_FORMATTED)) {
					if (PrintServiceUtils.supportDocFlavor(printService, doc.getDocFlavor())) {
						this.supportsOnlyPageable = true; // printer only handles pageable.
						this.docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
					}
				}
			}

			manager.addRemotePrintJob(uuid, this);
		} catch (IOException e) {
			throw new PrintException(e);
		} finally {
			
		}
	}
	
	/**
	 * Updates this element with the doc information.
	 * @deprecated Not is use anymore. SEVERE performance penalty.
	 * @param doc Document to be printed
	 * @param printRequestAttributeSet Request attribute.
	 * @throws PrintException
	 */
	public void update(Doc doc, PrintRequestAttributeSet printRequestAttributeSet)
			throws PrintException {
		this.printRequestAttributeSet = printRequestAttributeSet;
		this.docAttributeSet = doc.getAttributes();
		this.docFlavor = doc.getDocFlavor();
		try {
			printData = doc.getPrintData();
			InputStream transformed = PageableUtils.INSTANCE.getStreamForBytes(printData, getPageFormat(), printRequestAttributeSet);
			printData = transformed;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * @see javax.print.DocPrintJob#removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener)
	 */
	@Override
	public void removePrintJobAttributeListener(PrintJobAttributeListener arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#removePrintJobListener(javax.print.event.PrintJobListener)
	 */
	@Override
	public void removePrintJobListener(PrintJobListener arg0) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Sets a new PrintJob attributeSet.
	 * @deprecated Use setPrintJobAttributeSet instead.
	 * @param printJobAttributeSet
	 */
	public void setAttributes(PrintJobAttributeSet printJobAttributeSet) {
		this.printJobAttributeSet = printJobAttributeSet;
	}
	
	/**
	 * 
	 * @return A Reader for the input data.
	 * @throws IOException
	 */
	public InputStream getPrintData() throws IOException {
		return (InputStream)printData;
	}
	
	/**
	 * Just the previously saved object.
	 * @return Print data object.
	 */
	public Object getPrintDataObject() {
		return printData;
	}

	@Override
	public void setOriginalDocFlavor(DocFlavor originalDocFlavor) {
		this.originalDocFlavor = originalDocFlavor;
	}
	
	/**
	 * 
	 * @return The original doc flavor for the print data.
	 */
	public DocFlavor getOriginalDocFlavor() {
		return originalDocFlavor;
	}

	/**
	 * Sets the document flavor.
	 * @param docFlavor New document flavor to set.
	 */
	public void setDocFlavor(DocFlavor docFlavor){
		this.docFlavor = docFlavor;
	}
	
	/**
	 * 
	 * @return The original doc flavor for the print data.
	 */
	@Override
	public DocFlavor getDocFlavor() {
		return docFlavor;
	}
	
	/**
	 * 
	 * @return The original attribute set for the document.
	 */
	public DocAttributeSet getDocAttributeSet() {
		return docAttributeSet;
	}
	
	/**
	 * @return The original print request attribute set for the print action.
	 */
	public PrintRequestAttributeSet getPrintRequestAttributeSet() {
		return printRequestAttributeSet;
	}
	
	/**
	 * 
	 * @return The underlying print service name.
	 */
	public String getPrintServiceName() {
		return printServiceName;
	}

	/**
	 * @return Current print job status
	 */
	public RemotePrintJobStatus getStatus() {
		return status;
	}

	/**
	 * Sets a new status for the print job
	 * @param status the status to set.
	 */
	public void setStatus(RemotePrintJobStatus status) {
		this.status = status;
	}

	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#getPageFormat()
	 */
	public PageFormat getPageFormat() {
		if (pageFormat == null) {
			pageFormat = PageableUtils.INSTANCE.getPageFormat(printRequestAttributeSet);
		}
		return pageFormat;
	}

	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#setDocAttributeSet(javax.print.attribute.DocAttributeSet)
	 */
	@Override
	public void setDocAttributeSet(DocAttributeSet docAttributeSet) {
		this.docAttributeSet = docAttributeSet;
	}
	
	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#setPrintRequestAttributeSet(javax.print.attribute.PrintRequestAttributeSet)
	 */
	@Override
	public void setPrintRequestAttributeSet(
			PrintRequestAttributeSet printRequestAttributeSet) {
		this.printRequestAttributeSet = printRequestAttributeSet;
	}
	
	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#setPrintJobAttributeSet(javax.print.attribute.PrintJobAttributeSet)
	 */
	@Override
	public void setPrintJobAttributeSet(
			PrintJobAttributeSet printJobAttributeSet) {
		this.printJobAttributeSet = printJobAttributeSet;
	}
	
	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#setPrintDataObject(java.lang.Object)
	 */
	@Override
	public void setPrintDataObject(Object printDataObject) {
		this.printData = printDataObject;
	}
	
	/**
	 * @param pageFormat the pageFormat to set
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}

	@Override
	public Boolean getUsesDirectConnect() {
		return usesDirectConnect;
	}
	
	@Override
	public void setUsesDirectConnect(Boolean usesDirectConnect) {
		this.usesDirectConnect = usesDirectConnect;
	}

	/**
	 * @return the supportsOnlyPageable
	 */
	public Boolean getSupportsOnlyPageable() {
		return supportsOnlyPageable;
	}

	/**
	 * Indicates if the remote supports ONLY pageable documents.
	 * @param usesPageable The state to set.
	 */
	public void setSupportsOnlyPageable(Boolean usesPageable) {
		this.supportsOnlyPageable = usesPageable;
	}
	
	/**
	 * @see net.sf.wubiq.print.jobs.IRemotePrintJob#getRemotePrintServiceName()
	 */
	@Override
	public String getRemotePrintServiceName() {
		if (printService == null) {
			return remotePrintServiceName;
		}
		return printService.getName();
	}
}
