/**
 * 
 */
package net.sf.wubiq.print.pdf;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

import javax.print.PrintException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageable;

/**
 * Holds a converted Pdf document to pageable.
 * @author Federico Alcantara
 *
 */
public class PdfPageable implements Pageable {
	private static final Log LOG = LogFactory.getLog(PdfPageable.class);
	
	PDDocument document;
	PDPageable pageable;
	PrinterJob printerJob;
	
	public PdfPageable() {
		
	}
	
	/**
	 * Creates a PDFPageable using default PrinterJob.
	 * @param document Document to encapsulate.
	 * @throws PrintException Thrown if errors on the printer job.
	 */
	public PdfPageable(PDDocument document) throws PrintException {
		this.document = document;
		try {
			pageable = new PDPageable(document);
		} catch (IllegalArgumentException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new PrintException(e);
		} catch (PrinterException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new PrintException(e);
		}
	}

	/**
	 * Creates a PDFPageable using defined PrinterJob
	 * @param document Document to encapsulate.
	 * @param printerJob Current printer job.
	 * @throws PrintException
	 */
	public PdfPageable(PDDocument document, PrinterJob printerJob) throws PrintException {
		createPageable(document, printerJob);
	}
	
	/**
	 * Creates a pageable document.
	 * @param document PDF document.
	 * @param printerJob Current printer job.
	 * @throws PrintException
	 */
	private void createPageable(PDDocument document, PrinterJob printerJob) throws PrintException {
		this.document = document;
		try {
			pageable = new PDPageable(document);
			this.printerJob = printerJob;
		} catch (IllegalArgumentException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new PrintException(e);
		} catch (PrinterException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new PrintException(e);
		}
	}

	@Override
	public int getNumberOfPages() {
		return pageable.getNumberOfPages();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat pageFormat = printerJob != null ? printerJob.defaultPage() : null;
		return pageFormat;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return pageable.getPrintable(pageIndex);
	}
	
	/**
	 * Closes the underlying PDDocument
	 * @throws IOException
	 */
	public void close() throws IOException {
		document.close();
	}
}
