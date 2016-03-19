/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Converts an image into a escaped sequence.
 * Compatible with Star Micronics, Porti-S and most generic printers.
 * @author Federico Alcantara
 *
 */
public class ImageToEscaped extends BaseImageConversion {
	private static final Log LOG = LogFactory.getLog(ImageToEscaped.class);
	private static ImageToEscaped instance;
	
	private ImageToEscaped(){
		
	}
	
	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	public static InputStream convert(MobileDeviceInfo deviceInfo, BufferedImage img) {
		if (instance == null) {
			instance = new ImageToEscaped();
		}
		return instance.convertImage(deviceInfo, img);
	}
		
	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	private InputStream convertImage(MobileDeviceInfo deviceInfo, BufferedImage img) {
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
			LOG.error(ExceptionUtils.getMessage(e), e);
		}
				
		int w = width / 8;
		if((width % 8) != 0)
			w++;
		int mWidth = w * 8;
		
		int byteWidth = mWidth / 8;

		initializePrinter(deviceInfo, printData);

		int totalRowCount = 0;
		
		for (int z=0; z< (height - (height % 24))/24; z++) {
			startBitmapDefinition(deviceInfo, printData, byteWidth);
			totalRowCount = 
					defineBitmapData(deviceInfo, printData, byteWidth, width, mWidth, height, pixels, totalRowCount);
			printDefinedBitmap(deviceInfo, printData);
		}
			
		if (height % 24 > 0) {
			startBitmapDefinition(deviceInfo, printData, byteWidth);

			totalRowCount = 
					defineBitmapData(deviceInfo, printData, byteWidth, width, mWidth, height, pixels, totalRowCount);
			
			printDefinedBitmap(deviceInfo, printData);
		}
		
		byte lineFeed[] = {0x1b, 0x4A, 0x20};
		if (deviceInfo.getHints().containsKey(MobileConversionHint.LINE_FEED)) {
			lineFeed = (byte[]) deviceInfo.getHints().get(MobileConversionHint.LINE_FEED);
		}
		writeData(printData, lineFeed);
		writeData(printData, lineFeed);
		writeData(printData, lineFeed);
		return new ByteArrayInputStream(printData.toByteArray());
	}
	
	
	/**
	 * Starts the bitmap mode for the printer.
	 * @param deviceInfo Target device.
	 * @param printData Print data to be outputted.
	 * @param byteWidth Width (pixels) of each byte.
	 */
	private void startBitmapDefinition(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, int byteWidth) {
		byte[] defineBitmap = { 0x1b, 0x58, 0x34, 0, 24 };
		if (deviceInfo.getHints().containsKey(MobileConversionHint.DEFINE_BITMAP_WIDTH_ON_FOURTH)) {
			defineBitmap = (byte[]) deviceInfo.getHints().get(MobileConversionHint.DEFINE_BITMAP_WIDTH_ON_FOURTH);
		}
		defineBitmap[3] = (byte)byteWidth;
		writeData(printData, defineBitmap);
	}

	/**
	 * Read image bytes and creates the escape sequence to print it.
	 * @param deviceInfo Target device.
	 * @param printData Data to be outputted.
	 * @param byteWidth Length of the byte
	 * @param width Width of the image
	 * @param mWidth width of the image raster
	 * @param height Image height
	 * @param pixels Image as pixel data
	 * @param totalRowCount processed row count.
	 * @return Total pixel rows processed.
	 */
	private int defineBitmapData(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData,
			int byteWidth, int width, int mWidth,
			int height,
			int[] pixels,
			int totalRowCount){

		int idx = 0;
		byte[] data = new byte[byteWidth * 24];
		for(int y = 0; y< 24; y++)
		{
			for(int x = 0; x<byteWidth; x++)
			{
				int bits = 8;
				if((x == (byteWidth - 1)) && (width < mWidth))
					bits = 8 - (mWidth - width);
				
				for(int xbit = 0; xbit<bits; xbit++)
				{
					int pixel;
					if(totalRowCount < height)
					{
						pixel = pixels[pixelIndex(xbit + x * 8, totalRowCount, width)];
					}
					else 
					{
						pixel = Color.WHITE.getRGB();
					}

					if(pixelBrightness(pixel) < 127)
						data[idx] = (byte)(data[idx] | (0x01 << (7 - xbit)));
				}
				idx++;
			}
			totalRowCount++;
		}
		writeData(printData, data);
		return totalRowCount;
	}
	
	/**
	 * Escape sequence to initialize the printer.
	 * @param deviceInfo Target device
	 * @param printData Print data to be outputted.
	 */
	@Override
	protected void initializePrinter(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		byte[] initializePrinter = {0x1b, 0x40};

		if (deviceInfo.getHints().containsKey(MobileConversionHint.INITIALIZE_PRINTER)) {
			initializePrinter = (byte[]) deviceInfo.getHints().get(MobileConversionHint.INITIALIZE_PRINTER);
		}
		writeData(printData, initializePrinter);
		
		printDefinedBitmap(deviceInfo, printData);
	}
	
	/**
	 * Print code for printing previously defined bitmap.
	 * @param deviceInfo Target device.
	 * @param printData Print data to be output.
	 */
	@Override
	protected void printDefinedBitmap(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		byte[] printDefinedBitmap = {0x1b, 0x58, 0x32, 0x18};
		if (deviceInfo.getHints().containsKey(MobileConversionHint.PRINT_DEFINED_BITMAP)) {
			printDefinedBitmap = (byte[]) deviceInfo.getHints().get(MobileConversionHint.PRINT_DEFINED_BITMAP);
		}
		writeData(printData, printDefinedBitmap);
	}
	
	/**
	 * Appends data to the outputStream.
	 * @param printData Data to be outputted
	 * @param data data to be appended
	 */
	private void writeData(ByteArrayOutputStream printData, byte[] data) {
		try {
			printData.write(data);
			printData.flush();
		} catch (IOException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
		}
	}
}
