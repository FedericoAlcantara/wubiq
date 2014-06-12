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
import net.sf.wubiq.print.managers.impl.DirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class RemotePrintableAdapter implements IRemotePrintableAdapter {
	
	private Printable printable;
	private String queueId;
	private RemoteGraphicsAdapter remoteGraphicsAdapter;
	private DirectConnectorQueue queue;
	
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
		if (remoteGraphicsAdapter == null) {
			remoteGraphicsAdapter = new RemoteGraphicsAdapter(queueId());
		}
		int returnValue = printable.print(remoteGraphicsAdapter, pageFormat, pageIndex);
		queue.sendCommand(new RemoteCommand(RemoteCommandType.PRINTABLE, "setReturnValue", new GraphicParameter(int.class, returnValue)));
		return returnValue;
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
