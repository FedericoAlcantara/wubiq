/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.AWTException;
import java.awt.BufferCapabilities;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.ImageCapabilities;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

/**
 * @author Federico Alcantara
 *
 */
public class DefaultDeviceConfiguration extends GraphicsConfiguration {
	private transient GraphicsConfiguration conf;
	private transient DefaultGraphicsDevice device;
	
	public DefaultDeviceConfiguration(GraphicsConfiguration conf) {
		this.conf = conf;
	}
	
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		return conf.createCompatibleImage(width, height);
	}
	
	@Override
	public BufferedImage createCompatibleImage(int width, int height,
			int transparency) {
		return conf.createCompatibleImage(width, height, transparency);
	}
	
	@Override
	public VolatileImage createCompatibleVolatileImage(int width, int height) {
		return conf.createCompatibleVolatileImage(width, height);
	}	

	@Override
	public VolatileImage createCompatibleVolatileImage(int width, int height,
			ImageCapabilities caps) throws AWTException {
		return conf.createCompatibleVolatileImage(width, height, caps);
	}
	
	@Override
	public VolatileImage createCompatibleVolatileImage(int width, int height,
			ImageCapabilities caps, int transparency) throws AWTException {
		return conf.createCompatibleVolatileImage(width, height, caps, transparency);
	}
	
	@Override
	public VolatileImage createCompatibleVolatileImage(int width, int height,
			int transparency) {
		return conf.createCompatibleVolatileImage(width, height, transparency);
	}
	
	@Override
	public Rectangle getBounds() {
		return conf.getBounds();
	}
	
	@Override
	public BufferCapabilities getBufferCapabilities() {
		return conf.getBufferCapabilities();
	}
	
	@Override
	public ColorModel getColorModel() {
		return conf.getColorModel();
	}
	
	@Override
	public ColorModel getColorModel(int transparency) {
		return conf.getColorModel(transparency);
	}
	
	@Override
	public AffineTransform getDefaultTransform() {
		return conf.getDefaultTransform();
	}
	
	@Override
	public GraphicsDevice getDevice() {
		if (device == null) {
			device = new DefaultGraphicsDevice(conf.getDevice());
		}
		return device;
	}
	
	@Override
	public ImageCapabilities getImageCapabilities() {
		return conf.getImageCapabilities();
	}

	@Override
	public AffineTransform getNormalizingTransform() {
		return conf.getNormalizingTransform();
	}

}

