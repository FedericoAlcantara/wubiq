/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxy;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.proxies.ProxyAdapterSlave;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsConfigurationAdapter extends GraphicsConfiguration
		implements IAdapter, IProxy {
	
	public static final String[] FILTERED_METHODS = new String[]{
		"createCompatibleImage"
	};

	public GraphicsConfigurationAdapter() {
		initialize();
	}

	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		sendCommand("createCompatibleImageRemote",
			new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height));
		UUID remoteUUID = (UUID) queue().returnData();
		
		BufferedImageAdapter bufferedImageAdapter = (BufferedImageAdapter)
				Enhancer.create(BufferedImageAdapter.class, 
						new ProxyAdapterSlave(
								queue(),
								remoteUUID,
								BufferedImageAdapter.FILTERED_METHODS));
		return bufferedImageAdapter;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height, int transparency) {
		sendCommand("createCompatibleImageRemote",
			new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height),
			new GraphicParameter(int.class, transparency));
		
		UUID remoteUUID = (UUID) queue().returnData();
		
		BufferedImageAdapter bufferedImageAdapter = (BufferedImageAdapter)
				Enhancer.create(BufferedImageAdapter.class, 
						new ProxyAdapterSlave(
								queue(),
								remoteUUID,
								BufferedImageAdapter.FILTERED_METHODS));
		return bufferedImageAdapter;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel()
	 */
	@Override
	public ColorModel getColorModel() {
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel(int)
	 */
	@Override
	public ColorModel getColorModel(int transparency) {
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDefaultTransform()
	 */
	@Override
	public AffineTransform getDefaultTransform() {
		return null;
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
		return null;
	}

	/**
	 * Sends a command to the remote printer.
	 * @param graphicCommand Command to send. Must never be null.
	 */
	private synchronized void sendCommand(String methodName, 
			GraphicParameter...parameters) {
		queue().sendCommand(new RemoteCommand(objectUUID(),
				methodName, parameters));
	}

	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
	 */

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}
	
	/* *****************************************
	 * IAdapter interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return null;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return false;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#listeners()
	 */
	@Override
	public Set<IRemoteListener> listeners() {
		return null;
	}
}
