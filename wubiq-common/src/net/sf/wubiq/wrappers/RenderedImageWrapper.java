/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

/**
 * @author Federico Alcantara
 *
 */
public class RenderedImageWrapper extends ImageWrapper {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default constructor.
	 */
	public RenderedImageWrapper() {
		super();
	}
	
	/**
	 * Prefered constructor.
	 * @param img RenderedImage to be serialized.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RenderedImageWrapper(RenderedImage img) {
		BufferedImage image = null;
		if (img instanceof BufferedImage) {
			image = (BufferedImage)img;	
		} else {
			ColorModel cm = img.getColorModel();
			int width = img.getWidth();
			int height = img.getHeight();
			WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			Hashtable properties = new Hashtable();
			String[] keys = img.getPropertyNames();
			if (keys!=null) {
				for (int i = 0; i < keys.length; i++) {
					properties.put(keys[i], img.getProperty(keys[i]));
				}
			}
			image = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
			img.copyData(raster);
		}
		serializeImage(image);
	}
	
	public RenderedImage getRenderedImage(double xScale, double yScale) {
		return (RenderedImage)((BufferedImage)super.getImage(xScale, yScale));
	}
	
}
