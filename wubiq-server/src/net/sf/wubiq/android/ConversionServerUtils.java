/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.utils.PdfUtils;

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
	public InputStream convertToMobile(String deviceName, InputStream pdf) {
		InputStream returnValue = null;
		String[] deviceData = deviceName.split(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
		Object convertedValue = pdf;
		MobileDeviceInfo deviceInfo = MobileDevices.INSTANCE.getDevices().get(deviceData[2]); 
		for (MobileServerConversionStep step : deviceInfo.getServerSteps()) {
			if (step.equals(MobileServerConversionStep.PDF_TO_IMAGE)) {
				convertedValue = pdfToImg(deviceInfo, (InputStream)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.RESIZE)) {
				convertedValue = adjustToSize(deviceInfo, (BufferedImage)convertedValue);
			}
			if (step.equals(MobileServerConversionStep.IMAGE_TO_ESCAPED)) {
				convertedValue = imageToEscaped(deviceInfo, (BufferedImage)convertedValue);
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
				LOG.error(e.getMessage(), e);
			}
		}
		return returnValue;
	}
	
	/**
	 * @param object Will receive a pdf
	 * @return a stream representing a bitmap
	 */
	protected BufferedImage pdfToImg(MobileDeviceInfo deviceInfo, InputStream pdf) {
		File bitmap = PdfUtils.INSTANCE.convertPdfToPng(pdf, deviceInfo.isColorCapable());
		BufferedImage returnValue = null;
		try {
			returnValue = ImageIO.read(bitmap);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
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
		BufferedImage returnValue = img;
		int maxWidth = deviceInfo.getMaxHorPixels();
		int width = img.getWidth();
		// Let's resize it
		if (width > maxWidth) {
			returnValue = Scalr.resize(img, maxWidth);
		}
		return returnValue;
	}

	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	protected InputStream imageToEscaped(MobileDeviceInfo deviceInfo, BufferedImage img) {
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
				
		int w = width / 8;
		if((width % 8) != 0)
			w++;
		int mWidth = w * 8;
		
		int byteWidth = mWidth / 8;

		initializePrinter(deviceInfo, printData);

		int totalRowCount = 0;
		
		for(int z=0; z< (height - (height % 24))/24; z++)
		{
			startBitmapDefinition(deviceInfo, printData, byteWidth);
			totalRowCount = 
					defineBitmapData(deviceInfo, printData, byteWidth, width, mWidth, height, pixels, totalRowCount);
			printDefinedBitmap(deviceInfo, printData);
		}
			
		if(height % 24 > 0)
		{
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
	 * Calculates the address of the pixels (vertical wise)
	 * @param x Horizontal position of the pixel
	 * @param y Vertical position of the pixel
	 * @param width width of the image
	 * @return Calculated address
	 */
	private int PixelIndex(int x, int y, int width)
	{
		return (y * width) + x;
	}
	
	/** 
	 * 
	 * @param color Integer value represeting an RGB color
	 * @return Brightness as mean of red green and blue color.
	 */
	private int pixelBrightness(int color) {
		int red = new Color(color).getRed();
		int green = new Color(color).getGreen();
		int blue = new Color(color).getBlue();
		return (red + green + blue) / 3;
	}
	
	/**
	 * Escape sequence to initialize the printer.
	 * @param deviceInfo Target device
	 * @param printData Print data to be outputted.
	 */
	private void initializePrinter(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		byte[] initializePrinter = {0x1b, 0x40};

		if (deviceInfo.getHints().containsKey(MobileConversionHint.INITIALIZE_PRINTER)) {
			initializePrinter = (byte[]) deviceInfo.getHints().get(MobileConversionHint.INITIALIZE_PRINTER);
		}
		writeData(printData, initializePrinter);
		
		printDefinedBitmap(deviceInfo, printData);
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
						pixel = pixels[PixelIndex(xbit + x * 8, totalRowCount, width)];
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
	 * Print code for printing previously defined bitmap.
	 * @param deviceInfo Target device.
	 * @param printData Print data to be output.
	 */
	private void printDefinedBitmap(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
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
			LOG.error(e.getMessage(), e);
		}
	}
	
}
