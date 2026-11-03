package net.sf.wubiq.wrappers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.PrintException;

import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.dotmatrix.ESCPrinter;
import net.sf.wubiq.dotmatrix.GraphicsRaster;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.PrintServiceUtils;

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
 * <p>Text is written as text. What else the application draws (lines, boxes, charts, images) is drawn on a raster
 * and written as bit-image bands (see {@link GraphicsRaster}), unless the {@link PropertyKeys#WUBIQ_DOTMATRIX_GRAPHICS}
 * property is <code>false</code>. A page the application draws only text on writes no bands.</p>
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
	private PrinterType printerType;
	private boolean pins24;

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

	/**
	 * Tells the type of the printer, which the graphics offered to {@link #printAll(Pageable, int)} can't:
	 * 24-pin printers get 24 dot bands and the line spacing of their finer pins.
	 * @param printerType Type of the printer; null to take it from the graphics of each page.
	 */
	public void setPrinterType(PrinterType printerType) {
		this.printerType = printerType;
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
	 * Writes a whole document to the stream without a JDK print job. The JDK's print job would
	 * send the printer driver a job of its own, blank, since nothing is drawn on the printer's graphics:
	 * the printer would eject an empty page after the text. The pages are drawn on an image the size of
	 * the paper instead, with the origin and clip of the page format's imageable area, as the printer's
	 * graphics would have them.
	 * @param pageable Document to write.
	 * @param copies Number of times the document is written; less than one is one.
	 * @throws PrinterException If the stream can't be written.
	 */
	public synchronized void printAll(Pageable pageable, int copies) throws PrinterException {
		try {
			for (int copy = 0; copy < Math.max(copies, 1); copy++) {
				lastPage = -1;
				int pageIndex = 0;
				boolean more = true;
				while (more && (pageable.getNumberOfPages() == Pageable.UNKNOWN_NUMBER_OF_PAGES
						|| pageIndex < pageable.getNumberOfPages())) {
					PageFormat pageFormat;
					Printable printable;
					try {
						pageFormat = pageable.getPageFormat(pageIndex);
						printable = pageable.getPrintable(pageIndex);
					} catch (IndexOutOfBoundsException e) {
						break; // The pageable has no more pages.
					}
					BufferedImage image = new BufferedImage(
							Math.max((int) Math.ceil(pageFormat.getWidth()), 1),
							Math.max((int) Math.ceil(pageFormat.getHeight()), 1),
							BufferedImage.TYPE_BYTE_GRAY);
					Graphics2D graphics = image.createGraphics();
					try {
						graphics.setColor(Color.BLACK); // As the printer's graphics are.
						graphics.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
						graphics.setClip(0, 0, (int) Math.ceil(pageFormat.getImageableWidth()),
								(int) Math.ceil(pageFormat.getImageableHeight()));
						more = print(printable, graphics, pageFormat, pageIndex) == Printable.PAGE_EXISTS;
					} finally {
						graphics.dispose();
					}
					pageIndex++;
				}
			}
		} finally {
			close();
		}
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
		boolean pins24Page = written ? pins24 : PrinterType.DOT_MATRIX_HQ.equals(printerType(graph));
		GraphicsRaster raster = graphics() ? new GraphicsRaster(pageFormat, pins24Page) : null;
		GraphicsStreamWrapper wrapper = new GraphicsStreamWrapper((Graphics2D) graph, originX, originY, raster);
		int returnValue;
		try {
			returnValue = printable.print(wrapper, pageFormat, pageIndex);
			if (returnValue == Printable.NO_SUCH_PAGE) {
				close();
			} else {
				ESCPrinter escPrinter = printer(pageFormat, pins24Page);
				List<TextField> texts = wrapper.getTexts();
				List<GraphicsRaster.Band> bands = raster == null ? new ArrayList<GraphicsRaster.Band>()
						: raster.getBands(originX - (float) pageFormat.getImageableX(), originY - (float) pageFormat.getImageableY());
				// The paper only moves forward: texts and bands go in the order of their place down the page.
				int textIndex = 0;
				int bandIndex = 0;
				while (textIndex < texts.size() || bandIndex < bands.size()) {
					if (bandIndex < bands.size() && (textIndex >= texts.size()
							|| escPrinter.verticalTarget(bands.get(bandIndex)) <= escPrinter.verticalTarget(texts.get(textIndex)))) {
						escPrinter.print(bands.get(bandIndex++));
					} else {
						TextField textField = texts.get(textIndex++);
						escPrinter.advanceTo(textField);
						escPrinter.print(textField);
					}
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
		} finally {
			if (raster != null) {
				raster.dispose();
			}
		}
		return returnValue;
	}

	/**
	 * @return The type of the printer: the one set, or the one the graphics' device name tells.
	 */
	private PrinterType printerType(Graphics graph) {
		if (printerType != null) {
			return printerType;
		}
		return PrintServiceUtils.printerType(((Graphics2D) graph).getDeviceConfiguration().getDevice().getIDstring());
	}

	/**
	 * @return False if the {@link PropertyKeys#WUBIQ_DOTMATRIX_GRAPHICS} property is <code>false</code>: only text is written.
	 */
	private static boolean graphics() {
		return !"false".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_DOTMATRIX_GRAPHICS, "").trim());
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
			this.pins24 = pins24;
			printer = new ESCPrinter(url,
					pageFormat.getPaper().getWidth() / 72 * 1440,
					pageFormat.getPaper().getHeight() / 72 * 1440,
					pins24, written);
			written = true;
		}
		return printer;
	}
}
