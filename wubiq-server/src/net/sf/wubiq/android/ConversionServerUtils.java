/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.print.pdf.PdfImagePage;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.PdfUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imgscalr.Scalr;

/**
 * @author Federico Alcantara
 *
 */
public enum ConversionServerUtils {
	INSTANCE;
	
	private static final Log LOG = LogFactory.getLog(ConversionServerUtils.class);
	
	/*
	 * Perform the steps according to device specifications
	 */
	public InputStream convertToMobile(String deviceName, InputStream inputPdf) {
		InputStream returnValue = null;
		String[] deviceData = deviceName.split(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
		Object convertedValue = inputPdf;
		MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(deviceData[2].replaceAll("_", " ")); 
		for (MobileServerConversionStep step : deviceInfo.getServerSteps()) {
			if (step.equals(MobileServerConversionStep.PDF_TO_IMAGE)) {
				convertedValue = pdfToImg(deviceInfo, (InputStream)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.RESIZE)) {
				convertedValue = adjustToSize(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_ESCAPED)) {
				convertedValue = ImageToEscaped.convert(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_BIT_LINE)) {
				convertedValue = ImageToBitLine.convert(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_BITMAP)) {
				convertedValue = ImageToBitmap.convert(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_HEX)) {
				convertedValue = ImageToHex.convert(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_PCX)) {
				convertedValue = ImageToPcx.convert(deviceInfo, (BufferedImage)convertedValue);
			}
		}
		if (convertedValue instanceof InputStream) {
			returnValue = (InputStream)convertedValue;
		} else if (convertedValue instanceof BufferedImage) {
			BufferedImage image = (BufferedImage) convertedValue;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try {
				ImageIO.write(image, "png", outputStream);
				outputStream.flush();
				returnValue = new ByteArrayInputStream(outputStream.toByteArray());
			} catch (IOException e) {
				LOG.error(ExceptionUtils.getMessage(e), e);
			}
		}
		return returnValue;
	}
	
	/**
	 * @param object Will receive a pdf
	 * @return a stream representing a bitmap
	 */
	protected BufferedImage pdfToImg(MobileDeviceInfo deviceInfo, InputStream pdf) {
		IOUtils.INSTANCE.resetInputStream(pdf);
		List<PdfImagePage> bitmaps = PdfUtils.INSTANCE.convertPdfToPng(pdf, deviceInfo.getResolutionDpi());
		BufferedImage returnValue = null;
		try {
			if (bitmaps.size() >= 1) {
				returnValue = removeAlphaChannel(ImageIO.read(bitmaps.get(0).getImageFile()));
			}
		} catch (IOException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
		}
		return returnValue;
	}
	
	/**
	 * Resize the image so that it can be printed in the expected size.
	 * @param deviceInfo Target device
	 * @param img Image to be resized (if needed)
	 * @return Resized image.
	 */
	protected BufferedImage adjustToSize(MobileDeviceInfo deviceInfo,
			BufferedImage img) {
		BufferedImage returnValue = trimmed(deviceInfo, img);
		int maxWidth = deviceInfo.getMaxHorPixels();
		int width = returnValue.getWidth();
		// Let's resize it
		if (width > maxWidth) {
			double rate = new Double(width) / new Double(maxWidth);
			int maxHeight = new Double(returnValue.getHeight() / rate).intValue();
			returnValue = Scalr.resize(returnValue, maxWidth, maxHeight);
		}
		return returnValue;
	}

	/**
	 * Returns a trimmed at the bottom image.
	 * @param img Image to be trimmed.
	 * @return Trimmed image.
	 */
	protected BufferedImage trimmed(MobileDeviceInfo deviceInfo, BufferedImage img) {
		int trimmedHeight = getTrimmedHeight(deviceInfo, img);
		int trimmedWidth = getTrimmedWidth(deviceInfo, img, trimmedHeight);
		BufferedImage returnValue = img;
		int actualTrimmedWidth = trimmedWidth < img.getWidth() ? trimmedWidth : img.getWidth();
		int actualTrimmedHeight = trimmedHeight < img.getHeight() ? trimmedHeight : img.getHeight();
		if (actualTrimmedWidth != img.getWidth() ||
				actualTrimmedHeight != img.getHeight()) {
			returnValue = Scalr.crop(img, trimmedWidth, trimmedHeight);
		}

		return returnValue;
	}
	
	/**
	 * Calculates the trimmed height of the image.
	 * @param img Image to be calculated.
	 * @return Actual minimum height.
	 */
	private int getTrimmedHeight(MobileDeviceInfo deviceInfo, BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int trimmedHeight = 0;
        int bottomSpace = 0;
        if (deviceInfo.getHints().containsKey(MobileConversionHint.BOTTOM_SPACE)) {
        	try {
        		bottomSpace = (Integer) deviceInfo.getHints().get(MobileConversionHint.BOTTOM_SPACE);
        	} catch (Exception e) {
        		LOG.error(ExceptionUtils.getMessage(e), e);
        	}
        }
        outer:
        for (int j = height - 1; j >= 0; j--) {
            for (int i = 0; i < width; i++) {
            	if ((img.getRGB(i, j) >> 24) == 0x00) { // Transparent
            		continue;
            	}
                if(img.getRGB(i, j) != Color.WHITE.getRGB() &&
                        j > trimmedHeight) {
                    trimmedHeight = j + 1;
                    break outer;
                }
            }
        }

        return trimmedHeight + bottomSpace;
    }

	/**
	 * Calculates the trimmed width of the image.
	 * @param img Image to be calculated.
	 * @return Actual minimum width.
	 */
	private int getTrimmedWidth(MobileDeviceInfo deviceInfo, BufferedImage img, int actualHeight) {
        int width = img.getWidth();
        int trimmedHeight = img.getHeight() > actualHeight ? actualHeight : img.getHeight();
        int trimmedWidth = 0;
        int rightSpace = 0;
        if (deviceInfo.getHints().containsKey(MobileConversionHint.RIGHT_SPACE)) {
        	try {
        		rightSpace = (Integer) deviceInfo.getHints().get(MobileConversionHint.RIGHT_SPACE);
        	} catch (Exception e) {
        		LOG.error(ExceptionUtils.getMessage(e), e);
        	}
        }
        outer:
        for (int j = width - 1; j >= 0; j--) {
            for (int i = 0; i < trimmedHeight; i++) {
            	if ((img.getRGB(j, i) >> 24) == 0x00) { // Transparent
            		continue;
            	}
                if (img.getRGB(j, i) != Color.WHITE.getRGB() &&
                        j > trimmedWidth) {
                    trimmedWidth = j + 1;
                    break outer;
                }
            }
        }

        return trimmedWidth + rightSpace;
    }

	/**
	 * Removes the alpha channel of the image.
	 * @param img Image to remove the alpha channel from.
	 * @return Buffered image without alpha channel.
	 */
	private BufferedImage removeAlphaChannel(BufferedImage img) {
		BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = copy.createGraphics();
		g2d.setColor(Color.WHITE); // Or what ever fill color you want...
		g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return copy;
	}
}
