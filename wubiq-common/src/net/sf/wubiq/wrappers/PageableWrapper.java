/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class PageableWrapper implements Pageable, Serializable {
	private static final long serialVersionUID = 1L;
	private int numberOfPages;
	private PageFormatWrapper[] pageFormats;
	private PrintableWrapper[] printables;
	
	public PageableWrapper() {
	}

	public PageableWrapper(Pageable pageable) {
		numberOfPages = pageable.getNumberOfPages();
		pageFormats = new PageFormatWrapper[numberOfPages];
		printables = new PrintableWrapper[numberOfPages];
		for (int index = 0; index < numberOfPages; index++) {
			pageFormats[index] = new PageFormatWrapper(pageable.getPageFormat(index));
			printables[index] = new PrintableWrapper(pageable.getPrintable(index));
		}
	}

	@Override
	public int getNumberOfPages() {
		return numberOfPages;
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageFormats[pageIndex];
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return printables[pageIndex];
	}
	
}
