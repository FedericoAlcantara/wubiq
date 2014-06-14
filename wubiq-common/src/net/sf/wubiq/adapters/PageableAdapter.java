/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Set;
import java.util.UUID;

import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.interfaces.IRemotePageableAdapter;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.PageFormatWrapper;

/**
 * 
 * @author Federico Alcantara
 *
 */
public class PageableAdapter implements IRemotePageableAdapter {	
	private Pageable pageable;
	private int lastPageFormatProcessed = -1;
	private int lastPrintableProcessed = -1;
	private PageFormatWrapper lastPageFormat = null;
	private PrintableAdapter lastPrintable = null;
	private IDirectConnectorQueue queue;
	private UUID objectUUID;
	
	public PageableAdapter() {
	}
	
	public PageableAdapter(Pageable pageable, IDirectConnectorQueue queue, UUID objectUUID) {
		this();
		this.pageable = pageable;
		this.objectUUID = objectUUID;
		this.queue = queue;
		queue.registerObject(objectUUID, this);
	}

	public PageableAdapter(Pageable pageable, String queueId) {
		this();
	}
	
	
	/**
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		int pages = pageable.getNumberOfPages();
		sendCommand("setNumberOfPages", 
				new GraphicParameter(int.class, pages));
		return pages;
	}

	/**
	 * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat pageFormat = pageable.getPageFormat(pageIndex);
		if (pageIndex != lastPageFormatProcessed) {
			lastPageFormat = new PageFormatWrapper(pageFormat);
		}
		lastPageFormatProcessed = pageIndex;
		return lastPageFormat;
	}

	/**
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		Printable printable = pageable.getPrintable(pageIndex);
		if (pageIndex != lastPrintableProcessed) {
			lastPrintable = new PrintableAdapter(printable, queue());
		}
		lastPrintableProcessed = pageIndex;
		return lastPrintable; // Must be the adapter
	}
	
	public UUID getLastPrintableObjectUUID() throws IndexOutOfBoundsException {
		return lastPrintable.getObjectUUID();
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
		return this.objectUUID;
	}
	
}
