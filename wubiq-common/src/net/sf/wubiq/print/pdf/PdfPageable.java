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

import net.sf.wubiq.print.jobs.PrinterJobManager;

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
	
	/**
	 * Creates a PDFPageable using default PrinterJob.
	 * @param document Document to encapsulate.
	 * @throws PrintException Thrown if errors on the printer job.
	 */
	public PdfPageable(PDDocument document) throws PrintException {
		this.document = document;
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		if (printerJob instanceof PrinterJobManager) {
			printerJob = ((PrinterJobManager)printerJob).getDefaultPrinterJob();
		}
		createPageable(document, printerJob);
	}

	public PdfPageable(PDDocument document, PrinterJob printerJob) throws PrintException {
		createPageable(document, printerJob);
	}
	
	private void createPageable(PDDocument document, PrinterJob printerJob) throws PrintException {
		this.document = document;
		try {
			pageable = new PDPageable(document, printerJob);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		}
	}

	@Override
	public int getNumberOfPages() {
		return pageable.getNumberOfPages();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageable.getPageFormat(pageIndex);
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