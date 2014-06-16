/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.proxies.ProxyAdapterSlave;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class PrintableAdapter implements Printable, IAdapter, IProxyMaster {
	
	private GraphicsAdapter graphicsAdapter;
	public static final String[] FILTERED_METHODS = new String[]{
		
	};
	
	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return printable().print(graphics, pageFormat, pageIndex);
	}

	/**
	 * Special method for setting the communication between remote and local printable.
	 * @param pageFormat Page format to use.
	 * @param pageIndex Page index.
	 * @param remoteGraphicsUUID UUID of the correspondant remote graphics
	 * @return Status of the action.
	 * @throws PrinterException
	 */
	public int print(PageFormat pageFormat, int pageIndex, UUID remoteGraphicsUUID) throws PrinterException {
		if (graphicsAdapter == null) {
			graphicsAdapter = (GraphicsAdapter)
					Enhancer.create(GraphicsAdapter.class, 
							new ProxyAdapterSlave(queue(), remoteGraphicsUUID, GraphicsAdapter.FILTERED_METHODS));
		}
		return print(graphicsAdapter, pageFormat, pageIndex);
	}
	
	public Printable printable() {
		return (Printable) decoratedObject();
	}

	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
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
	
	/* *****************************************
	 * IProxyMaster interface implementation
	 * *****************************************
	 */

	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	@Override
	public Object decoratedObject() {
		return null;
	}
}
