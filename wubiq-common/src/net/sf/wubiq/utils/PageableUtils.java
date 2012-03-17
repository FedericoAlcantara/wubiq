/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;

import javax.print.DocFlavor;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

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
	
	public InputStream getStreamForBytes(Object printData, DocFlavor docFlavor, 
			PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) throws IOException {
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
			returnValue = serializePageable((Pageable)printData, printRequestAttributes);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
		} else if (printData instanceof Printable) {
			returnValue = serializePrintable((Printable)printData, pageFormat, printRequestAttributes);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		}
		
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @return Input stream representing the serialized pageable.
	 */
	private InputStream serializePageable(Pageable inputPageable, PrintRequestAttributeSet printRequestAttributes) {
		InputStream returnValue = null;
		PageableWrapper pageable = new PageableWrapper(inputPageable);
		int pageResult = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		do {
			try {
				PageFormat originalPageFormat = getPageFormat(pageable.getOriginal().getPageFormat(pageIndex), printRequestAttributes);
				PageFormatWrapper pageFormat = new PageFormatWrapper(originalPageFormat);
				PrintableWrapper printable = new PrintableWrapper(pageable.getOriginal().getPrintable(pageIndex));
				printable.setNotSerialized(true);
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
	private InputStream serializePrintable(Printable inputPrintable, PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) {
		InputStream returnValue = null;
		PrintableWrapper printable = new PrintableWrapper(inputPrintable);
		printable.setNotSerialized(true);
		try {
			printPrintable(printable, new PageFormatWrapper(getPageFormat(pageFormat, printRequestAttributes)), 0);
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
	private synchronized int printPrintable(Printable printable, PageFormat pageFormat, int pageIndex) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		try {
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.scale(1, 1);
			graph.setTransform(scaleTransform);
			graph.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			graph.setClip(new Rectangle2D.Double(
					0,
					0,
					pageFormat.getPaper().getImageableWidth(), 
					pageFormat.getPaper().getImageableHeight()));
			graph.setBackground(Color.WHITE);
			graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
					(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
			returnValue = ((PrintableWrapper)printable).print(graph, pageFormat, pageIndex);
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Creates a pageformat according to the print request attributes.
	 * @param printRequestAttributes Print requestAttributes
	 * @return a new appropriate page format or null if print request does not contains enough information.
	 */
	public PageFormat getPageFormat(PrintRequestAttributeSet printRequestAttributes) {
		PageFormat pageFormat = null; // Creates default page format.
		if (printRequestAttributes != null) {
			MediaSizeName mediaSizeName = null;
			MediaPrintableArea mediaPrintableArea = null;
			OrientationRequested orientation = null;
			for (Attribute attribute : printRequestAttributes.toArray()) {
				if (attribute instanceof MediaSizeName) {
					mediaSizeName = (MediaSizeName)attribute;
				} else if (attribute instanceof MediaPrintableArea) {
					mediaPrintableArea = (MediaPrintableArea)attribute;
				} else if (attribute instanceof OrientationRequested) {
					orientation = (OrientationRequested)attribute;
				}
			}
			if (mediaSizeName != null) {
				if (orientation == null) {
					orientation = OrientationRequested.PORTRAIT;
				}
				MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaSizeName);
				if (mediaSize != null) {
					float width = mediaSize.getX(MediaSize.INCH);
					float height = mediaSize.getY(MediaSize.INCH);
					float x = 0;
					float y = 0;
					float printableWidth = width;
					float printableHeight = height;
					if (mediaPrintableArea != null) {
						if (mediaPrintableArea.getX(MediaPrintableArea.INCH) + 
								mediaPrintableArea.getWidth(MediaPrintableArea.INCH) <= width) {
							x = mediaPrintableArea.getX(MediaPrintableArea.INCH);
							printableWidth = mediaPrintableArea.getWidth(MediaPrintableArea.INCH);
						}
						if (mediaPrintableArea.getY(MediaPrintableArea.INCH) + 
								mediaPrintableArea.getHeight(MediaPrintableArea.INCH) <= height) {
							y = mediaPrintableArea.getY(MediaPrintableArea.INCH);
							printableHeight = mediaPrintableArea.getHeight(MediaPrintableArea.INCH);
						}
					}
					Paper paper = new Paper();
					paper.setSize(width * 72, height * 72);
					paper.setImageableArea(x * 72, y * 72, printableWidth * 72, printableHeight * 72);
					pageFormat = new PageFormat();
					pageFormat.setPaper(paper);
					int pageOrientation = PageFormat.PORTRAIT;
					if (orientation.equals(OrientationRequested.LANDSCAPE)) {
						pageOrientation = PageFormat.LANDSCAPE;
					} else if (orientation.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
						pageOrientation = PageFormat.REVERSE_LANDSCAPE;
					}
					pageFormat.setOrientation(pageOrientation);
				}
			}
		}
		return pageFormat;
	}
	
	/**
	 * Creates or return a page format.
	 * @param pageFormat Page format to use as default.
	 * @param printRequestAttributes Print request attributes.
	 * @return A most appropriate page format. Never null.
	 */
	public PageFormat getPageFormat(PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) {
		PageFormat returnValue = getPageFormat(printRequestAttributes);
		if (returnValue == null) {
			returnValue = pageFormat;
		}
		if (returnValue == null) {
			returnValue = new PageFormat();
		}
		return returnValue;
	}
	
}
