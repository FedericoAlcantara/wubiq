/**
 * 
 */
package net.sf.wubiq.print;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.wubiq.dao.WubiqPrintJobPageDao;

/**
 * @author Federico Alcantara
 *
 */
public class JpaPageable implements Pageable, Serializable {
	private static final long serialVersionUID = 1L;
	private List<PageFormat> pageFormats;
	private List<Long> printablePageIds;
	
	public JpaPageable() {
		pageFormats = new ArrayList<PageFormat>();
		printablePageIds = new ArrayList<Long>();
	}
	
	@Override
	public int getNumberOfPages() {
		return printablePageIds.size();
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		if (pageIndex >= getNumberOfPages()) {
			throw new IndexOutOfBoundsException("");
		}
		return pageFormats.get(pageIndex);
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		if (pageIndex >= getNumberOfPages()) {
			throw new IndexOutOfBoundsException("");
		}
		Long id = printablePageIds.get(pageIndex);
		return WubiqPrintJobPageDao.INSTANCE.findPrintable(id);
	}

	public void add(Long id, PageFormat pageFormat) {
		printablePageIds.add(id);
		pageFormats.add(pageFormat);
	}
}
