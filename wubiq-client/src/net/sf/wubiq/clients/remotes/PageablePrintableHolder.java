/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

/**
 * Holds a single printable within a pageable.
 * @author Federico Alcantara
 *
 */
public class PageablePrintableHolder implements Pageable {

	private PageFormat pageFormat;
	private PrintableChunkRemote printable;
	
	/**
	 * Constructor.
	 * @param printable Printable to be hold.
	 */
	public PageablePrintableHolder(PrintableChunkRemote printable) {
		this.printable = printable;
	}
	
	/**
	 * Always return one.
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	@Override
	public int getNumberOfPages() {
		return 1;
	}

	/**
	 * Sets the page format for the printable.
	 * @param pageFormat Page format to use.
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	
	/**
	 * Ignores the parameter for index and always return the same
	 * page format.
	 * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	@Override
	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {
		return this.pageFormat;
	}

	/**
	 * Returns the unique printable.
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	@Override
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		return printable;
	}

}
