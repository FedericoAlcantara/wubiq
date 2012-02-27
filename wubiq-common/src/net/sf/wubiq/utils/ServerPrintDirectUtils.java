/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Prints the pdf file directly to print service.
 * Now is deprecated. Instead use the remote print service as intended from any application using wubiq.<br/>
 * For example:<br/>
 * Doc doc = new SimpleDoc(PdfUtils.INSTANCE.pdfToPageable(printDocument), DocFlavor.SERVICE_FORMATTED.PAGEABLE, newAttributes);<br/>
 * DocPrintJob printJob = printService.createPrintJob();<br/>
 * printJob.print(doc, requestAttributes);<br/>
 * <br/>
 * @author Federico Alcantara
 *
 */
@Deprecated
public enum ServerPrintDirectUtils {
	INSTANCE;
	
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
			DocAttributeSet attributes = PrintServiceUtils.createDocAttributes(printAttributes);
			// Set Request Attributes
			PrintRequestAttributeSet requestAttributes = PrintServiceUtils.createPrintRequestAttributes(printAttributes);
			requestAttributes.add(new JobName(jobId, Locale.getDefault()));
			
			// Create doc and printJob
			DocAttributeSet newAttributes = attributes;
			Doc doc = new SimpleDoc(PdfUtils.INSTANCE.pdfToPageable(printDocument), DocFlavor.SERVICE_FORMATTED.PAGEABLE, newAttributes);
			DocPrintJob printJob = printService.createPrintJob();
			printJob.print(doc, requestAttributes);
			printDocument.close();
			if (PrintServiceUtils.isRemotePrintService(printService)) {
				try {
					Method getUuid = printService.getClass().getDeclaredMethod("getUuid", new Class[]{});
					String uuid = (String)getUuid.invoke(printService, new Object[]{});
					IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
					manager.addRemotePrintJob(uuid, (RemotePrintJob)printJob);
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
			}
		} catch (PrintException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
