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

import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsConfigurationAdapter extends GraphicsConfiguration
		implements IRemoteAdapter {
	private IDirectConnectorQueue queue;
	private UUID objectUUID;
	
	public GraphicsConfigurationAdapter(IDirectConnectorQueue queue, UUID objectUUID) {
		this.queue = queue;
		this.objectUUID = objectUUID;
		queue.registerObject(objectUUID, this);
	}
	
	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height) {
		sendCommand("createCompatibleImageRemote",
			new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height));
		
		return (BufferedImage)DirectConnectUtils.INSTANCE
				.deserializeImage((String) queue().returnData());
	}

	/**
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
	 */
	@Override
	public BufferedImage createCompatibleImage(int width, int height, int transparency) {
		sendCommand("createCompatibleImageRemote",
			new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height));
		
		return (BufferedImage)DirectConnectUtils.INSTANCE
				.deserializeImage((String) queue().returnData());
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getBounds()
	 */
	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getColorModel()
	 */
	@Override
	public ColorModel getColorModel() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getColorModel(int)
	 */
	@Override
	public ColorModel getColorModel(int transparency) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getDefaultTransform()
	 */
	@Override
	public AffineTransform getDefaultTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getDevice()
	 */
	@Override
	public GraphicsDevice getDevice() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.awt.GraphicsConfiguration#getNormalizingTransform()
	 */
	@Override
	public AffineTransform getNormalizingTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Sends a command to the remote printer.
	 * @param graphicCommand Command to send. Must never be null.
	 */
	private synchronized void sendCommand(String methodName, 
			GraphicParameter...parameters) {
		queue.sendCommand(new RemoteCommand(getObjectUUID(),
				methodName, parameters));
	}

	/* *****************************************
	 * IRemoteAdapter interface implementation
	 * *****************************************
	 */
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return queue;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
		queue.addListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return queue.removeListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#listeners()
	 */
	public Set<IRemoteListener> listeners() {
		return queue.listeners();
	}
	
	/* **************************************
	 * SUPPORT ROUTINES
	 * *************************************
	 */

	/**
	 * @return The invoking method name.
	 */
	private String methodName() {
		if (Thread.currentThread().getStackTrace().length >= 3) {
			return Thread.currentThread().getStackTrace()[2].getMethodName();
		} else { 
			return null;
		}
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}


}
