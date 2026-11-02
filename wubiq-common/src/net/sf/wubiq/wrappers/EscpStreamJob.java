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

import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.dotmatrix.ESCPrinter;
import net.sf.wubiq.enums.PrinterType;

/**
 * One print job written as a single ESC/P stream.
 * <p>The JDK calls <code>Printable.print</code> more than once for the same page (a first pass
 * that only looks at what is drawn, then the real one), and <code>Pageable.getPrintable</code> may
 * be called again for every call. The job therefore keeps what has been written: a page is
 * printed on its first call, repeated calls for the same page do nothing, and the stream is
 * closed after the last page so that the printer receives the whole job.</p>
 * <p>The printer counts positions from where it prints, not from the paper's edge: a printer that
 * can't print on the first quarter inch of the paper counts from there. The application's
 * coordinates start at the page format's imageable area, so the text goes to the imageable area's
 * position, less the printable area's position (what the printer driver does), plus the offset of the
 * {@link PropertyKeys#WUBIQ_DOTMATRIX_OFFSET_X} and {@link PropertyKeys#WUBIQ_DOTMATRIX_OFFSET_Y} properties.
 * If the printer's printable area isn't known, the application's coordinates start where the printer does.</p>
 * @author Federico Alcantara
 *
 */
public class EscpStreamJob {
	private final String url;
	private final int numberOfPages;
	private final float printableX;
	private final float printableY;
	private final float offsetX;
	private final float offsetY;
	private ESCPrinter printer;
	private int lastPage = -1;
	private boolean written;

	/**
	 * @param url Destination of the ESC/P stream (file, port or share).
	 * @param numberOfPages Number of pages of the job, or {@link Pageable#UNKNOWN_NUMBER_OF_PAGES}.
	 */
	public EscpStreamJob(String url, int numberOfPages) {
		this(url, numberOfPages, Float.NaN, Float.NaN);
	}

	/**
	 * @param url Destination of the ESC/P stream (file, port or share).
	 * @param numberOfPages Number of pages of the job, or {@link Pageable#UNKNOWN_NUMBER_OF_PAGES}.
	 * @param printableX Position of the printer's printable area, in points from the paper's left edge, or NaN if unknown.
	 * @param printableY Position of the printer's printable area, in points from the paper's top edge, or NaN if unknown.
	 */
	public EscpStreamJob(String url, int numberOfPages, float printableX, float printableY) {
		this.url = url;
		this.numberOfPages = numberOfPages;
		this.printableX = printableX;
		this.printableY = printableY;
		this.offsetX = inchesProperty(PropertyKeys.WUBIQ_DOTMATRIX_OFFSET_X) * 72;
		this.offsetY = inchesProperty(PropertyKeys.WUBIQ_DOTMATRIX_OFFSET_Y) * 72;
	}

	private static float inchesProperty(String key) {
		String value = System.getProperty(key);
		if (value != null) {
			try {
				return Float.parseFloat(value.trim());
			} catch (NumberFormatException e) {
				// Not a number: no offset.
			}
		}
		return 0;
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
		float originX = offsetX;
		float originY = offsetY;
		if (!Float.isNaN(printableX) && !Float.isNaN(printableY)) {
			originX += (float) pageFormat.getImageableX() - printableX;
			originY += (float) pageFormat.getImageableY() - printableY;
		}
		GraphicsStreamWrapper wrapper = new GraphicsStreamWrapper((Graphics2D) graph, originX, originY);
		int returnValue;
		try {
			returnValue = printable.print(wrapper, pageFormat, pageIndex);
			if (returnValue == Printable.NO_SUCH_PAGE) {
				close();
			} else {
				ESCPrinter escPrinter = printer(pageFormat, PrinterType.DOT_MATRIX_HQ.equals(wrapper.getPrinterType()));
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

	private ESCPrinter printer(PageFormat pageFormat, boolean pins24) throws PrintException {
		if (printer == null) {
			printer = new ESCPrinter(url,
					pageFormat.getPaper().getWidth() / 72 * 1440,
					pageFormat.getPaper().getHeight() / 72 * 1440,
					pins24, written);
			written = true;
		}
		return printer;
	}
}
