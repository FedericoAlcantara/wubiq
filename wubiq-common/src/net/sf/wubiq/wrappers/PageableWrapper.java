/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Federico Alcantara
 *
 */
public class PageableWrapper implements Pageable, Serializable {
	private static final long serialVersionUID = 1L;
	private int numberOfPages;
	private List<PageFormatWrapper> pageFormats;
	private List<PrintableWrapper> printables;
	private transient Pageable original;
	private transient boolean notSerialized = false;
	
	public PageableWrapper() {
		pageFormats = new ArrayList<PageFormatWrapper>();
		printables = new ArrayList<PrintableWrapper>();
		numberOfPages = Pageable.UNKNOWN_NUMBER_OF_PAGES;
	}

	public PageableWrapper(Pageable pageable) {
		this();
		numberOfPages = pageable.getNumberOfPages();
		this.original = pageable;
		if (pageable instanceof PageableWrapper) {
			this.notSerialized = ((PageableWrapper)pageable).isNotSerialized();
		}
	}

	@Override
	public int getNumberOfPages() {
		return numberOfPages;
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageFormats.get(pageIndex);
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		return printables.get(pageIndex);
	}
	
	public void addPageFormat(PageFormatWrapper pageFormat) {
		pageFormats.add(pageFormat);
	}
	
	public void addPrintable(PrintableWrapper printable) {
		printable.setNotSerialized(notSerialized);
		printables.add(printable);
	}
	
	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	
	public Pageable getOriginal() {
		return original;
	}

	/**
	 * @return the notSerialized
	 */
	public boolean isNotSerialized() {
		return notSerialized;
	}

	/**
	 * @param notSerialized the notSerialized to set
	 */
	public void setNotSerialized(boolean notSerialized) {
		this.notSerialized = notSerialized;
	}
}
