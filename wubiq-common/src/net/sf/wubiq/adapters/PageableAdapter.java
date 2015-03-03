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
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.proxies.ProxyAdapterMaster;
import net.sf.wubiq.wrappers.PageFormatWrapper;

/**
 * 
 * @author Federico Alcantara
 *
 */
public class PageableAdapter implements Pageable, IAdapter, IProxyMaster {	
	public static final String[] FILTERED_METHODS = new String[]{
		"pageable",
		"getPageFormat",
		"getPrintable",
		"getLastPrintableObjectUUID",
		"setClientSupportsCompression"
	};

	private int lastPageFormatProcessed = -1;
	private int lastPrintableProcessed = -1;
	private PageFormatWrapper lastPageFormat = null;
	private PrintableChunkAdapter lastPrintable = null;
	private boolean clientSupportsCompression = false;
	
	public PageableAdapter() {
		initialize();
	}
	
	/**
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		return 0;
	}

	public void setClientSupportsCompression(boolean clientSupportsCompression) {
		this.clientSupportsCompression = clientSupportsCompression;
	}
	
	/**
	 * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat pageFormat = pageable().getPageFormat(pageIndex);
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
		Printable printable = pageable().getPrintable(pageIndex);
		if (pageIndex != lastPrintableProcessed) {
			lastPrintable = 
					(PrintableChunkAdapter)
					Enhancer.create(PrintableChunkAdapter.class,
							new ProxyAdapterMaster(
									jobId(),
									queue(),
									printable,
									PrintableChunkAdapter.FILTERED_METHODS));
			lastPrintable.setPageable(this);
		}
		lastPrintableProcessed = pageIndex;
		((PrintableChunkAdapter)lastPrintable).setClientSupportsCompression(clientSupportsCompression);
		return lastPrintable; // Must be the adapter
	}
	
	public UUID getLastPrintableObjectUUID() throws IndexOutOfBoundsException {
		return lastPrintable.objectUUID();
	}
	
	public Pageable pageable() {
		return (Pageable) decoratedObject();
	}
	
	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	@Override
	public void initialize(){
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#jobId()
	 */
	@Override
	public Long jobId() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	@Override
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
