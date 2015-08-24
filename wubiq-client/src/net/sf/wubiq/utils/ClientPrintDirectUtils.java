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
import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableStreamWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableStreamWrapper;
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
						doc = new SimpleDoc(enablePageableStream(formattedPageable, printRequestAttributeSet), DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
					} else if (docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
						PrintableWrapper printable = (PrintableWrapper) input.readObject();
						doc = new SimpleDoc(enablePrintableStream(printable, printRequestAttributeSet), DocFlavor.SERVICE_FORMATTED.PRINTABLE, new HashDocAttributeSet());
					}
				} else {
					doc = new SimpleDoc(printData, docFlavor, docAttributeSet);
				}
				DocPrintJob printJob = printService.createPrintJob();
				if (printJob instanceof RemotePrintJob) {
					((RemotePrintJob)printJob).setAttributes(printJobAttributeSet);
				}
				printJob.print(doc, printRequestAttributeSet);
			}
				
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		} catch (ClassNotFoundException e) {
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
	

		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPageable(enablePageableStream(pageable, printRequestAttributeSet));
		try {
			printerJob.setPrintService(printService);
			printerJob.print(filteredRequestAttributeSet);
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
	 * Enables the print to stream.
	 * @param originalPageable Original pageable.
	 * @param printRequestAttributeSet Print request attribute set.
	 * @return A Pageable capable of streaming or not.
	 */
	private static Pageable enablePageableStream(Pageable originalPageable,
			PrintRequestAttributeSet printRequestAttributeSet) {
		Pageable toPrintPageable = originalPageable;
		String printerUrl = printerUrl(printRequestAttributeSet);
		if (printerUrl != null) {
			toPrintPageable = new PageableStreamWrapper(originalPageable, printerUrl);
		}
		return toPrintPageable;
	}
	
	/**
	 * Enables the print to stream.
	 * @param originalPrintable Original printable.
	 * @param printRequestAttributeSet Print request attribute set.
	 * @return A Printable capable of streaming or not.
	 */
	private static Printable enablePrintableStream(Printable originalPrintable,
			PrintRequestAttributeSet printRequestAttributeSet) {
		Printable toPrintPrintable = originalPrintable;
		String printerUrl = printerUrl(printRequestAttributeSet);
		if (printerUrl != null) {
			toPrintPrintable = new PrintableStreamWrapper(originalPrintable, printerUrl);
		}
		return toPrintPrintable;
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
		
		Doc doc = new SimpleDoc(enablePageableStream(pageable, printRequestAttributeSet), DocFlavor.SERVICE_FORMATTED.PAGEABLE, new HashDocAttributeSet());
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