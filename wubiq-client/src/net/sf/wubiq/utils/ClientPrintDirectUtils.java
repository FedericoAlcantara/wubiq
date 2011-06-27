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

import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Prints the pdf file directly to print service.
 * @author Federico Alcantara
 *
 */
public final class ClientPrintDirectUtils {
	private static Log LOG = LogFactory.getLog(ClientPrintDirectUtils.class);
	
	/**
	 * Prints the pdf file with the given preference.
	 * @param file pdf file.
	 * @param preference preference information.
	 * @throws IOException if service is not found and no default service.
	 */
	public static void printPdf(String jobId, String printServiceName, Collection<Attribute> allAttributes, InputStream jreport)  throws IOException {
		try {
			PrintService printService = PrintServiceUtils.findPrinterOrDefault(printServiceName);
			if (printService == null) {
				throw new IOException(("error.print.noPrintDevice"));
			}
			// Set Document Attributes
			DocAttributeSet attributes = PrintServiceUtils.createDocAttributes(allAttributes);
			// Set Request Attributes
			PrintRequestAttributeSet requestAttributes = PrintServiceUtils.createPrintRequestAttributes(allAttributes);
			requestAttributes.add(new JobName(jobId, Locale.getDefault()));
			
			// Create doc and printJob
			Doc doc = new SimpleDoc(jreport, DocFlavor.INPUT_STREAM.PDF, attributes);
			DocPrintJob printJob = printService.createPrintJob();

			printJob.print(doc, requestAttributes);
				
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
