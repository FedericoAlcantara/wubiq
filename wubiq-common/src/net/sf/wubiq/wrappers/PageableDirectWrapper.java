/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

/**
 * Represents a pageable that handles direct printing.
 * Some printers requires hand tuning and with this wrapper is possible to
 * accomplish such tasks.
 * @author Federico Alcantara
 *
 */
public class PageableDirectWrapper implements Pageable {
	private Pageable pageable;
	
	public PageableDirectWrapper(Pageable pageable) {
		this.pageable = pageable;
	}

	@Override
	public int getNumberOfPages() {
		return pageable.getNumberOfPages();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {
		PageFormat pageFormat = pageable.getPageFormat(pageIndex);
		return pageFormat;
	}

	@Override
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		Printable returnValue = new PrintableDirectWrapper(pageable.getPrintable(pageIndex));
		return returnValue;
	}
}
