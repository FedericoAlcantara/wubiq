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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;

import net.sf.wubiq.utils.PageableUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableWrapper;

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
				if (defaultClassName.equals("net.sf.wubiq.print.jobs.PrinterJobHandler")) {
					defaultClassName = System.getProperty("net.sf.wubiq.default.printerjob", null);
				} else {
					System.setProperty("net.sf.wubiq.default.printerjob", defaultClassName);
					System.setProperty("java.awt.printerjob", "net.sf.wubiq.print.jobs.PrinterJobHandler");
				}
				defaultPrinterJobClass = Class.forName(defaultClassName);
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
			if (pageFormat != null) {
				((RemotePrintJob)printJob).setPageFormat(pageFormat);
			}
			PrintRequestAttributeSet request = printRequestAttributeSet == null ? new HashPrintRequestAttributeSet() : printRequestAttributeSet;
			try {
				printJob.print(doc, request);
			} catch (PrintException e) {
				throw new PrinterException(e.getMessage());
			}
		}
	}
	
	private void printLocalPrintJob(PrintRequestAttributeSet printRequestAttributeSet) throws PrinterException {
		PrintRequestAttributeSet request = printRequestAttributeSet == null ? new HashPrintRequestAttributeSet() : printRequestAttributeSet;
		request.add(new JobName("WubiqLocal", Locale.getDefault()));
		Object printData = document != null ? document : painter;
		if (printData != null) {
			DocFlavor docFlavor = document != null ? DocFlavor.SERVICE_FORMATTED.PAGEABLE : DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			try {
				InputStream data = PageableUtils.INSTANCE.getStreamForBytes(printData, docFlavor, pageFormat);
				ObjectInputStream input = new ObjectInputStream(data);
				if (document != null) {
					PageableWrapper pageable = (PageableWrapper)input.readObject();
					Printable printable = document.getPrintable(0);
					PageFormat pageFormat = document.getPageFormat(0);
					pageable.setNumberOfPages(1);
					pageable.addPrintable(new PrintableWrapper(printable));
					pageable.addPageFormat(new PageFormatWrapper(pageFormat));
					defaultPrinterJob.setPageable(pageable);
				} else {
					PrintableWrapper printable = (PrintableWrapper)input.readObject();
					defaultPrinterJob.setPrintable(printable);
				}
				defaultPrinterJob.setCopies(1);
				defaultPrinterJob.print(request);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				throw new PrinterException(e.getMessage());
			}
		}
	}
}
