package net.sf.wubiq.utils;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

public class VectorGraphicsPdfUtils {
	public static void main(String[] args) throws Exception {
		VectorGraphicsPdfUtils a = new VectorGraphicsPdfUtils();
		String testPage = "net/sf/wubiq/reports/TestPage.pdf";
		String outputPdf = "/Users/federico/Downloads/0000-test.pdf";
		PrintService printService = PrintServiceUtils.findPrinter("Print to VipRiser");
		InputStream input = a.getClass().getClassLoader().getResourceAsStream(testPage);
		FileOutputStream output = new FileOutputStream(outputPdf);
		PrintRequestAttributeSet requestAttributes = new HashPrintRequestAttributeSet();
		requestAttributes.add(new JobName("Test page", Locale.getDefault()));
		requestAttributes.add(MediaSizeName.NA_LETTER);
		requestAttributes.add(new Copies(1));
		//requestAttributes.add(OrientationRequested.LANDSCAPE);
		Pageable pageable = PdfUtils.INSTANCE.pdfToPageable(input, printService, requestAttributes);

		/* Pageable */
		/*
		PageableUtils.INSTANCE.pageableToPdf(pageable, output, requestAttributes);
		Doc docPageable = new SimpleDoc(pageable, DocFlavor.SERVICE_FORMATTED.PAGEABLE, null);
		DocPrintJob jobPageable = printService.createPrintJob();
		jobPageable.print(docPageable, requestAttributes);
		*/
		
		/* Printable */
		Printable printable = pageable.getPrintable(0);
		PageableUtils.INSTANCE.printableToPdf(printable, output, requestAttributes);
		Doc docPrintable = new SimpleDoc(printable, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
		DocPrintJob jobPrintable = printService.createPrintJob();
		jobPrintable.print(docPrintable, requestAttributes);
	}
	
}
