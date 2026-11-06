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
	private String url;
	
	public PageableStreamWrapper(Pageable pageable, String url) {
		this.pageable = pageable;
		this.url = url;
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
		return new PrintableStreamWrapper(printable, url);
	}
	
}
