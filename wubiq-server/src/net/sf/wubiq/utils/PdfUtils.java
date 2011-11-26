/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
	 * Converts a pdf input stream into a bitmap file
	 * @param pdf Pdf file
	 * @return File object or null if something failed.
	 */
	public File convertPdfToPng(InputStream pdf, boolean colorCapable) {
		File returnValue = null;
		PDDocument document = null;
		try {
			PDFImageWriter imageWriter = new PDFImageWriter();
			document = PDDocument.load(pdf);
			String imageFormat = "png";
	        int startPage = 1;
	        int endPage = Integer.MAX_VALUE;
	        File tempFile = File.createTempFile("temp", "pdf.png");
	        String outputPrefix = tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf('.'));
	        int imageType = colorCapable ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY;
	        int resolution = Toolkit.getDefaultToolkit().getScreenResolution() * 2;
	        if (imageWriter.writeImage(document, imageFormat, "", startPage, endPage, outputPrefix, imageType, resolution)) {
	        	returnValue = new File(outputPrefix + "1." + imageFormat);
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
		return returnValue;
	}
	
}
