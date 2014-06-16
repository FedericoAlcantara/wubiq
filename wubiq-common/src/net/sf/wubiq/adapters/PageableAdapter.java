/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.interfaces.IRemotePageableAdapter;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.wrappers.PageFormatWrapper;

/**
 * 
 * @author Federico Alcantara
 *
 */
public class PageableAdapter implements IRemotePageableAdapter, IProxyMaster {	
	private Pageable pageable;
	private int lastPageFormatProcessed = -1;
	private int lastPrintableProcessed = -1;
	private PageFormatWrapper lastPageFormat = null;
	private PrintableAdapter lastPrintable = null;
	public IDirectConnectorQueue queue;
	public UUID objectUUID;
	public static final String[] FILTERED_METHODS = new String[]{
		
	};
	
	public PageableAdapter() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IAdapterMaster#initialize()
	 */
	public void initialize() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IAdapterMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return pageable;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#setDecoratedObject(java.lang.Object)
	 */
	public void setDecoratedObject(Object pageable) {
		this.pageable = (Pageable)pageable;
	}
		
	/**
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		int pages = pageable.getNumberOfPages();
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
			lastPrintable = (PrintableAdapter)
					Enhancer.create(PrintableAdapter.class,
							new ProxyAdapterMaster(queue, PrintableAdapter.FILTERED_METHODS));
			lastPrintable.initialize();
			lastPrintable.setDecoratedObject(printable);
		}
		lastPrintableProcessed = pageIndex;
		return lastPrintable; // Must be the adapter
	}
	
	public UUID getLastPrintableObjectUUID() throws IndexOutOfBoundsException {
		return lastPrintable.getObjectUUID();
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
