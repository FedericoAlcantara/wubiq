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
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.utils.DirectConnectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Creates a remote handler for GraphicsConfiguration.
 * @author Federico Alcantara
 *
 */
public class GraphicsConfigurationRemote extends GraphicsConfiguration 
		implements IProxyMaster {
	private static final Log LOG = LogFactory.getLog(GraphicsConfigurationRemote.class);
	
	private DirectPrintManager manager;
	private UUID objectUUID;
	private GraphicsConfiguration graphicsConfiguration;
	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClientSlave#initialize()
	 */
	public void initialize() {
		
	}
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClientMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return graphicsConfiguration;
	}
	
	public void setDecoratedObject(Object graphicsConfiguration) {
		this.graphicsConfiguration = (GraphicsConfiguration)graphicsConfiguration;
	}
	
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
		BufferedImage image = graphicsConfiguration.createCompatibleImage(width, height);
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
		BufferedImage image = graphicsConfiguration.createCompatibleImage(width, height, transparency);
		return DirectConnectUtils.INSTANCE.serializeImage(image);
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return graphicsConfiguration.getBounds();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel()
	 */
	@Override
	public ColorModel getColorModel() {
		return graphicsConfiguration.getColorModel();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel(int)
	 */
	@Override
	public ColorModel getColorModel(int transparency) {
		return graphicsConfiguration.getColorModel();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDefaultTransform()
	 */
	@Override
	public AffineTransform getDefaultTransform() {
		return graphicsConfiguration.getDefaultTransform();
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
		return graphicsConfiguration.getNormalizingTransform();
	}
	/**
	 * @see net.sf.wubiq.interfaces.IClientRemote#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}

}
