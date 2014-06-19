/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.HashMap;
import java.util.Map;
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
	private Map<Integer, PageFormat> pageFormats;
	private Integer numberOfPages = null;
	
	public static final String[] FILTERED_METHODS = new String[]{
		"getNumberOfPages",
		"getPageFormat",
		"getPrintable"
		};

	public PageableRemote() {
		initialize();
		pageFormats = new HashMap<Integer, PageFormat>();
	}
	
	/**
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		if (numberOfPages == null) {
			numberOfPages = (Integer)manager().readFromRemote(new RemoteCommand(objectUUID(), "getNumberOfPages"));
		}
		return numberOfPages;
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat returnValue = pageFormats.get(pageIndex);
		if (returnValue == null) {
			returnValue = (PageFormat)manager().readFromRemote(new RemoteCommand(objectUUID(), "getPageFormat",
					new GraphicParameter(int.class, pageIndex)));
			pageFormats.put(pageIndex, returnValue);
			manager().doLog("Page: " + pageIndex, 4);
		}
		return returnValue;
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
			printable = (PrintableChunkRemote) Enhancer.create(PrintableChunkRemote.class,
					new ProxyClientSlave(
							jobId(),
							manager(),
							printableObjectUUID,
							PrintableChunkRemote.FILTERED_METHODS));
		}

		return printable;
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
	 * IProxyClient interface implementation
	 * *****************************************
	 */
	@Override
	public DirectPrintManager manager() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
