/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates a bit line stream output. These are streams of 8-bit elements.
 * Compatible con Datamax O'Neil apex printers.
 * @author Federico Alcantara
 *
 */
public class ImageToBitLine extends BaseImageConversion {
	public static Log LOG = LogFactory.getLog(ImageToBitLine.class);
	private static ImageToBitLine instance;
	
	protected ImageToBitLine(){
	}
	
	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	public static InputStream convert(MobileDeviceInfo deviceInfo, BufferedImage img) {
		if (instance == null) {
			instance = new ImageToBitLine();
		}
		return instance.convertImage(deviceInfo, img);
	}

	
	/**
	 * Escape the image so that can be printed in a compressed format.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	protected InputStream convertImage(MobileDeviceInfo deviceInfo, BufferedImage img) {
		ByteArrayOutputStream printData = new ByteArrayOutputStream();
		int[] pixels = null;
		int width = img.getWidth();
		int height = img.getHeight();

		PixelGrabber grabber = new PixelGrabber(img, 0, 0, -1, -1, true);
		try {
			if (grabber.grabPixels()) {
				pixels = (int[]) grabber.getPixels();
			}
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		createData(deviceInfo, printData, width, height, pixels);
		return new ByteArrayInputStream(printData.toByteArray());
	}
	
	/**
	 * Creates the data stream for printing.
	 * @param deviceInfo Device where data is going to be printed.
	 * @param printData Data buffer.
	 * @param width Image width.
	 * @param height Image height.
	 * @param pixels Image information in pixels.
	 */
	protected void createData(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, int width, int height, int[] pixels) {
		ByteArrayOutputStream dataBlock = new ByteArrayOutputStream();
		initializePrinter(deviceInfo, printData, width, height);
		int cutWidth = cutWidth(deviceInfo, width);
		int dataWidth = cutWidth;
		int maxWidth = deviceInfo.getMaxHorPixels() / 8;
		while (dataWidth % 8 != 0) {
			dataWidth++;
		}
		dataWidth = dataWidth / 8;
		int lineWidth = dataWidth * 8;
		int byteCount = 0;
		int byteBuffer = 0;
		// First read all data and convert to bytes.
		for (int iy = 0; iy < height; iy++) {
			for (int ix = 0; ix < lineWidth; ix++) {
				int pixel = 0;
				if (ix < cutWidth) {
					pixel = pixels[pixelIndex(ix, iy, width)];
				}
				if (pixelBrightness(pixel) < 127) {
					byteBuffer += Math.pow(2, (7 - byteCount));
				}
				if (byteCount == 7) {
					writeData(dataBlock, new byte[]{(byte)byteBuffer});
					byteBuffer = 0;
					byteCount = 0;
				} else {
					byteCount++;
				}
			}
			if (dataBlock.size() < maxWidth) {
				writeData(dataBlock, new byte[maxWidth - dataBlock.size()]);
			}
			formatData(deviceInfo, printData, dataBlock);
			dataBlock = new ByteArrayOutputStream();
		}
		finalizePrinter(deviceInfo, printData);
	}
	
	/**
	 * Calculates the cut width based on 8 bit bounded line
	 * @param deviceInfo Device in perspective.
	 * @param width Original image width.
	 * @return The same width or bigger to compensate for the missing bit's.
	 */
	protected int cutWidth(MobileDeviceInfo deviceInfo, int width) {
		int cutWidth = width;
		if (cutWidth > deviceInfo.getMaxHorPixels()) {
			cutWidth = deviceInfo.getMaxHorPixels();
		}
		return cutWidth;
	}
	
	/**
	 * Formats the data in bit line.
	 * @param printData Print buffer.
	 * @param dataBlock Current processed line.
	 */
	protected void formatData(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, ByteArrayOutputStream dataBlock) {
		// now let's parse the data block
		byte[] dataInBlock = dataBlock.toByteArray();
		printDefinedBitmap(deviceInfo, printData);
		for (int index = 0; index < dataBlock.size(); index++) {
			writeData(printData, new byte[]{dataInBlock[index]});
		}
	}
	
}
