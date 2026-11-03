/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

/**
 * @author Federico Alcantara
 * 
 */
public class PageableStreamWrapper implements Pageable {
	
	private Pageable pageable;
	private EscpStreamJob job;
	
	public PageableStreamWrapper(Pageable pageable, String url) {
		this(pageable, url, Float.NaN, Float.NaN);
	}
	
	/**
	 * @param pageable Pageable to wrap.
	 * @param url Destination of the ESC/P stream.
	 * @param printableX Position of the printer's printable area, in points from the paper's left edge, or NaN if unknown.
	 * @param printableY Position of the printer's printable area, in points from the paper's top edge, or NaN if unknown.
	 */
	public PageableStreamWrapper(Pageable pageable, String url, float printableX, float printableY) {
		this.pageable = pageable;
		this.job = new EscpStreamJob(url, pageable.getNumberOfPages(), printableX, printableY);
	}
	
	@Override
	public int getNumberOfPages() {
		return pageable.getNumberOfPages();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return pageable.getPageFormat(pageIndex);
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		Printable printable = pageable.getPrintable(pageIndex);
		return new PrintableStreamWrapper(printable, job);
	}
	
}
