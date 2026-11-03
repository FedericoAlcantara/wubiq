/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Destination;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import net.sf.wubiq.clients.remotes.PageablePrintableHolder;
import net.sf.wubiq.clients.remotes.PageableRemote;
import net.sf.wubiq.print.attribute.CustomMediaSizeName;
import net.sf.wubiq.print.attribute.OriginalOrientationRequested;
import net.sf.wubiq.wrappers.EscpStreamJob;
import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sends the document object directly to print service.
 * @author Federico Alcantara
 *
 */
public final class ClientPrintDirectUtils {
	private static Log LOG = LogFactory.getLog(ClientPrintDirectUtils.class);
	
	/**
	 * Sends the input stream file with the given preferences to the print service.
	 * @param jobId Identifying job id.
	 * @param printService PrintService to print to.
	 * @param printRequestAttributeSet Attributes to be set on the print service.
	 * @param printJobAttributeSet Attributes for the print job.
	 * @param docAttributeSet Attributes for the document.
	 * @param docFlavor Document flavor.
	 * @param printData Document as input stream to sent to the print service.
	 * @throws IOException if service is not found and no default service.
	 */
	public static void print(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			DocFlavor docFlavor,
			InputStream printData)  throws IOException {
		try {
			if (printService == null) {
				throw new IOException(("error.print.noPrintDevice"));
			}
			if (printData != null) {
				Doc doc = null;
				setJobName(jobId, printRequestAttributeSet, printJobAttributeSet);
				// Create doc and printJob
				if (docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) ||
						docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
					ObjectInputStream input = new ObjectInputStream(printData);
					if (docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE)) {
						PageableWrapper pageable = (PageableWrapper) input.readObject();
						PageableWrapper formattedPageable = new PageableWrapper();
						formattedPageable.setNotSerialized(false);
						for (int index = 0; index < pageable.getNumberOfPages(); index++) {
							PrintableWrapper printable = (PrintableWrapper)pageable.getPrintable(index);
							PageFormat originalPageFormat = pageable.getPageFormat(index);
							Paper originalPaper = originalPageFormat.getPaper();
							PageFormat newPageFormat = new PageFormat();
							newPageFormat.setOrientation(originalPageFormat.getOrientation());
							Paper newPaper = new Paper();
							newPaper.setSize(originalPaper.getWidth(), originalPaper.getHeight());
							newPaper.setImageableArea(originalPaper.getImageableX(), 
									originalPaper.getImageableY(), 
									originalPaper.getImageableWidth(), 
									originalPaper.getHeight());
							newPageFormat.setPaper(newPaper);
							PageFormat formattedPageFormat = PageableUtils.INSTANCE.getPageFormat(newPageFormat, printRequestAttributeSet);
							formattedPageable.addPageFormat(new PageFormatWrapper(formattedPageFormat));
							formattedPageable.addPrintable(printable);
						}
						formattedPageable.setNumberOfPages(pageable.getNumberOfPages());
						if (printToStream(formattedPageable, printRequestAttributeSet, printService)) {
							return;
						}
						doc = new SimpleDoc(formattedPageable, DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
					} else if (docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
						PrintableWrapper printable = (PrintableWrapper) input.readObject();
						if (printerUrl(printRequestAttributeSet) != null) {
							PageablePrintableHolder holder = new PageablePrintableHolder(printable);
							holder.setPageFormat(getPageFormat(printService, filterPrintRequestAttributeSet(printRequestAttributeSet)));
							if (printToStream(holder, printRequestAttributeSet, printService)) {
								return;
							}
						}
						doc = new SimpleDoc(printable, DocFlavor.SERVICE_FORMATTED.PRINTABLE, new HashDocAttributeSet());
					}
				} else {
					doc = new SimpleDoc(printData, docFlavor, docAttributeSet);
				}
				DocPrintJob printJob = printService.createPrintJob();
				printJob.print(doc, printRequestAttributeSet);
			}
				
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage(), e);
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Prints a pageable object.
	 * @param jobId Id of the object.
	 * @param printService Print service.
	 * @param printRequestAttributeSet Print request Attribute set.
	 * @param printJobAttributeSet Print Job attribute set.
	 * @param docAttributeSet Document attribute set.
	 * @param pageable Pageable.
	 */
	public static void printPageable(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			Pageable pageable) throws PrinterException {
		PrintRequestAttributeSet filteredRequestAttributeSet =
				filterPrintRequestAttributeSet(printRequestAttributeSet);
		setJobName(jobId, filteredRequestAttributeSet, printJobAttributeSet);

		PageFormat pageFormat = getPageFormat(printService, filteredRequestAttributeSet);
		if (pageable instanceof PageableRemote) {
			PageableRemote remote = (PageableRemote)pageable;
			remote.setPageFormat(pageFormat);
		}
	
		if (printToStream(pageable, printRequestAttributeSet, printService)) {
			return;
		}

		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPageable(pageable);
		try {
			printerJob.setPrintService(printService);
			if (isMacOs() && carriedByPageFormat(filteredRequestAttributeSet)) {
				// macOS Java 8 builds before 8u201 move the page whenever print() receives attributes (JDK-8201818).
				JobName jobName = (JobName) filteredRequestAttributeSet.get(JobName.class);
				if (jobName != null) {
					printerJob.setJobName(jobName.getValue());
				}
				Copies copies = (Copies) filteredRequestAttributeSet.get(Copies.class);
				if (copies != null) {
					printerJob.setCopies(copies.getValue());
				}
				printerJob.print();
			} else {
				printerJob.print(filteredRequestAttributeSet);
			}
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
		}
		
		/*
		Doc doc = new SimpleDoc(pageable, DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
		DocPrintJob printJob = printService.createPrintJob();
		try {
			printJob.print(doc, printRequestAttributeSet);
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
		*/
	}
	
