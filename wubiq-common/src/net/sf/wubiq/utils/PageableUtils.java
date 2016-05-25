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
import java.io.OutputStream;
import java.io.Reader;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;

/**
 * @author Federico Alcantara
 *
 */
public enum PageableUtils {
	INSTANCE;

	public static final Log LOG = LogFactory.getLog(PageableUtils.class);
	
	/**
	 * Converts a print data into a stream.
	 * @param printData Print data to convert.
	 * @param outputStream Recipient of the serialized print data.
	 * @param pageFormat Default page format to use.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 * @throws IOException
	 */
	public void writeToStream(Object printData, OutputStream outputStream, PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) throws IOException {
		if (printData instanceof InputStream) {
			IOUtils.INSTANCE.copy((InputStream)printData, outputStream);
		} else if (printData instanceof Reader) {
			IOUtils.INSTANCE.copy(((Reader)printData), outputStream);
		} else if (printData instanceof Pageable) {
			serializePageableToStream((Pageable)printData, outputStream, printRequestAttributes);
		} else if (printData instanceof Printable) {
			serializePrintableToStream((Printable)printData, outputStream, pageFormat, printRequestAttributes);
		}
	}
	
	/**
	 * Serialize a pageable into an output stream.
	 * @param inputPageable Pageable to serialize.
	 * @param outputStream Recipient of the serialized pageable.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 */
	private void serializePageableToStream(Pageable inputPageable, OutputStream outputStream, PrintRequestAttributeSet printRequestAttributes) {
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
		
		ObjectOutputStream output;
		try {
			output = new ObjectOutputStream(outputStream);
			output.writeObject(pageable);
			output.flush();
			output.close();
			outputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Serialize a printable into an output stream.
	 * @param inputPrintable Printable to serialize.
	 * @param outputStream Recipient of the serialized printable.
	 * @param pageFormat Default page format to use.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 */
	private void serializePrintableToStream(Printable inputPrintable, OutputStream outputStream, PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) {
		PrintableWrapper printable = new PrintableWrapper(inputPrintable);
		printable.setNotSerialized(true);
		try {
			int result = Printable.PAGE_EXISTS;
			int pageIndex = 0;
			PageFormatWrapper printablePageFormat = new PageFormatWrapper(getPageFormat(pageFormat, printRequestAttributes));
			do {
				result = printPrintable(printable, printablePageFormat, pageIndex);
				pageIndex++;
			} while (result == Printable.PAGE_EXISTS);
			ObjectOutputStream output = new ObjectOutputStream(outputStream);
			output.writeObject(printable);
			output.flush();
			output.close();
			outputStream.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Converts a print data into a stream.
	 * @param printData Print data to convert.
	 * @param pageFormat Default page format to use.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 * @return InputStream representing the print data as it should be printed.
	 * @throws IOException
	 */
	public InputStream getStreamForBytes(Object printData,
			PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) throws IOException {
		InputStream returnValue = null;
		if (printData instanceof InputStream) {
			returnValue = (InputStream)printData;
		} else if (printData instanceof Reader) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.INSTANCE.copy(((Reader)printData), output);
			returnValue = new ByteArrayInputStream(output.toByteArray());
			output.close();
		} else if (printData instanceof Pageable) {
			returnValue = serializePageable((Pageable)printData, printRequestAttributes);
		} else if (printData instanceof Printable) {
			returnValue = serializePrintable((Printable)printData, pageFormat, printRequestAttributes);
		}
		
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 * @return InputStream representing the print data as serialized Pageable.
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
	 * Serialize a printable and produce a input stream.
	 * @param inputPrintable Printable to serialize.
	 * @param pageFormat Default page format to use.
	 * @param printRequestAttributes Print request attributes for formatting the print output.
	 * @return Input stream representing the serialized printable.
	 */
	private InputStream serializePrintable(Printable inputPrintable, PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) {
		InputStream returnValue = null;
		PrintableWrapper printable = new PrintableWrapper(inputPrintable);
		printable.setNotSerialized(true);
		try {
			int result = Printable.PAGE_EXISTS;
			int pageIndex = 0;
			PageFormatWrapper printablePageFormat = new PageFormatWrapper(getPageFormat(pageFormat, printRequestAttributes));
			do {
				result = printPrintable(printable, printablePageFormat, pageIndex);
				pageIndex++;
			} while (result == Printable.PAGE_EXISTS);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(out);
			output.writeObject(printable);
			output.flush();
			output.close();
			out.close();
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
			} else {
				Paper paper = new Paper();
				paper.setSize(612, 828);
				paper.setImageableArea(0, 0, 612, 828);
				pageFormat = new PageFormat();
				pageFormat.setOrientation(PageFormat.PORTRAIT);
				pageFormat.setPaper(paper);
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
	
	public void printPageableToStream(Pageable pageable) {
		
	}
	
	/**
	 * Creates a pdf from a pageable.
	 * @param pageable Pageable to be converted.
	 * @param outputStream Output stream.
	 * @param printRequestAttributes Print request attributes.
	 */
	public void pageableToPdf(Pageable pageable, OutputStream outputStream, PrintRequestAttributeSet printRequestAttributes) {
		int pageResult = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		
		int dpi = ServerProperties.INSTANCE.getPdfToImageDotsPerInch();
		PDDocument document = new PDDocument();
		do {
			try {
				Printable printable = pageable.getPrintable(pageIndex);
				PageFormat pageFormat = pageable.getPageFormat(pageIndex);

				preparePageFormatAndAttributes(pageFormat, printRequestAttributes);

				pageResult = addPrintableToPdf(printable, pageFormat, pageIndex, dpi, document);
				if (pageResult == Printable.PAGE_EXISTS) {
					pageIndex++;
				}
			} catch (IndexOutOfBoundsException e) {
				LOG.debug("Reached end of printables");
				break;
			}
		} while (pageResult == Printable.PAGE_EXISTS);
		try {
			if (document != null) {
				document.save(outputStream);
				document.close();
			}
			outputStream.flush();
			outputStream.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (COSVisitorException e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * Creates a pdf from a printable.
	 * @param printable Printable to be converted.
	 * @param outputStream Output stream.
	 * @param printRequestAttributes Print request attributes.
	 */
	public void printableToPdf(Printable printable, OutputStream outputStream, PrintRequestAttributeSet printRequestAttributes) {
		int dpi = ServerProperties.INSTANCE.getPdfToImageDotsPerInch();
		PDDocument document = new PDDocument();
		PageFormat pageFormat = getPageFormat(printRequestAttributes);
		preparePageFormatAndAttributes(pageFormat, printRequestAttributes);
		addPrintableToPdf(printable, pageFormat, 0, dpi, document);
		try {
			if (document != null) {
				document.save(outputStream);
			}
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (COSVisitorException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Prepares the page format and the print request attribute set for pageables / printables proper creation.
	 * @param pageFormat Current page format (will be updated).
	 * @param printRequestAttributes Print request attribute set to be updated.
	 */
	private void preparePageFormatAndAttributes(PageFormat pageFormat, PrintRequestAttributeSet printRequestAttributes) {
		Paper paper = pageFormat.getPaper();
		MediaSizeName mediaName = (MediaSizeName) printRequestAttributes.get(MediaSizeName.class);
		if (mediaName == null) {
			mediaName = (MediaSizeName) printRequestAttributes.get(Media.class);
		}
		MediaPrintableArea printableArea = (MediaPrintableArea) printRequestAttributes.get(MediaPrintableArea.class);
		OrientationRequested orientation = (OrientationRequested) printRequestAttributes.get(OrientationRequested.class);
		if (mediaName != null) {
			MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaName);
			double x = 0d;
			double y = 0d;
			double width = (double)mediaSize.getX(MediaSize.INCH) * 72f;
			double height = (double)mediaSize.getY(MediaSize.INCH) * 72f;
			paper = pageFormat.getPaper();
			paper.setImageableArea(x, y, width, height);
			pageFormat.setPaper(paper);
		}
		
		if (OrientationRequested.LANDSCAPE.equals(orientation)) {
			pageFormat.setOrientation(PageFormat.LANDSCAPE);
		} else if (OrientationRequested.REVERSE_LANDSCAPE.equals(orientation)) {
			pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
		} else if (OrientationRequested.PORTRAIT.equals(orientation) ||
				OrientationRequested.REVERSE_PORTRAIT.equals(orientation)) {
			pageFormat.setOrientation(PageFormat.PORTRAIT);
		}
		
		// Without a MediaPrintableArea, the output is shifted out of its default margins.
		if (printableArea == null) {
			float x = (float)pageFormat.getImageableX() / 72f;
			float y = (float)pageFormat.getImageableY() / 72f;
			float width = (float)pageFormat.getWidth() / 72f;
			float height = (float)pageFormat.getHeight() /72f;
			// We ask inverse as we are taking the values from an already rotated paper bounds.
			if (((OrientationRequested.LANDSCAPE.equals(orientation)
					|| OrientationRequested.REVERSE_LANDSCAPE.equals(orientation))
					&& width > height) 
					||
				((OrientationRequested.PORTRAIT.equals(orientation)
					|| OrientationRequested.REVERSE_PORTRAIT.equals(orientation))
					&& height > width)
					) {
				printableArea = new MediaPrintableArea(y, x, height, width,
						MediaPrintableArea.INCH);
			} else {
				printableArea = new MediaPrintableArea(x, y, width, height,
						MediaPrintableArea.INCH);
			}

			printRequestAttributes.add(printableArea);
		}

	}
	
	/**
	 * Outputs a printable to stream as PNG file.
	 * @param printable Printable object.
	 * @param pageFormat Page format.
	 * @param pageIndex Page index.
	 * @param dpi Dots per inches (resolution). Minimal recommended 144.
	 * @param output Output stream to put the png.
	 * @return Status of printable.
	 */
	private int addPrintableToPdf(Printable printable, PageFormat pageFormat, int pageIndex, double dpi, PDDocument document) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		double resolution = dpi / 72d;
		int width = new Double(pageFormat.getWidth() * resolution).intValue();
		int height = new Double(pageFormat.getHeight() * resolution).intValue();
		float x = (float) (pageFormat.getImageableX());
		float y = (float) (pageFormat.getImageableY());
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		try {
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.scale(resolution, resolution);
			graph.setTransform(scaleTransform);
			graph.translate(x, y);
			graph.setClip(new Rectangle2D.Double(
					0,
					0,
					pageFormat.getPaper().getImageableWidth(), 
					pageFormat.getPaper().getImageableHeight()));
			graph.setBackground(Color.WHITE);
			graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
					(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
			returnValue = printable.print(graph, pageFormat, pageIndex);
			if (Printable.PAGE_EXISTS == returnValue) {
				PDRectangle mediaBox = new PDRectangle((float)pageFormat.getWidth(), (float)pageFormat.getHeight());
				PDPage page = new PDPage(mediaBox);
				document.addPage(page);
				PDXObjectImage pdImage = new PDJpeg(document, img, 0.99999f);
				//PDXObjectImage pdImage = new PDPixelMap(document, img);
				PDPageContentStream contents = new PDPageContentStream(document, page);
				contents.drawXObject(pdImage, 0, 0, new Double(pageFormat.getWidth()).intValue(),
						new Double(pageFormat.getHeight()).intValue());
				contents.close();
			}
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}


	/**
	 * Outputs a printable to stream as PNG file.
	 * @param printable Printable object.
	 * @param pageFormat Page format.
	 * @param pageIndex Page index.
	 * @param dpi Dots per inches (resolution). Minimal recommended 144.
	 * @param output Output stream to put the png.
	 * @return Status of printable.
	 */
/*
	private int addPrintableToPdf-PDFBOX2(Printable printable, PageFormat pageFormat, int pageIndex, double dpi, PDDocument document) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		double resolution = dpi / 72d;
		int width = new Double(pageFormat.getWidth() * resolution).intValue();
		int height = new Double(pageFormat.getHeight() * resolution).intValue();
		float x = (float) (pageFormat.getImageableX());
		float y = (float) (pageFormat.getImageableY());
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graph = new GraphicsPdfRecorder(img.createGraphics());
		try {
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.scale(resolution, resolution);
			graph.setTransform(scaleTransform);
			graph.translate(x, y);
			graph.setClip(new Rectangle2D.Double(
					0,
					0,
					pageFormat.getPaper().getImageableWidth(), 
					pageFormat.getPaper().getImageableHeight()));
			graph.setBackground(Color.WHITE);
			graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
					(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
			returnValue = printable.print(graph, pageFormat, pageIndex);
			if (Printable.PAGE_EXISTS == returnValue) {
				PDRectangle mediaBox = new PDRectangle((float)pageFormat.getWidth(), (float)pageFormat.getHeight());
				PDPage page = new PDPage(mediaBox);
				document.addPage(page);
				PDImageXObject pdImage = LosslessFactory.createFromImage(document, img);
				PDPageContentStream contents = new PDPageContentStream(document, page);
				contents.drawImage(pdImage, 0, 0,
						new Double(pageFormat.getWidth()).intValue(),
						new Double(pageFormat.getHeight()).intValue());
				contents.close();
			}
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}
*/

}
