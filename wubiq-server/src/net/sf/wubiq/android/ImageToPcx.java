/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Converts an image into a string of hex characters.
 * @author Federico Alcantara
 *
 */
public class ImageToPcx extends ImageToBitLine {
	private static ImageToPcx instance;
	
	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	public static InputStream convert(MobileDeviceInfo deviceInfo, BufferedImage img) {
		if (instance == null) {
			instance = new ImageToPcx();
		}
		return instance.convertImage(deviceInfo, img);
	}

	/**
	 * Creates the pcx image data (B/W).
	 */
	@Override
	protected void createData(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, int width, int height, int[] pixels)  {
		int maxHeight = height;
		if (deviceInfo.getHints().containsKey(MobileConversionHint.MAX_IMAGE_HEIGHT)) {
			maxHeight = (Integer)deviceInfo.getHints().get(MobileConversionHint.MAX_IMAGE_HEIGHT);
		} 
		
		for (int startY = 0; startY < height; startY += maxHeight) {
			int printHeight = (height - startY);
			if (printHeight > maxHeight) {
				printHeight = maxHeight;
			}
			int endY = startY + printHeight;
			writeImageBlock(deviceInfo, printData, width, printHeight, pixels, startY, endY);
		}
	}
	
	/**
	 * Writes a image data block.
	 * @param deviceInfo Current mobile device.
	 * @param printData Data to be printed.
	 * @param width Width of the image block.
	 * @param height Height of the image block;
	 * @param pixels Pixels representing the image block.
	 * @param startY Top of the block within the full image.
	 * @param endY Bottom of the block within the full image.
	 */
	private void writeImageBlock(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, int width, int height, int[]pixels, int startY, int endY) {
		initializePrinter(deviceInfo, printData, width, height);
		
		ByteArrayOutputStream pcx = new ByteArrayOutputStream();
		writePcxHeader(deviceInfo, pcx, width, height);
		int bytesPerLine = (width + 7) / 8; // 8 bit boundary
		int byteBuffer = 0;
		int byteCount = 0;
		int lastByte = 0;
		int blockCount = 1;
		int dataWidth = bytesPerLine * 8;
		// First read all data and convert to bytes.
		for (int iy = startY; iy < endY; iy++) {
			for (int ix = 0; ix < dataWidth; ix++) {
				int pixel = (ix < width) ? pixels[pixelIndex(ix, iy, width)] : 255;
				if (pixelBrightness(pixel) >= 127) {
					byteBuffer += Math.pow(2, (7 - byteCount));
				}
				if (byteCount == 7) {
					if (blockCount == 63 || lastByte != byteBuffer 
							|| (iy == (height - 1) && ix == (dataWidth -1)) ) {
						writeData(pcx, new byte[]{(byte)(blockCount | 0xC0), (byte)lastByte});
						blockCount = 1;
					} else {
						blockCount++;
					}
					lastByte = byteBuffer;
					byteBuffer = 0;
					byteCount = 0;
				} else {
					byteCount++;
				}
			}
		}
		
		writeData(printData, pcx.toByteArray());
		
		finalizePrinter(deviceInfo, printData);
	}
	
	/**
	 * Creates the PCX Header.
	 * @param deviceInfo Current device.
	 * @param pcx PCX output stream.
	 * @param width Width of the image.
	 * @param height Height of the image.
	 */
	private void writePcxHeader(MobileDeviceInfo deviceInfo, ByteArrayOutputStream pcx, int width, int height) {
		int rightMargin = width - 1 ;
		int bottomMargin = height - 1;
		int bytesPerLine = (width + 7) / 8; // 8 bit boundary
		int dpi = deviceInfo.getResolutionDpi();
		writeData(pcx, new byte[]{
				0x0A, // Pcx file
				0x05, // Version 5
				0x01, // RLE encoding
				0x01, // 1 bit per pixel
				0x00, 0x00, // Left margin (x start position)
				0x00, 0x00, // Top margin (y start position)
				(byte)(rightMargin & 0xFF), (byte)((rightMargin >> 8) & 0xFF), // right margin (x end position)
				(byte)(bottomMargin & 0xFF), (byte)((bottomMargin >> 8) & 0xFF), // bottom margin (y end position)
				(byte)(dpi & 0xFF), (byte)((dpi >> 8) & 0xFF), // horizontal resolution (dpi)
				(byte)(dpi & 0xFF), (byte)((dpi >> 8) & 0xFF), // vertical resolution (dpi)
				0x0F, 0x0F, 0x0F, 0x0E, 0x0E, 0x0E, 0x0D, 0x0D, 0x0D, 0x0C, 0x0C, 0x0C,   //48-byte EGA palette info
	            0x0B, 0x0B, 0x0B, 0x0A, 0x0A, 0x0A, 0x09, 0x09, 0x09, 0x08, 0x08, 0x08,  
	            0x07, 0x07, 0x07, 0x06, 0x06, 0x06, 0x05, 0x05, 0x05, 0x04, 0x04, 0x04,  
	            0x03, 0x03, 0x03, 0x02, 0x02, 0x02, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,
	            0x00, // reserved
	            0x01, // 1 color plane
	            (byte)(bytesPerLine & 0xFF), (byte)((bytesPerLine >> 8) & 0xFF), // Bytes per scan line
	            0x01, 0x00, // 1 color (B/W)
	            0x00, 0x00, // Horizontal screen size (not used)
	            0x00, 0x00 // Vertical screen size (not used)
		});
		writeData(pcx, new byte[54]);
	}
}
