/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemote implements Pageable {
	private DirectPrintManager manager;
	private int lastFormatPageIndex;
	private int lastPrintablePageIndex;
	private PageFormat pageFormat;
	private Printable printable;
	
	
	public PageableRemote(DirectPrintManager manager) {
		this.manager = manager;
		lastFormatPageIndex = -1;
		lastPrintablePageIndex = -1;
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
			printable = new PrintableRemote(manager);
			lastPrintablePageIndex = pageIndex;
			manager.registerObject(RemoteCommandType.PRINTABLE, printable);
			readFromRemote("getPrintable",
					new GraphicParameter(int.class, pageIndex));
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
				new RemoteCommand(RemoteCommandType.PAGEABLE, methodName, parameters));
	}
}
