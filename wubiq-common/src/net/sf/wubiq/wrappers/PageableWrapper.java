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
		if (numberOfPages == Pageable.UNKNOWN_NUMBER_OF_PAGES) {
			int pageIndex = 0;
			do {
				try {
					pageable.getPageFormat(pageIndex);
					pageIndex++;
				} catch (IndexOutOfBoundsException e) {
					pageIndex = pageIndex - 1;
					break;
				}
			} while (true);
			numberOfPages = pageIndex;
		}
		if (numberOfPages > 0) {
			pageFormats = new PageFormatWrapper[numberOfPages];
			printables = new PrintableWrapper[numberOfPages];
			for (int index = 0; index < numberOfPages; index++) {
				pageFormats[index] = new PageFormatWrapper(pageable.getPageFormat(index));
				printables[index] = new PrintableWrapper(pageable.getPrintable(index));
			}
		} else {
			pageFormats = new PageFormatWrapper[]{};
			printables = new PrintableWrapper[]{};
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
