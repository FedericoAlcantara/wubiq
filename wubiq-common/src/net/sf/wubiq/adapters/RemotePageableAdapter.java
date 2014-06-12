/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Set;

import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.interfaces.IRemotePageableAdapter;
import net.sf.wubiq.print.managers.impl.DirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.PageFormatWrapper;

/**
 * 
 * @author Federico Alcantara
 *
 */
public class RemotePageableAdapter implements IRemotePageableAdapter {	
	private Pageable pageable;
	private String queueId;
	private int lastPageFormatProcessed = -1;
	private int lastPrintableProcessed = -1;
	private PageFormatWrapper lastPageFormat = null;
	private RemotePrintableAdapter lastPrintable = null;
	private DirectConnectorQueue queue;
	
	public RemotePageableAdapter() {
	}
	
	public RemotePageableAdapter(Pageable pageable, String queueId) {
		this();
		this.pageable = pageable;
		this.queueId = queueId;
		queue = DirectConnectUtils.INSTANCE.directConnector(queueId());
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
		
		sendCommand("setPageFormat", 
				new GraphicParameter(int.class, pageIndex),
				new GraphicParameter(PageFormat.class, lastPageFormat));
		return pageFormat;
	}

	/**
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		Printable printable = pageable.getPrintable(pageIndex);
		if (pageIndex != lastPrintableProcessed) {
			lastPrintable = new RemotePrintableAdapter(printable, queueId());
			sendCommand("setPagePrintable",
					new GraphicParameter(int.class, pageIndex));
		}
		lastPrintableProcessed = pageIndex;
		return lastPrintable; // Must be the adapter
	}

	/**
	 * Sends a command to the remote printer.
	 * @param graphicCommand Command to send. Must never be null.
	 */
	private synchronized void sendCommand(String methodName, 
			GraphicParameter...parameters) {
		queue.sendCommand(new RemoteCommand(RemoteCommandType.PAGEABLE,
				methodName, parameters));
	}
	
	@Override
	public void addListener(IRemoteListener listener) {
		queue.addListener(listener);
	}

	@Override
	public boolean removeListener(IRemoteListener listener) {
		return queue.removeListener(listener);
	}

	public Set<IRemoteListener> listeners() {
		return queue.listeners();
	}

	@Override
	public String queueId() {
		return queueId;
	}
	
}
