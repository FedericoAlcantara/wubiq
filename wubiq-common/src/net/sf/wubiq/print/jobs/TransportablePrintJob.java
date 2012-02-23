/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.io.IOException;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;

/**
 * @author Federico Alcantara
 *
 */
public class TransportablePrintJob implements DocPrintJob {
	private PrintJobAttributeSet printJobAttributeSet;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private PrintService printService;
	private Doc doc;
	
	public TransportablePrintJob() {
	}

	public TransportablePrintJob(PrintService printService) {
		this.printService = printService;
	}

	/**
	 * @see javax.print.DocPrintJob#addPrintJobAttributeListener(javax.print.event.PrintJobAttributeListener, javax.print.attribute.PrintJobAttributeSet)
	 */
	@Override
	public void addPrintJobAttributeListener(PrintJobAttributeListener arg0,
			PrintJobAttributeSet arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#addPrintJobListener(javax.print.event.PrintJobListener)
	 */
	@Override
	public void addPrintJobListener(PrintJobListener arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#getAttributes()
	 */
	@Override
	public PrintJobAttributeSet getAttributes() {
		return printJobAttributeSet;
	}

	/**
	 * @see javax.print.DocPrintJob#getPrintService()
	 */
	@Override
	public PrintService getPrintService() {
		return printService;
	}

	/**
	 * @see javax.print.DocPrintJob#print(javax.print.Doc, javax.print.attribute.PrintRequestAttributeSet)
	 */
	@Override
	public void print(Doc doc, PrintRequestAttributeSet printRequestAttributeSet)
			throws PrintException {
		this.doc = doc;
		this.printRequestAttributeSet = printRequestAttributeSet;
	}

	/**
	 * @see javax.print.DocPrintJob#removePrintJobAttributeListener(javax.print.event.PrintJobAttributeListener)
	 */
	@Override
	public void removePrintJobAttributeListener(PrintJobAttributeListener arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.DocPrintJob#removePrintJobListener(javax.print.event.PrintJobListener)
	 */
	@Override
	public void removePrintJobListener(PrintJobListener arg0) {
		throw new UnsupportedOperationException();
	}

	public Object getPrintData() throws IOException {
		return doc.getPrintData();
	}
	
	public DocFlavor getDocFlavor() {
		return doc.getDocFlavor();
	}
	
	public DocAttributeSet getDocAttributeSet() {
		return doc.getAttributes();
	}
	/**
	 * @return the printRequestAttributeSet
	 */
	public PrintRequestAttributeSet getPrintRequestAttributeSet() {
		return printRequestAttributeSet;
	}

}
