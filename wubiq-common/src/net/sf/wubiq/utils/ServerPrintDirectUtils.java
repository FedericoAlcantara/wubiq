/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.impl.PrintJobInputStream;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.print.pdf.PdfImagePage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Prints the pdf file directly to print service.
 * @author Federico Alcantara
 *
 */
public final class ServerPrintDirectUtils {
	private static Log LOG = LogFactory.getLog(ServerPrintDirectUtils.class);
	
	/**
	 * Sends the input stream file with the given preferences to the print service or the remote print service manager.
	 * @param jobId Identifying job id.
	 * @param printAttributes Attributes to be set on the print service.
	 * @param printDocument Document as input stream to sent to the print service.
	 * @throws IOException if service is not found and no default service.
	 */
	public void print(String jobId, String printServiceName, Collection<Attribute> printAttributes, InputStream printDocument)  throws IOException {
		try {
			PrintService printService = PrintServiceUtils.findPrinterOrDefault(printServiceName);
			if (printService == null) {
				throw new IOException(("error.print.noPrintDevice"));
			}
			if (PrintServiceUtils.isRemotePrintService(printService)) {
				try {
					Method getRemoteName = printService.getClass().getDeclaredMethod("getRemoteName", new Class[]{});
					Method getUuid = printService.getClass().getDeclaredMethod("getUuid", new Class[]{});
					String remoteName = (String)getRemoteName.invoke(printService, new Object[]{});
					String uuid = (String)getUuid.invoke(printService, new Object[]{});
					DocFlavor docFlavor = PrintServiceUtils.determineDocFlavor(printService);
					IRemotePrintJob remotePrintJob = new PrintJobInputStream(remoteName, printDocument, printAttributes, docFlavor);
					IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
					manager.addRemotePrintJob(uuid, remotePrintJob);
				} catch (SecurityException e) {
					LOG.error(e.getMessage(), e);
				} catch (NoSuchMethodException e) {
					LOG.error(e.getMessage(), e);
				} catch (IllegalArgumentException e) {
					LOG.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					LOG.error(e.getMessage(), e);
				} catch (InvocationTargetException e) {
					LOG.error(e.getMessage(), e);
				}
			} else {
				// Set Document Attributes
				DocAttributeSet attributes = PrintServiceUtils.createDocAttributes(printAttributes);
				// Set Request Attributes
				PrintRequestAttributeSet requestAttributes = PrintServiceUtils.createPrintRequestAttributes(printAttributes);
				requestAttributes.add(new JobName(jobId, Locale.getDefault()));
				
				// Create doc and printJob
				DocFlavor docFlavor = PrintServiceUtils.determineDocFlavor(printService);
				List<PdfImagePage> documentsToPrint = PrintServiceUtils.convertToProperStream(printDocument, docFlavor);
				for (PdfImagePage pdfImagePage : documentsToPrint) {
					InputStream documentToPrint = new FileInputStream(pdfImagePage.getImageFile());
					DocAttributeSet newAttributes = attributes;
					newAttributes.add(new MediaPrintableArea(0, 0, pdfImagePage.getPageHeight(), 
							pdfImagePage.getPageWidth(), MediaPrintableArea.INCH));
					Doc doc = new SimpleDoc(documentToPrint, docFlavor, attributes);
					DocPrintJob printJob = printService.createPrintJob();
					printJob.print(doc, requestAttributes);
					documentToPrint.close();
				}
			}
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
