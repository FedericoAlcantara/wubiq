/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.awt.HeadlessException;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;

import net.sf.wubiq.utils.PrintServiceUtils;

/**
 * Poses a printer job that returns a proper page format
 * based on the attributes.
 * @author Federico Alcantara
 *
 */
public class PdfPrinterJob extends PrinterJob {
	PrintRequestAttributeSet attributes;
	PrinterJob printerJob;
	
	public PdfPrinterJob(PrintRequestAttributeSet attributes) {
		this.attributes = attributes;
		printerJob = PrinterJob.getPrinterJob();
	}
	
	@Override
	public void cancel() {
		printerJob.cancel();
	}

	@Override
	public PageFormat defaultPage() {
		PageFormat pageFormat =  printerJob.getPageFormat(attributes); // This is where the magic is done
		MediaPrintableArea printableArea = 
				(MediaPrintableArea)PrintServiceUtils.findCategoryAttribute(attributes, MediaPrintableArea.class);
		Paper paper = null;
		if (printableArea == null) { // Make sure printing starts at 0
			paper = pageFormat.getPaper();
			paper.setImageableArea(0, 0,
					paper.getImageableX() + paper.getImageableWidth(),
					paper.getImageableY() + paper.getImageableHeight());
			
		} else {
			paper = new Paper();
			paper.setSize(printableArea.getWidth(MediaPrintableArea.INCH) * 72,
					printableArea.getHeight(MediaPrintableArea.INCH) * 72);
			paper.setImageableArea(printableArea.getX(MediaPrintableArea.INCH) * 72,
					printableArea.getY(MediaPrintableArea.INCH) * 72,
					printableArea.getWidth(MediaPrintableArea.INCH) * 72,
					printableArea.getHeight(MediaPrintableArea.INCH) * 72);
		}
		pageFormat.setPaper(paper);
		return pageFormat;
	}
	
	@Override
	public PageFormat defaultPage(PageFormat page) {
		return printerJob.defaultPage(page);
	}

	@Override
	public int getCopies() {
		return printerJob.getCopies();
	}

	@Override
	public String getJobName() {
		return printerJob.getJobName();
	}

	@Override
	public String getUserName() {
		return printerJob.getUserName();
	}

	@Override
	public boolean isCancelled() {
		return printerJob.isCancelled();
	}

	@Override
	public PageFormat pageDialog(PageFormat page) throws HeadlessException {
		return printerJob.pageDialog(page);
	}

	@Override
	public void print() throws PrinterException {
		printerJob.print();
	}

	@Override
	public boolean printDialog() throws HeadlessException {
		return printerJob.printDialog();
	}

	@Override
	public void setCopies(int copies) {
		printerJob.setCopies(copies);
	}

	@Override
	public void setJobName(String jobName) {
		printerJob.setJobName(jobName);
	}

	@Override
	public void setPageable(Pageable document) throws NullPointerException {
		printerJob.setPageable(document);
	}

	@Override
	public void setPrintable(Printable painter) {
		printerJob.setPrintable(painter);
	}

	@Override
	public void setPrintable(Printable painter, PageFormat format) {
		printerJob.setPrintable(painter, format);
	}

	@Override
	public PageFormat validatePage(PageFormat page) {
		return printerJob.validatePage(page);
	}

	@Override
	public void setPrintService(PrintService service) throws PrinterException {
		printerJob.setPrintService(service);
	}
	
	@Override
	public PageFormat getPageFormat(PrintRequestAttributeSet attributes) {
		return printerJob.getPageFormat(attributes);
	}

}
