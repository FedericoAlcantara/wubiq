/**
 *
 */
package net.sf.wubiq.wrappers;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * Prints a page of a job into an ESC/P stream.
 * @author Federico Alcantara
 *
 */
public class PrintableStreamWrapper implements Printable {
	private Printable printable;
	private EscpStreamJob job;

	/**
	 * Wraps a printable that is the whole job.
	 * @param printable Printable to wrap.
	 * @param url Destination of the ESC/P stream.
	 */
	public PrintableStreamWrapper(Printable printable, String url) {
		this(printable, url, Float.NaN, Float.NaN);
	}
	
	/**
	 * Wraps a printable that is the whole job.
	 * @param printable Printable to wrap.
	 * @param url Destination of the ESC/P stream.
	 * @param printableX Position of the printer's printable area, in points from the paper's left edge, or NaN if unknown.
	 * @param printableY Position of the printer's printable area, in points from the paper's top edge, or NaN if unknown.
	 */
	public PrintableStreamWrapper(Printable printable, String url, float printableX, float printableY) {
		this(printable, new EscpStreamJob(url, Pageable.UNKNOWN_NUMBER_OF_PAGES, printableX, printableY));
	}

	/**
	 * Wraps a page of a job.
	 * @param printable Printable to wrap.
	 * @param job Job that owns the stream.
	 */
	public PrintableStreamWrapper(Printable printable, EscpStreamJob job) {
		this.printable = printable;
		this.job = job;
	}

	@Override
	public int print(Graphics graph, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return job.print(printable, graph, pageFormat, pageIndex);
	}

}
