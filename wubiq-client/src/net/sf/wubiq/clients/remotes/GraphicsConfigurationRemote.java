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
import net.sf.wubiq.interfaces.IRemoteClient;

/**
 * Creates a remote handler for GraphicsConfiguration.
 * @author Federico Alcantara
 *
 */
public class GraphicsConfigurationRemote extends GraphicsConfiguration 
		implements IRemoteClient {
	private DirectPrintManager manager;
	private GraphicsConfiguration graphicsConfiguration;
	private UUID objectUUID;
	
	public GraphicsConfigurationRemote(DirectPrintManager manager, 
			GraphicsConfiguration graphicsConfiguration) {
		this.manager = manager;
		this.graphicsConfiguration = graphicsConfiguration;
		objectUUID = UUID.randomUUID();
		this.manager.registerObject(objectUUID, this);
	}
	
	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		BufferedImage image = graphicsConfiguration.createCompatibleImage(width, height);
		return image;
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
