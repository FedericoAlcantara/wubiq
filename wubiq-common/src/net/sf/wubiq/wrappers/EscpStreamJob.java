package net.sf.wubiq.wrappers;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.Iterator;

import javax.print.PrintException;

import net.sf.wubiq.dotmatrix.ESCPrinter;

/**
 * One print job written as a single ESC/P stream.
 * <p>The JDK calls <code>Printable.print</code> more than once for the same page (a first pass
 * that only looks at what is drawn, then the real one), and <code>Pageable.getPrintable</code> may
 * be called again for every call. The job therefore keeps what has been written: a page is
 * printed on its first call, repeated calls for the same page do nothing, and the stream is
 * closed after the last page so that the printer receives the whole job.</p>
 * @author Federico Alcantara
 *
 */
public class EscpStreamJob {
	private final String url;
	private final int numberOfPages;
	private ESCPrinter printer;
	private int lastPage = -1;
	private boolean written;

	/**
	 * @param url Destination of the ESC/P stream (file, port or share).
	 * @param numberOfPages Number of pages of the job, or {@link Pageable#UNKNOWN_NUMBER_OF_PAGES}.
	 */
	public EscpStreamJob(String url, int numberOfPages) {
		this.url = url;
		this.numberOfPages = numberOfPages;
	}

	/**
	 * Prints one page of the job.
	 * @param printable Application's printable.
	 * @param graph Graphics sent by the JDK.
	 * @param pageFormat Page format of the page.
	 * @param pageIndex Index of the page.
	 * @return {@link Printable#PAGE_EXISTS} or {@link Printable#NO_SUCH_PAGE}.
	 * @throws PrinterException If the stream can't be written.
	 */
	public synchronized int print(Printable printable, Graphics graph, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		if (pageIndex == lastPage) {
			return Printable.PAGE_EXISTS; // Second pass over a page already written.
		}
		if (pageIndex < lastPage) {
			lastPage = -1; // The job starts again (a new copy).
		}
		GraphicsStreamWrapper wrapper = new GraphicsStreamWrapper((Graphics2D) graph);
		int returnValue;
		try {
			returnValue = printable.print(wrapper, pageFormat, pageIndex);
			if (returnValue == Printable.NO_SUCH_PAGE) {
				close();
			} else {
				ESCPrinter escPrinter = printer(pageFormat);
				Iterator<TextField> it = wrapper.getTexts().iterator();
				while (it.hasNext()) {
					TextField textField = it.next();
					escPrinter.advanceTo(textField);
					escPrinter.print(textField);
				}
				escPrinter.endPage();
				lastPage = pageIndex;
				if (numberOfPages != Pageable.UNKNOWN_NUMBER_OF_PAGES && pageIndex >= numberOfPages - 1) {
					close();
				}
			}
		} catch (PrintException e) {
			close();
			throw new PrinterException(e.getMessage());
		} catch (IOException e) {
			close();
			throw new PrinterException(e.getMessage());
		} catch (PrinterException e) {
			close();
			throw e;
		} catch (RuntimeException e) {
			close();
			throw e;
		}
		return returnValue;
	}

	/**
	 * Closes the stream. The next page, if any, appends to what was written.
	 */
	public synchronized void close() {
		if (printer != null) {
			printer.close();
			printer = null;
		}
	}

	private ESCPrinter printer(PageFormat pageFormat) throws PrintException {
		if (printer == null) {
			printer = new ESCPrinter(url,
					pageFormat.getPaper().getWidth() / 72 * 1440,
					pageFormat.getPaper().getHeight() / 72 * 1440,
					false, written);
			written = true;
		}
		return printer;
	}
}
