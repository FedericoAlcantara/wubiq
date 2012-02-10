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
 * Sends the document object directly to print service.
 * @author Federico Alcantara
 *
 */
public final class ClientPrintDirectUtils {
	private static Log LOG = LogFactory.getLog(ClientPrintDirectUtils.class);
	
	/**
	 * Sends the input stream file with the given preferences to the print service.
	 * @param jobId Identifying job id.
	 * @param printAttributes Attributes to be set on the print service.
	 * @param printDocument Document as input stream to sent to the print service.
	 * @param serializedDocFlavor contains the expected docflavor for the document.
	 * @throws IOException if service is not found and no default service.
	 */
	public static void print(String jobId, PrintService printService, Collection<Attribute> printAttributes, 
			InputStream printDocument, String serializedDocFlavor)  throws IOException {
		try {
			if (printService == null) {
				throw new IOException(("error.print.noPrintDevice"));
			}
			if (printDocument != null) {
				// Set Document Attributes
				DocAttributeSet attributes = PrintServiceUtils.createDocAttributes(printAttributes);
				// Set Request Attributes
				PrintRequestAttributeSet requestAttributes = PrintServiceUtils.createPrintRequestAttributes(printAttributes);
				requestAttributes.add(new JobName(jobId, Locale.getDefault()));
				
				// Create doc and printJob
				DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(serializedDocFlavor);
				Doc doc = new SimpleDoc(printDocument, docFlavor, attributes);
				DocPrintJob printJob = printService.createPrintJob();
	
				printJob.print(doc, requestAttributes);
			}
				
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
