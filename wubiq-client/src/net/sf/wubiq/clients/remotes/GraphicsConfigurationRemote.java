/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.utils.DirectConnectUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Creates a remote handler for GraphicsConfiguration.
 * @author Federico Alcantara
 *
 */
public class GraphicsConfigurationRemote extends GraphicsConfiguration 
		implements IProxyClient, IProxyMaster {
	
	public static final String[] FILTERED_METHODS = new String[]{
	};
		
	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		throw new NotImplementedException();
	}
	
	/**
	 * Creates a compatible image and converts it to hex.
	 * @param width Width of the image.
	 * @param height Height of the image.
	 * @return String representing an image.
	 */
	public String createCompatibleImageRemote(int width, int height) {
		BufferedImage image = graphicsConfiguration().createCompatibleImage(width, height);
		return DirectConnectUtils.INSTANCE.serializeImage(image);
	}

	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height, int transparency) {
		throw new NotImplementedException();
	}

	/**
	 * Creates a compatible image and converts it to hex.
	 * @param width Width of the image.
	 * @param height Height of the image.
	 * @return String representing an image.
	 */
	public String createCompatibleImageRemote(int width, int height, int transparency) {
		BufferedImage image = graphicsConfiguration().createCompatibleImage(width, height, transparency);
		return DirectConnectUtils.INSTANCE.serializeImage(image);
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return graphicsConfiguration().getBounds();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel()
	 */
	@Override
	public ColorModel getColorModel() {
		return graphicsConfiguration().getColorModel();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel(int)
	 */
	@Override
	public ColorModel getColorModel(int transparency) {
		return graphicsConfiguration().getColorModel();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDefaultTransform()
	 */
	@Override
	public AffineTransform getDefaultTransform() {
		return graphicsConfiguration().getDefaultTransform();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDevice()
	 */
	@Override
	public GraphicsDevice getDevice() {
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getNormalizingTransform()
	 */
	@Override
	public AffineTransform getNormalizingTransform() {
		return graphicsConfiguration().getNormalizingTransform();
	}

	private GraphicsConfiguration graphicsConfiguration() {
		return (GraphicsConfiguration) decoratedObject();
	}
	
	/* ***************************
	 * Proxied methods
	 * ***************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyClient#manager()
	 */
	public DirectPrintManager manager() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}


}
