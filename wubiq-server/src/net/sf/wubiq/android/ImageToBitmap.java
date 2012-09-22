/**
 * 
 */
package net.sf.wubiq.android;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generates a bit line stream output. These are streams of 8-bit elements.
 * Compatible con Datamax O'Neil apex printers.
 * @author Federico Alcantara
 *
 */
public class ImageToBitmap extends BaseImageConversion {
	public static Log LOG = LogFactory.getLog(ImageToBitmap.class);
	private static ImageToBitmap instance;
	
	protected ImageToBitmap(){
	}
	
	/**
	 * Escape the image so that can be printed.
	 * @param deviceInfo Target device.
	 * @param img Image to be printed.
	 * @return Byte array containing the formatted bitmap.
	 */
	public static InputStream convert(MobileDeviceInfo deviceInfo, BufferedImage img) {
		if (instance == null) {
			instance = new ImageToBitmap();
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
		BufferedImage bw = new BufferedImage(img.getWidth(null), 
				img.getHeight(null), BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d = (Graphics2D) bw.getGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		try {
			ImageIO.write(bw, "png", printData);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return new ByteArrayInputStream(printData.toByteArray());
	}
	
}
