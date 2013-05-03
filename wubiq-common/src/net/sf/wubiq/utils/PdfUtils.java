/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.image.BufferedImage;
import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.print.jobs.PdfPrinterJob;
import net.sf.wubiq.print.pdf.PdfImagePage;
import net.sf.wubiq.print.pdf.PdfPageable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * @author Federico Alcantara
 *
 */
public enum PdfUtils {
	INSTANCE;
	private static final Log LOG = LogFactory.getLog(PdfUtils.class);
	
	/**
	 * Converts a pdf input stream into a bitmap file of type png.
	 * @param pdf Pdf file.
	 * @return File object or null if something failed.
	 */
	public List<PdfImagePage> convertPdfToPng(InputStream pdf, int resolution) {
		return convertPdfToImg(pdf, resolution, "png");
	}

	/**
	 * Converts a pdf input stream into a bitmap file of type png.
	 * @param pdf Pdf file.
	 * @return File object or null if something failed.
	 */
	public List<PdfImagePage> convertPdfToJpg(InputStream pdf, int resolution) {
		return convertPdfToImg(pdf, resolution, "jpg");
	}
	
	/**
	 * Converts a pdf input stream into a bitmap file of given type
	 * @param pdf Pdf file.
	 * @return File object or null if something failed.
	 */
	public List<PdfImagePage> convertPdfToImg(InputStream pdf, int resolution, String suffix) {
		List<PdfImagePage> returnValue = new ArrayList<PdfImagePage>();
		PDDocument document = null;
		File tempFile = null;
		try {
			document = PDDocument.load(pdf);
			String imageFormat = suffix;
	        tempFile = File.createTempFile("temp", "pdf-." + suffix);
	        String outputPrefix = tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf('.'));
	        int imageType = BufferedImage.TYPE_INT_RGB;
	        Pageable pageable = (Pageable)document;
	        for (int pageIndex = 0; pageIndex < pageable.getNumberOfPages(); pageIndex++) {
	        	PDPage page = (PDPage)pageable.getPrintable(pageIndex);
	        	BufferedImage image = page.convertToImage(imageType, resolution);
	        	String indexed = ("000000" + pageIndex);
	        	indexed = indexed.substring(indexed.length() - 6);
	        	File fileOutput = new File(outputPrefix + indexed + "." + imageFormat);
	        	ImageIO.write(image, imageFormat, fileOutput);
	        	float height = page.getArtBox().getHeight() / 72;
	        	float width = page.getArtBox().getWidth() / 72;
	        	PdfImagePage pdfImagePage = new PdfImagePage(pageIndex, fileOutput, height, width);
	        	returnValue.add(pdfImagePage);
	        }
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
	        if (tempFile != null) {
	        	tempFile.delete();
	        }
		}
		LOG.info("Converted:" + returnValue);
		return returnValue;
	}
	
	/**
	 * Pdf to pageable.
	 * @param printDocument Converts a pdf into a pageable.
	 * @return A pageable object. Null if an error occurs.
	 * @deprecated Use pdfToPageable(InputStream, PrinterJob) or pdfToPageable(InputStream, PrintRequestAttributeSet)
	 * as these create a properly formatted pdf. 
	 */
	@Deprecated
	public Pageable pdfToPageable(InputStream printDocument) throws PrintException {
		Pageable pageable = null;
		try {
			PDDocument document = PDDocument.load(printDocument);
			pageable = new PdfPageable(document);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		}
		return pageable;
	}
	
	/**
	 * Pdf to pageable.
	 * @param printDocument Converts a pdf into a pageable.
	 * @param printerJob Current printer job
	 * @return A pageable object. Null if an error occurs. 
	 */
	public Pageable pdfToPageable(InputStream printDocument, PrinterJob printerJob) throws PrintException {
		Pageable pageable = null;
		try {
			PDDocument document = PDDocument.load(printDocument);
			pageable = new PdfPageable(document, printerJob);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		}
		return pageable;
	}
	
	/**
	 * Pdf to pageable.
	 * @param printDocument Converts a pdf into a pageable.
	 * @return A pageable object. Null if an error occurs. 
	 */
	public Pageable pdfToPageable(InputStream printDocument, PrintService printService, PrintRequestAttributeSet attributes) throws PrintException {
		Pageable pageable = null;
		try {
			PDDocument document = PDDocument.load(printDocument);
			PrinterJob printerJob = new PdfPrinterJob(attributes);
			printerJob.setPrintService(printService);
			pageable = new PdfPageable(document, printerJob);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new PrintException(e);
		}
		return pageable;
	}
	
	/**
	 * Takes care of closing a PDDocument.
	 * @param pageable Pageable (probably a PDDocument) to be closed.
	 */
	public void closePageable(Pageable pageable) {
		if (pageable instanceof PdfPageable) {
			try {
				((PdfPageable)pageable).close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
		if (pageable instanceof PDDocument) {
			try {
				((PDDocument)pageable).close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}
}
