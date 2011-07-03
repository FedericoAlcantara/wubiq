/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.impl.PrintJobInputStream;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.utils.PrintServiceUtils;

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
	public static void print(String jobId, String printServiceName, Collection<Attribute> printAttributes, InputStream printDocument)  throws IOException {
		try {
			PrintService printService = PrintServiceUtils.findPrinterOrDefault(printServiceName);
			if (printService == null) {
				throw new IOException(("error.print.noPrintDevice"));
			}
			if (printService instanceof RemotePrintService) {
				RemotePrintService remotePrintService = (RemotePrintService)printService;
				IRemotePrintJob remotePrintJob = new PrintJobInputStream(remotePrintService.getRemoteName(), printDocument, printAttributes);
				IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
				manager.addRemotePrintJob(remotePrintService.getUuid(), remotePrintJob);
			} else {
				// Set Document Attributes
				DocAttributeSet attributes = PrintServiceUtils.createDocAttributes(printAttributes);
				// Set Request Attributes
				PrintRequestAttributeSet requestAttributes = PrintServiceUtils.createPrintRequestAttributes(printAttributes);
				requestAttributes.add(new JobName(jobId, Locale.getDefault()));
				
				// Create doc and printJob
				Doc doc = new SimpleDoc(printDocument, DocFlavor.INPUT_STREAM.AUTOSENSE, attributes);
				DocPrintJob printJob = printService.createPrintJob();
	
				printJob.print(doc, requestAttributes);
			}
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
