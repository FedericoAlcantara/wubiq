/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;

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
	public List<File> convertPdfToPng(InputStream pdf, int resolution) {
		return convertPdfToImg(pdf, resolution, "png");
	}

	/**
	 * Converts a pdf input stream into a bitmap file of type png.
	 * @param pdf Pdf file.
	 * @return File object or null if something failed.
	 */
	public List<File> convertPdfToJpg(InputStream pdf, int resolution) {
		return convertPdfToImg(pdf, resolution, "jpg");
	}
	
	/**
	 * Converts a pdf input stream into a bitmap file of given type
	 * @param pdf Pdf file.
	 * @return File object or null if something failed.
	 */
	public List<File> convertPdfToImg(InputStream pdf, int resolution, String suffix) {
		List<File> returnValue = new ArrayList<File>();
		PDDocument document = null;
		try {
			PDFImageWriter imageWriter = new PDFImageWriter();
			document = PDDocument.load(pdf);
			String imageFormat = suffix;
	        int startPage = 1;
	        int endPage = Integer.MAX_VALUE;
	        File tempFile = File.createTempFile("temp", "pdf." + suffix);
	        String outputPrefix = tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf('.'));
	        int imageType = BufferedImage.TYPE_INT_RGB;
	        if (imageWriter.writeImage(document, imageFormat, "", startPage, endPage, outputPrefix, imageType, resolution)) {
	        	for (int index = startPage; index < endPage; index++) {
	        		File file = new File(outputPrefix + index + "." + imageFormat);
	        		if (file.exists()) {
	        			returnValue.add(file);
	        		} else {
	        			break;
	        		}
	        	}
	        }
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (document != null) {
				try {
					document.close();
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
		}
		LOG.info("Converted:" + returnValue);
		return returnValue;
	}
	
}
