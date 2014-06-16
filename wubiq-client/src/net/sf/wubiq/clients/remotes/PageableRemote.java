/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IProxySlave;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemote implements Pageable, IProxySlave {
	private DirectPrintManager manager;
	private UUID objectUUID;
	private int lastPrintablePageIndex;
	private Printable printable;
	
	public static final String[] FILTERED_METHODS = new String[]{
		"getPrintable"};
	
	public PageableRemote() {
		lastPrintablePageIndex = -1;
		initialize();
	}
	
	public void initialize() {
	}
	
	/**
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		return 0;
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return null;
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
			manager.readFromRemote(new RemoteCommand(objectUUID, "getPrintable",
					pageIndex));
			UUID printableObjectUUID = (UUID) manager.readFromRemote(objectUUID, "getLastPrintableObjectUUID");
			printable = (PrintableRemote) Enhancer.create(PrintableRemote.class,
					new ProxyRemoteSlave(manager, printableObjectUUID,
							PrintableRemote.FILTERED_METHODS));
		}

		return printable;
	}

}
