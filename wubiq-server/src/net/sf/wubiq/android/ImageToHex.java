/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Federico Alcantara
 *
 */
public class ImageToHex extends ImageToBitLine {
	private static ImageToHex instance;

	/**
	 * Convert to hex image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	public static InputStream convert(MobileDeviceInfo deviceInfo, BufferedImage img) {
		if (instance == null) {
			instance = new ImageToHex();
		}
		return instance.convertImage(deviceInfo, img);
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
		initializePrinter(deviceInfo, printData, width, height);
		int cutWidth = cutWidth(deviceInfo, width);
		int dataWidth = cutWidth;
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
				int pixel = 255;
				if (ix < cutWidth) {
					pixel = pixels[pixelIndex(ix, iy, width)];
				}
				if (pixelBrightness(pixel) < 127) {
					byteBuffer += Math.pow(2, (7 - byteCount));
				}
				if (byteCount == 7) {
					writeData(printData, String.format("%02X", byteBuffer));
					byteBuffer = 0;
					byteCount = 0;
				} else {
					byteCount++;
				}
			}
			writeData(printData, "\r\n");
		}
		finalizePrinter(deviceInfo, printData);
	}

	
	/**
	 * Let's convert them to hex.
	 */
	@Override
	protected void formatData(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData,
			ByteArrayOutputStream dataBlock) {
		StringBuffer hex = new StringBuffer("");
		for (byte byteData : dataBlock.toByteArray()) {
			hex.append(String.format("%02X", byteData));
		}
		writeData(printData, hex.toString());
	}
}
