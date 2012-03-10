/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.image.BufferedImage;
import java.awt.print.Pageable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.wubiq.print.pdf.PdfImagePage;

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
	 */
	public Pageable pdfToPageable(InputStream printDocument) {
		PDDocument document = null;
		try {
			document = PDDocument.load(printDocument);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return (Pageable)document;
	}
	
	public void closePageable(Pageable pageable) {
		if (pageable != null && pageable instanceof PDDocument) {
			try {
				((PDDocument)pageable).close();
			} catch (IOException e) {
				LOG.error(e.getMessage());
			}
		}
	}
}
