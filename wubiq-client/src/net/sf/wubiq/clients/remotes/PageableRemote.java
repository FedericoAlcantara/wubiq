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
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.proxies.ProxyClientSlave;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemote implements Pageable, IProxyClient {
	private int lastPrintablePageIndex = -1;
	private Printable printable;
	
	public static final String[] FILTERED_METHODS = new String[]{
		"getPrintable"};

	public PageableRemote() {
		initialize();
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
			manager().readFromRemote(new RemoteCommand(objectUUID(), "getPrintable",
					new GraphicParameter(int.class, pageIndex)));
			UUID printableObjectUUID = (UUID) manager().readFromRemote(new RemoteCommand(objectUUID(),
					"getLastPrintableObjectUUID"));
			printable = (PrintableRemote) Enhancer.create(PrintableRemote.class,
					new ProxyClientSlave(manager(), printableObjectUUID,
							PrintableRemote.FILTERED_METHODS));
		}

		return printable;
	}

	/* ***************************
	 * Proxied methods
	 * ***************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyClient#manager()
	 */
	public DirectPrintManager manager() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}
	
}