	/**
	 * Writes the document as an ESC/P stream to the request's destination (the fast dot-matrix path),
	 * instead of printing it through the printer driver.
	 * @param pageable Document to write.
	 * @param printRequestAttributeSet Attributes of the request: destination, copies and paper.
	 * @param printService Print service of the printer.
	 * @return True if the request has a destination and the document was written (or failed trying, which is logged); false if the request is for the printer driver.
	 */
	private static boolean printToStream(Pageable pageable, PrintRequestAttributeSet printRequestAttributeSet,
			PrintService printService) {
		String printerUrl = printerUrl(printRequestAttributeSet);
		if (printerUrl == null) {
			return false;
		}
		float[] printable = printableOrigin(printService, printRequestAttributeSet);
		Copies copies = (Copies) printRequestAttributeSet.get(Copies.class);
		try {
			new EscpStreamJob(printerUrl, pageable.getNumberOfPages(), printable[0], printable[1])
					.printAll(pageable, copies == null ? 1 : copies.getValue());
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
		}
		return true;
	}
	
	/**
	 * Where the printer's printable area starts for the job's paper: the position its driver counts
	 * from, and so what the ESC/P stream's positions have to be relative to to land where the driver puts them.
	 * @param printService Print service of the printer.
	 * @param printRequestAttributeSet Attributes of the request, with the paper (Media) of the job.
	 * @return Left and top of the printable area, in points from the paper's edges; NaN, NaN if the service doesn't say.
	 */
	private static float[] printableOrigin(PrintService printService, PrintRequestAttributeSet printRequestAttributeSet) {
		float[] returnValue = new float[] {Float.NaN, Float.NaN};
		try {
			PrintRequestAttributeSet paper = new HashPrintRequestAttributeSet();
			Media media = (Media) printRequestAttributeSet.get(Media.class);
			if (media != null) {
				paper.add(media);
			}
			Object values = printService.getSupportedAttributeValues(MediaPrintableArea.class, DocFlavor.SERVICE_FORMATTED.PAGEABLE, paper);
			if (values instanceof MediaPrintableArea[] && ((MediaPrintableArea[]) values).length > 0) {
				MediaPrintableArea area = ((MediaPrintableArea[]) values)[0];
				returnValue[0] = area.getX(MediaPrintableArea.INCH) * 72;
				returnValue[1] = area.getY(MediaPrintableArea.INCH) * 72;
			}
		} catch (RuntimeException e) {
			LOG.debug("The printable area of the printer isn't known: " + e.getMessage());
		}
		return returnValue;
	}
	
