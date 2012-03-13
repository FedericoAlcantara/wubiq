/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

import javax.print.DocFlavor;

import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum PageableUtils {
	INSTANCE;
	
	public static final Log LOG = LogFactory.getLog(PageableUtils.class);
	
	public synchronized InputStream getStreamForBytes(Object printData, DocFlavor docFlavor, 
			PageFormat pageFormat) throws IOException {
		InputStream returnValue = null;
		if (printData instanceof InputStream) {
			returnValue = (InputStream)printData;
		} else if (printData instanceof Reader) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int byteVal = -1;
			while((byteVal = ((Reader)printData).read()) > -1) {
				output.write(byteVal);
			}
			returnValue = new ByteArrayInputStream(output.toByteArray());
			output.close();
		} else if (printData instanceof Pageable) {
			returnValue = serializePageable((Pageable)printData);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
		} else if (printData instanceof Printable) {
			returnValue = serializePrintable((Printable)printData, pageFormat);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		}
		
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @return Input stream representing the serialized pageable.
	 */
	private InputStream serializePageable(Pageable inputPageable) {
		InputStream returnValue = null;
		PageableWrapper pageable = new PageableWrapper(inputPageable);
		int pageResult = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		do {
			try {
				PageFormatWrapper pageFormat = new PageFormatWrapper(pageable.getOriginal().getPageFormat(pageIndex));
				PrintableWrapper printable = new PrintableWrapper(pageable.getOriginal().getPrintable(pageIndex));
				pageResult = printPrintable(printable, pageFormat, pageIndex);
				if (pageResult == Printable.PAGE_EXISTS) {
					pageable.addPageFormat(pageFormat);
					pageable.addPrintable(printable);
					pageIndex++;
				}
			} catch (IndexOutOfBoundsException e) {
				LOG.debug("Reached end of printables");
				break;
			}
		} while (pageResult == Printable.PAGE_EXISTS);
		pageable.setNumberOfPages(pageIndex);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream output;
		try {
			output = new ObjectOutputStream(out);
			output.writeObject(pageable);
			returnValue = new ByteArrayInputStream(out.toByteArray());
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @return Input stream representing the serialized pageable.
	 */
	private InputStream serializePrintable(Printable inputPrintable, PageFormat pageFormat) {
		InputStream returnValue = null;
		PrintableWrapper printable = new PrintableWrapper(inputPrintable);
		try {
			printPrintable(printable, new PageFormatWrapper(getPageFormat(pageFormat)), 0);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(out);
			output.writeObject(printable);
			returnValue = new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Perform a printable print method and conserves the graphic command list.
	 * @param printable Printable to print.
	 * @param pageFormat Page format.
	 * @param pageIndex Page to be printed.
	 */
	private int printPrintable(Printable printable, PageFormat pageFormat, int pageIndex) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		try {
			returnValue = ((PrintableWrapper)printable).print(graph, pageFormat, pageIndex, 1.0, 1.0);
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	private PageFormat getPageFormat(PageFormat pageFormat) {
		if (pageFormat != null) {
			return pageFormat;
		} else {
			return new PageFormatWrapper(new PageFormat());
		}
	}
}
