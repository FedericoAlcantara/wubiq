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

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

/**
 * @author Federico Alcantara
 *
 */
public class PrinterJobHandler extends PrinterJob {
	private PrinterJob printerJobManager;
	
	@SuppressWarnings("rawtypes")
	public PrinterJobHandler() {
		try {
			Class printerJobClass = Thread.currentThread().getContextClassLoader().loadClass("net.sf.wubiq.print.jobs.PrinterJobManager");
			printerJobManager = (PrinterJob) printerJobClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see java.awt.print.PrinterJob#cancel()
	 */
	@Override
	public void cancel() {
		printerJobManager.cancel();
	}

	/**
	 * @see java.awt.print.PrinterJob#defaultPage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat defaultPage(PageFormat page) {
		return printerJobManager.defaultPage(page);
	}

	@Override
	public PageFormat defaultPage() {
		return printerJobManager.defaultPage();
	}
	/**
	 * @see java.awt.print.PrinterJob#getCopies()
	 */
	@Override
	public int getCopies() {
		return printerJobManager.getCopies();
	}

	/**
	 * @see java.awt.print.PrinterJob#getJobName()
	 */
	@Override
	public String getJobName() {
		return printerJobManager.getJobName();
	}

	public PrintService getPrintService() {
		return printerJobManager.getPrintService();
	}
	
	/**
	 * @see java.awt.print.PrinterJob#getUserName()
	 */
	@Override
	public String getUserName() {
		return printerJobManager.getUserName();
	}

	/**
	 * @see java.awt.print.PrinterJob#isCancelled()
	 */
	@Override
	public boolean isCancelled() {
		return printerJobManager.isCancelled();
	}

	/**
	 * @see java.awt.print.PrinterJob#pageDialog(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat pageDialog(PageFormat page) throws HeadlessException {
		return printerJobManager.pageDialog(page);
	}

	@Override
	public PageFormat pageDialog(PrintRequestAttributeSet attributes)
			throws HeadlessException {
		return printerJobManager.pageDialog(attributes);
	}
	
	/**
	 * @see java.awt.print.PrinterJob#print()
	 */
	@Override
	public void print() throws PrinterException {
		printerJobManager.print();
	}
	
	@Override
	public void print(PrintRequestAttributeSet attributes)
			throws PrinterException {
		printerJobManager.print(attributes);
	}

	/**
	 * @see java.awt.print.PrinterJob#printDialog()
	 */
	@Override
	public boolean printDialog() throws HeadlessException {
		return printerJobManager.printDialog();
	}

	/**
	 * @see java.awt.print.PrinterJob#setCopies(int)
	 */
	@Override
	public void setCopies(int copies) {
		printerJobManager.setCopies(copies);
	}

	/**
	 * @see java.awt.print.PrinterJob#setJobName(java.lang.String)
	 */
	@Override
	public void setJobName(String jobName) {
		printerJobManager.setJobName(jobName);
	}

	/**
	 * @see java.awt.print.PrinterJob#setPageable(java.awt.print.Pageable)
	 */
	@Override
	public void setPageable(Pageable document) throws NullPointerException {
		printerJobManager.setPageable(document);
	}

	/**
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable)
	 */
	@Override
	public void setPrintable(Printable painter) {
		printerJobManager.setPrintable(painter);
	}

	/**
	 * @see java.awt.print.PrinterJob#setPrintable(java.awt.print.Printable, java.awt.print.PageFormat)
	 */
	@Override
	public void setPrintable(Printable painter, PageFormat format) {
		printerJobManager.setPrintable(painter, format);
	}

	@Override
	public void setPrintService(PrintService service) throws PrinterException {
		printerJobManager.setPrintService(service);
	}
	
	/**
	 * @see java.awt.print.PrinterJob#validatePage(java.awt.print.PageFormat)
	 */
	@Override
	public PageFormat validatePage(PageFormat page) {
		return printerJobManager.validatePage(page);
	}

}
