package net.sf.wubiq.servlets;

import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.InputStream;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.servlet.ServletException;

import net.sf.wubiq.print.jobs.PrinterJobManager;
import net.sf.wubiq.print.pdf.PdfPageable;
import net.sf.wubiq.utils.PdfUtils;

import org.apache.pdfbox.pdmodel.PDDocument;

public class ATest {

	public static void main(String[] args) throws Exception {
		ATest test = new ATest();
		test.printLongDocument();
	}
	
	private void printLongDocument() throws Exception {
		PrintService printService = null;
		for (PrintService readPrintService : PrintServiceLookup.lookupPrintServices(null, null)) {
			if ("CUPS-PDF".equals(readPrintService.getName())) {
				printService = readPrintService;
			}
		}
		System.out.println(printService);
		
		InputStream input = this.getClass().getClassLoader().getResourceAsStream("net/sf/wubiq/reports/" + "TestPage-50x2.pdf");
		DocAttributeSet docAttributes = new HashDocAttributeSet();
		PrintRequestAttributeSet requestAttributes = new HashPrintRequestAttributeSet();
		requestAttributes.add(new JobName("Test page", Locale.getDefault()));
		requestAttributes.add(MediaSizeName.NA_LETTER);
		requestAttributes.add(new Copies(1));

		docPrintJob(printService, input, docAttributes, requestAttributes);
		//printerJob(printService, input, docAttributes, requestAttributes);
		input.close();
	}
	
	private void docPrintJob(PrintService printService, InputStream input, DocAttributeSet docAttributes, 
			PrintRequestAttributeSet requestAttributes) throws Exception {
		Doc doc = new SimpleDoc(input, DocFlavor.INPUT_STREAM.PDF, docAttributes);
		DocPrintJob printJob = printService.createPrintJob();
		try {
			printJob.print(doc, requestAttributes);
		} catch (PrintException e) {
			e.printStackTrace();
		}
	}
	
	private void printerJob(PrintService printService, InputStream input, DocAttributeSet docAttributes, 
			PrintRequestAttributeSet requestAttributes) throws Exception {
		//PrinterJobManager.initializePrinterJobManager();
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		Pageable pageable;
		try {
			pageable = PdfUtils.INSTANCE.pdfToPageable(input, printerJob);
			synchronized(pageable) {
				printerJob.setPageable(pageable);
				try {
					printerJob.setPrintService(printService);
					printerJob.print(requestAttributes);
				} catch (PrinterException e) {
					e.printStackTrace();
				} finally {
					if (pageable != null) {
						if (pageable instanceof PDDocument) {
							((PDDocument)pageable).close();
						} else if (pageable instanceof PdfPageable) {
							((PdfPageable)pageable).close();
						}
					}
				}
			}
		} catch (PrintException e) {
			throw new ServletException(e);
		}

	}

}
