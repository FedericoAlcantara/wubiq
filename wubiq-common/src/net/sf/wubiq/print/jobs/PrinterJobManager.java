/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.awt.HeadlessException;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Semaphore for printing.
 * @author Federico Alcantara
 *
 */
public class PrinterJobManager extends PrinterJob {
	private static final Log LOG = LogFactory.getLog(PrinterJobManager.class);
	@SuppressWarnings("rawtypes")
	private static Class defaultPrinterJobClass;
	
	private int copies;
	private String jobName;
	private Pageable document;
	private Printable painter;
	private PageFormat pageFormat;
	private PrintService service;
	private PrinterJob defaultPrinterJob;
	
	public PrinterJobManager() {
		try {
			defaultPrinterJob = (PrinterJob) defaultPrinterJobClass.newInstance();
		} catch (InstantiationException e) {
			LOG.error(e);
		} catch (IllegalAccessException e) {
			LOG.error(e);
		}
	}
	
	/**
	 * @see java.awt.print.PrinterJob#cancel()
	 */
	@Override
	public void cancel() {
		
	}
	
	/**
	 * @see java.awt.print.PrinterJob#defaultPage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat defaultPage(PageFormat pageFormat) {
		return (PageFormat) pageFormat.clone();
	}

	/**
	 * @see java.awt.print.PrinterJob#getCopies()
	 */
	@Override
	public int getCopies() {
		return copies;
	}

	/**
	 * @see java.awt.print.PrinterJob#getJobName()
	 */
	@Override
	public String getJobName() {
		return jobName;
	}

	/**
	 * @see java.awt.print.PrinterJob#getUserName()
	 */
	@Override
	public String getUserName() {
		return "User";
	}

	/**
	 * @see java.awt.print.PrinterJob#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return false;
	}

	/**
	 * @see java.awt.print.PrinterJob#pageDialog(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat pageDialog(PageFormat pageFormat) throws HeadlessException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			return pageFormat;
		} else {
			return defaultPrinterJob.pageDialog(pageFormat);
		}
	}
	
	@Override
	public PageFormat pageDialog(PrintRequestAttributeSet attributes)
			throws HeadlessException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			return pageFormat;
		} else {
			return defaultPrinterJob.pageDialog(attributes);
		}
	}

	/**
	 * @see java.awt.print.PrinterJob#print()
	 */
	@Override
	public void print() throws PrinterException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			registerRemotePrintJob(null);
		} else {
			defaultPrinterJob.print();
		}
	}
	
	@Override
	public void print(PrintRequestAttributeSet attributes)
			throws PrinterException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			registerRemotePrintJob(attributes);
		} else {
			defaultPrinterJob.print(attributes);
		}
	}

	/**
	 * @see java.awt.print.PrinterJob#printDialog()
	 */
	@Override
	public boolean printDialog() throws HeadlessException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			return true;
		} else {
			return defaultPrinterJob.printDialog();
		}
	}

	@Override
	public boolean printDialog(PrintRequestAttributeSet attributes)
			throws HeadlessException {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			return true;
		} else {
			return defaultPrinterJob.printDialog(attributes);
		}
	}
	
	/**
	 * @see java.awt.print.PrinterJob#setCopies(int)
	 */
	@Override
	public void setCopies(int copies) {
		this.copies = copies;
		defaultPrinterJob.setCopies(copies);
	}

	/**
	 * @see java.awt.print.PrinterJob#setJobName(java.lang.String)
	 */
	@Override
	public void setJobName(String jobName) {
		this.jobName = jobName;
		defaultPrinterJob.setJobName(jobName);

	}

	/**
	 * @see java.awt.print.PrinterJob#setPageable(java.awt.print.Pageable)
	 */
	@Override
	public void setPageable(Pageable document) throws NullPointerException {
		this.document = document;
		defaultPrinterJob.setPageable(document);
	}

	/**
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable)
	 */
	@Override
	public void setPrintable(Printable painter) {
		this.painter = painter;
		defaultPrinterJob.setPrintable(painter);
	}

	/**
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable, java.awt.print.PageFormat)
	 */
	@Override
	public void setPrintable(Printable painter, PageFormat format) {
		this.painter = painter;
		this.pageFormat = format;
		defaultPrinterJob.setPrintable(painter, format);
	}

	@Override
	public void setPrintService(PrintService service) throws PrinterException {
		this.service = service;
		defaultPrinterJob.setPrintService(service);
	}


	/**
	 * @see java.awt.print.PrinterJob#validatePage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat validatePage(PageFormat pageFormat) {
		if (PrintServiceUtils.isRemotePrintService(service)) {
			return pageFormat;
		} else {
			return defaultPrinterJob.validatePage(pageFormat);
		}
	}

	
	public static void initializePrinterJobManager() {
		if (defaultPrinterJobClass == null) {
			try {
				String defaultClassName = System.getProperty("java.awt.printerjob", null);
				defaultPrinterJobClass = Class.forName(defaultClassName);
				System.setProperty("java.awt.printerjob", "net.sf.wubiq.print.jobs.PrinterJobHandler");
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}
	
	private void registerRemotePrintJob(PrintRequestAttributeSet printRequestAttributeSet) throws PrinterException {
		Doc doc = null;
		if (document != null) {
			doc = new SimpleDoc(document, DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
		} else {
			if (painter != null) {
				doc = new SimpleDoc(painter, DocFlavor.SERVICE_FORMATTED.PRINTABLE, new HashDocAttributeSet());
			}
		}
		if (doc != null) {
			DocPrintJob printJob = service.createPrintJob();
			((RemotePrintJob)printJob).setPageFormat(pageFormat);
			PrintRequestAttributeSet request = printRequestAttributeSet == null ? new HashPrintRequestAttributeSet() : printRequestAttributeSet;
			try {
				printJob.print(doc, request);
			} catch (PrintException e) {
				throw new PrinterException(e.getMessage());
			}
		}
	}
}
