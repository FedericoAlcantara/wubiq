/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

import net.sf.wubiq.clients.DirectPrintManager;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemote implements Pageable {
	private int numberOfPages;
	private PageFormat pageFormat;
	private Printable printable;
	private DirectPrintManager manager;
	
	public PageableRemote(DirectPrintManager manager) {
		this.manager = manager;
		numberOfPages = Pageable.UNKNOWN_NUMBER_OF_PAGES;
	}

	@Override
	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	public void setPrintable(int pageIndex) {
		printable = new PrintableRemote(manager);
	}
	
	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageFormat;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return printable;
	}
	
	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	
}
