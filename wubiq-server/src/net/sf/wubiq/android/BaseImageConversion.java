/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public abstract class BaseImageConversion {
	private static final Log LOG = LogFactory.getLog(BaseImageConversion.class);
	
	/**
	 * Escape sequence to initialize the printer.
	 * @param deviceInfo Target device
	 * @param printData Print data to be outputted.
	 */
	protected void initializePrinter(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		Object initializePrinter = new byte[]{0x1b, 0x24};

		if (deviceInfo.getHints().containsKey(MobileConversionHint.INITIALIZE_PRINTER)) {
			initializePrinter = deviceInfo.getHints().get(MobileConversionHint.INITIALIZE_PRINTER);
		}
		writeData(printData, initializePrinter);
	}
	/**
	 * Escape sequence to initialize the printer.
	 * @param deviceInfo Target device
	 * @param printData Print data to be outputted.
	 * @param width Width of the image.
	 * @param height Height of the image.
	 */
	protected void initializePrinter(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData, int width, int height) {
		Object initializePrinter = null;
		if (deviceInfo.getHints().containsKey(MobileConversionHint.INITIALIZE_PRINTER)) {
			initializePrinter = deviceInfo.getHints().get(MobileConversionHint.INITIALIZE_PRINTER);
		}
		if (initializePrinter != null && initializePrinter instanceof String) {
			String initialize = (String)initializePrinter;
			initialize = initialize.replaceAll("\\{width\\}", Integer.toString(width));
			initialize = initialize.replaceAll("\\{height\\}", Integer.toString(height));
			initialize = initialize.replaceAll("\\{byteWidth\\}", Integer.toString((width + 7) / 8));
			initialize = initialize.replaceAll("\\{byteHeight\\}", Integer.toString((height + 7) / 8));
			writeData(printData, initialize);
		}
	}
	/**
	 * Escape sequence to initialize the printer.
	 * @param deviceInfo Target device
	 * @param printData Print data to be outputted.
	 */
	protected void finalizePrinter(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		Object finalizePrinter = null;

		if (deviceInfo.getHints().containsKey(MobileConversionHint.FINALIZE_PRINTER)) {
			finalizePrinter = deviceInfo.getHints().get(MobileConversionHint.FINALIZE_PRINTER);
		}
		writeData(printData, finalizePrinter);
	}

	/**
	 * Print code for printing previously defined bitmap.
	 * @param deviceInfo Target device.
	 * @param printData Print data to be output.
	 */
	protected void printDefinedBitmap(MobileDeviceInfo deviceInfo, ByteArrayOutputStream printData) {
		Object printDefinedBitmap = new byte[]{0x1b, 0x58, 0x32, 0x18};
		if (deviceInfo.getHints().containsKey(MobileConversionHint.PRINT_DEFINED_BITMAP)) {
			printDefinedBitmap = deviceInfo.getHints().get(MobileConversionHint.PRINT_DEFINED_BITMAP);
		}
		writeData(printData, printDefinedBitmap);
	}
	
	/**
	 * Appends data to the outputStream.
	 * @param printData Data to be outputted
	 * @param data data to be appended
	 */
	protected void writeData(ByteArrayOutputStream printData, Object data) {
		if (data != null) {
			if (data instanceof byte[]) {
				writeData(printData, (byte[])data);
			} else if (data instanceof String) {
				writeData(printData, (String)data);
			}
		}
	}
	
	/**
	 * Appends data to the outputStream.
	 * @param printData Data to be outputted
	 * @param data data to be appended
	 */
	private void writeData(ByteArrayOutputStream printData, byte[] data) {
		try {
			if (data.length > 0) {
				printData.write(data);
				printData.flush();
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Appends data to the outputStream.
	 * @param printData Data to be outputted
	 * @param data data to be appended
	 */
	private void writeData(ByteArrayOutputStream printData, String data) {
		try {
			for (char d : data.toCharArray()) {
				printData.write(new byte[]{(byte)d});
			}
			printData.flush();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	/**
	 * Calculates the address of the pixels (vertical wise)
	 * @param x Horizontal position of the pixel
	 * @param y Vertical position of the pixel
	 * @param width width of the image
	 * @return Calculated address
	 */
	protected int pixelIndex(int x, int y, int width)
	{
		return (y * width) + x;
	}
	
	/** 
	 * 
	 * @param color Integer value represeting an RGB color
	 * @return Brightness as mean of red green and blue color.
	 */
	protected int pixelBrightness(int color) {
		int red = new Color(color).getRed();
		int green = new Color(color).getGreen();
		int blue = new Color(color).getBlue();
		return (red + green + blue) / 3;
	}
	

}