	/**
	 * Converts the url to the proper format.
	 * @param printRequestAttributeSet Attribute set containing the url.
	 * @return String with the printer url or null if none found.
	 */
	private static String printerUrl(PrintRequestAttributeSet printRequestAttributeSet) {
		String printerUrl = null;
		if (printRequestAttributeSet.containsKey(Destination.class)) {
			Destination uri = (Destination) printRequestAttributeSet.get(Destination.class);
			printerUrl = uri.getURI().toASCIIString();
		}
		return printerUrl;
	}

	/**
	 * Prints a printable object.
	 * @param jobId Id of the object.
	 * @param printService Print service.
	 * @param printRequestAttributeSet Print request Attribute set.
	 * @param printJobAttributeSet Print Job attribute set.
	 * @param docAttributeSet Document attribute set.
	 * @param printable Printable.
	 */
	public static void printPrintable(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			Printable printable) throws PrinterException {
		PrintRequestAttributeSet filteredRequestAttributeSet =
				filterPrintRequestAttributeSet(printRequestAttributeSet);
		setJobName(jobId, filteredRequestAttributeSet, printJobAttributeSet);

		PageFormat pageFormat = getPageFormat(printService, filteredRequestAttributeSet);
		PageablePrintableHolder pageable = new PageablePrintableHolder(printable);
		pageable.setPageFormat(pageFormat);
		/*
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPrintable(printable);
		try {
			printerJob.setPrintService(printService);
			printerJob.print(printRequestAttributeSet);
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
		}
		*/
		
		if (printToStream(pageable, printRequestAttributeSet, printService)) {
			return;
		}
		Doc doc = new SimpleDoc(pageable, DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
		DocPrintJob printJob = printService.createPrintJob();
		try {
			printJob.print(doc, filteredRequestAttributeSet);
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	/**
	 * The page format to use. It invokes a printer job which normally talks to print devices through native calls.
	 * @param printService Associated print service.
	 * @param printRequestAttributeSet Print request attribute set.
	 * @return A page format. Never null.
	 * @throws PrinterException Thrown if print service is not found.
	 */
	private static PageFormat getPageFormat(PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet) throws PrinterException {
		PageFormat returnValue = new PageFormat();
		PageFormat pageFormat = new PageFormat();
		Paper paper = new Paper();
		boolean paperSet = false;
		MediaSizeName mediaSizeName = (MediaSizeName) printRequestAttributeSet.get(MediaSizeName.class);
		if (mediaSizeName == null) {
			mediaSizeName = (MediaSizeName) printRequestAttributeSet.get(Media.class);
		}
		if (mediaSizeName != null) {
			if (mediaSizeName instanceof CustomMediaSizeName) {
				CustomMediaSizeName customMedia = (CustomMediaSizeName)mediaSizeName;
				PrintServiceUtils.findMedia(customMedia.getX(), customMedia.getY(), customMedia.getUnits()); // To enable it.
			}
			MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaSizeName);
			if (mediaSize != null) {
				paper.setSize(Math.round(mediaSize.getX(MediaSize.INCH) * 72), 
						(Math.round(mediaSize.getY(MediaSize.INCH) * 72)));
				paper.setImageableArea(0, 0, mediaSize.getX(MediaSize.INCH) * 72,
						mediaSize.getY(MediaSize.INCH) * 72);
				pageFormat.setPaper(paper);
				paperSet = true;
			}
		}
		PrinterJob printerJob = null;
		try {
			if (!paperSet) {
				printerJob = PrinterJob.getPrinterJob();
				printerJob.setPrintService(printService);
				pageFormat = printerJob.getPageFormat(printRequestAttributeSet);
			}
			// It might be an already left shifted by a previous pdf conversion.
			double x = 0;
			double y = 0;

			MediaPrintableArea mediaPrintableArea = (MediaPrintableArea) printRequestAttributeSet.get(MediaPrintableArea.class);
			if (mediaPrintableArea != null) {
				x = x + (mediaPrintableArea.getX(MediaPrintableArea.INCH) * 72);
				y = y + (mediaPrintableArea.getY(MediaPrintableArea.INCH) * 72);
			}
			paper.setImageableArea(x, y, pageFormat.getWidth() - x , pageFormat.getHeight() - y);
			double width = pageFormat.getWidth();
			double height = pageFormat.getHeight();
			OriginalOrientationRequested originalOrientation = (OriginalOrientationRequested) printRequestAttributeSet.get(OriginalOrientationRequested.class);
			if (originalOrientation != null) {
				if (originalOrientation == OriginalOrientationRequested.REVERSE_PORTRAIT) {
					pageFormat.setOrientation(PageFormat.PORTRAIT);
				} else if (originalOrientation == OriginalOrientationRequested.REVERSE_LANDSCAPE) {
					pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
				} else if (originalOrientation == OriginalOrientationRequested.PORTRAIT){
					pageFormat.setOrientation(PageFormat.PORTRAIT);
				} else {
					pageFormat.setOrientation(PageFormat.LANDSCAPE);
				}
				if (!System.getProperty("os.name").toLowerCase().contains("win")) {
					// We must rotate the paper size as it was previously changed.
					paper.setSize(height, width);
				}
			} else {
				OrientationRequested orientation = (OrientationRequested) printRequestAttributeSet.get(OrientationRequested.class);
				if (orientation != null) {
					if (orientation == OrientationRequested.REVERSE_PORTRAIT) {
						pageFormat.setOrientation(PageFormat.PORTRAIT);
					} else if (orientation == OrientationRequested.REVERSE_LANDSCAPE) {
						pageFormat.setOrientation(PageFormat.REVERSE_LANDSCAPE);
					} else if (orientation == OrientationRequested.PORTRAIT){
						pageFormat.setOrientation(PageFormat.PORTRAIT);
					} else {
						pageFormat.setOrientation(PageFormat.LANDSCAPE);
					}
				}
			}
			pageFormat.setPaper(paper);
			returnValue = pageFormat;
		} finally {
			if (printerJob != null) {
				printerJob.cancel();
			}
		}
		return returnValue;
	}
	
	/**
	 * Creates the attribute for the job name.
	 * @param jobId Id of the print job.
	 * @param printRequestAttributeSet Print request attribute set.
	 * @param printJobAttributeSet Print job attribute set.
	 */
	private static void setJobName(String jobId, PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet) {
		JobName jobName = (JobName)PrintServiceUtils.findCategoryAttribute(printRequestAttributeSet, JobName.class);
		if (jobName == null) {
			jobName = (JobName)PrintServiceUtils.findCategoryAttribute(printJobAttributeSet, JobName.class);
		}
		StringBuffer newJobName = new StringBuffer("Remote_")
				.append(jobId);
		if (jobName != null) {
			newJobName.append('_')
				.append(jobName.getValue());
		}
		printRequestAttributeSet.add(new JobName(newJobName.toString(), Locale.getDefault()));
	}
	
	private static boolean isMacOs() {
		return System.getProperty("os.name").toLowerCase().startsWith("mac");
	}

	/**
	 * @param printRequestAttributeSet Print request attribute set.
	 * @return True if every attribute is either in the page format (media, printable area, orientation)
	 * or settable on the printer job (job name, copies).
	 */
	private static boolean carriedByPageFormat(PrintRequestAttributeSet printRequestAttributeSet) {
		for (Attribute attribute : printRequestAttributeSet.toArray()) {
			if (!(attribute instanceof Media
					|| attribute instanceof MediaPrintableArea
					|| attribute instanceof OrientationRequested
					|| attribute instanceof OriginalOrientationRequested
					|| attribute instanceof JobName
					|| attribute instanceof Copies)) {
				return false;
			}
		}
		return true;
	}

	private static PrintRequestAttributeSet filterPrintRequestAttributeSet(PrintRequestAttributeSet input) {
		PrintRequestAttributeSet returnValue = new HashPrintRequestAttributeSet();
		for (Attribute attribute : input.toArray()) {
			if (attribute instanceof CustomMediaSizeName) {
				CustomMediaSizeName media = (CustomMediaSizeName)attribute;
				MediaSizeName mediaSizeName = PrintServiceUtils.findMedia(media.getX(), media.getY(), media.getUnits());
				returnValue.add(mediaSizeName);
			} else {
				returnValue.add(attribute);
			}
		}
		return returnValue;
	}
	
}