/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IRemoteClient;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemote implements Pageable, IRemoteClient {
	private DirectPrintManager manager;
	private int lastFormatPageIndex;
	private int lastPrintablePageIndex;
	private PageFormat pageFormat;
	private Printable printable;
	private UUID objectUUID;

	public PageableRemote(DirectPrintManager manager, UUID objectUUID) {
		this.manager = manager;
		lastFormatPageIndex = -1;
		lastPrintablePageIndex = -1;
		this.objectUUID = objectUUID;
		manager.registerObject(objectUUID, this);
	}
	
	public PageableRemote(DirectPrintManager manager) {
		this(manager, UUID.randomUUID());
	}

	@Override
	public int getNumberOfPages() {
		return (Integer) readFromRemote("getNumberOfPages");
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		if (lastFormatPageIndex != pageIndex) {
			pageFormat = (PageFormat) readFromRemote("getPageFormat", 
				new GraphicParameter(int.class, pageIndex));
			lastFormatPageIndex = pageIndex;
		}
		return pageFormat;
	}

	/**
	 * Will create a printable object which directly communicates with the
	 * remote printable object.
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		if (lastPrintablePageIndex != pageIndex) {
			lastPrintablePageIndex = pageIndex;
			readFromRemote("getPrintable",
					new GraphicParameter(int.class, pageIndex));
			UUID printableObjectUUID = (UUID) readFromRemote("getLastPrintableObjectUUID");
			printable = new PrintableRemote(manager, printableObjectUUID);
		}

		return printable;
	}
	
	/**
	 * Reads information from the remote pageable.
	 * @param methodName Name of the method to invoke.
	 * @param parameters Parameters.
	 * @return Object read from remote. Might be null.
	 */
	private Object readFromRemote(String methodName, GraphicParameter... parameters) {
		return manager.readFromRemote(
				new RemoteCommand(getObjectUUID(), methodName, parameters));
	}

	/**
	 * @see net.sf.wubiq.interfaces.IClientRemote#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}

}
