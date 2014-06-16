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
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.interfaces.IRemotePrintableAdapter;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class PrintableAdapter implements IRemotePrintableAdapter, IProxyMaster {
	
	private Printable printable;
	private GraphicsAdapter graphicsAdapter;
	private IDirectConnectorQueue queue;
	private UUID objectUUID;
	public static final String[] FILTERED_METHODS = new String[]{
		
	};

	public PrintableAdapter() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#initialize()
	 */
	public void initialize() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return printable;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#setDecoratedObject(java.lang.Object)
	 */
	public void setDecoratedObject(Object printable) {
		this.printable = (Printable)printable;
	}
	
	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return printable.print(graphics, pageFormat, pageIndex);
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
							new ProxyAdapterSlave(queue, remoteGraphicsUUID, GraphicsAdapter.FILTERED_METHODS));
		}
		return print(graphicsAdapter, pageFormat, pageIndex);
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

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}
	
}
