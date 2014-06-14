/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Set;

import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.interfaces.IRemotePrintableAdapter;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class RemotePrintableAdapter implements IRemotePrintableAdapter {
	
	private Printable printable;
	private String queueId;
	private RemoteGraphicsAdapter remoteGraphicsAdapter;
	private IDirectConnectorQueue queue;
	
	public RemotePrintableAdapter() {
	}

	public RemotePrintableAdapter(Printable printable, String queueId) {
		this();
		this.printable = printable;
		this.queueId = queueId;
		queue = DirectConnectUtils.INSTANCE.directConnector(queueId());
	}
	
	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return printable.print(remoteGraphicsAdapter, pageFormat, pageIndex);
	}

	public int print(PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (remoteGraphicsAdapter == null) {
			remoteGraphicsAdapter = new RemoteGraphicsAdapter(queueId());
			queue.registerObject(RemoteCommandType.GRAPHICS, remoteGraphicsAdapter);
		}
		return print(remoteGraphicsAdapter, pageFormat, pageIndex);
	}
	
	@Override
	public String queueId() {
		return queueId;
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

}
