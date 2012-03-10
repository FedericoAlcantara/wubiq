/**
 * 
 */
package net.sf.wubiq.print.jobs;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.wrappers.PageFormatWrapper;
import net.sf.wubiq.wrappers.PageableWrapper;
import net.sf.wubiq.wrappers.PrintableWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class RemotePrintJob implements DocPrintJob {
	private static final Log LOG = LogFactory.getLog(RemotePrintJob.class);
	private PrintJobAttributeSet printJobAttributeSet;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private DocAttributeSet docAttributeSet;
	private DocFlavor docFlavor;
	private PrintService printService;
	private String printServiceName;
	private RemotePrintJobStatus status;
	private Object printData;
	private PageFormat pageFormat;
	
	public RemotePrintJob() {
	}

	public RemotePrintJob(PrintService printService) {
		this.printService = printService;
		status = RemotePrintJobStatus.NOT_PRINTED;
		Method getRemoteName;
		try {
			getRemoteName = printService.getClass().getDeclaredMethod("getRemoteName", new Class[]{});
			printServiceName = (String)getRemoteName.invoke(printService, new Object[]{});
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
		update(doc, printRequestAttributeSet);
		try {			
			Method getUuid = printService.getClass().getDeclaredMethod("getUuid", new Class[]{});
			String uuid = (String)getUuid.invoke(printService, new Object[]{});
			
			IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
			manager.addRemotePrintJob(uuid, this);
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
	}

	/**
	 * Updates this element with the doc information.
	 * @param doc Document to be printed
	 * @param printRequestAttributeSet Request attribute.
	 * @throws PrintException
	 */
	public void update(Doc doc, PrintRequestAttributeSet printRequestAttributeSet)
			throws PrintException {
		this.printRequestAttributeSet = printRequestAttributeSet;
		this.docAttributeSet = doc.getAttributes();
		this.docFlavor = doc.getDocFlavor();
		try {
			this.printData = doc.getPrintData();
			printData = getStreamForBytes();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		
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
	
	/**
	 * Sets a new PrintJob attributeSet.
	 * @param printJobAttributeSet
	 */
	public void setAttributes(PrintJobAttributeSet printJobAttributeSet) {
		this.printJobAttributeSet = printJobAttributeSet;
	}
	
	public void setPrintData(Object printData) {
		this.printData = printData;
	}
	
	/**
	 * 
	 * @return A Reader for the input data.
	 * @throws IOException
	 */
	public Object getPrintData() throws IOException {
		return printData;
	}
	
	public synchronized InputStream getStreamForBytes() throws IOException {
		InputStream returnValue = null;
		if (printData instanceof InputStream) {
			returnValue = (InputStream)printData;
		} else if (printData instanceof Reader) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int byteVal = -1;
			while((byteVal = ((Reader)printData).read()) > -1) {
				output.write(byteVal);
			}
			returnValue = new ByteArrayInputStream(output.toByteArray());
			output.close();
		} else if (printData instanceof Pageable) {
			returnValue = serializePageable((Pageable)printData);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
		} else if (printData instanceof Printable) {
			returnValue = serializePrintable((Printable)printData);
			docFlavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		}
		
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @return Input stream representing the serialized pageable.
	 */
	private InputStream serializePageable(Pageable inputPageable) {
		InputStream returnValue = null;
		PageableWrapper pageable = new PageableWrapper(inputPageable);
		int pageResult = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		do {
			try {
				PageFormatWrapper pageFormat = new PageFormatWrapper(pageable.getOriginal().getPageFormat(pageIndex));
				PrintableWrapper printable = new PrintableWrapper(pageable.getOriginal().getPrintable(pageIndex));
				pageResult = printPrintable(printable, pageFormat, pageIndex);
				if (pageResult == Printable.PAGE_EXISTS) {
					pageable.addPageFormat(pageFormat);
					pageable.addPrintable(printable);
					pageIndex++;
				}
			} catch (IndexOutOfBoundsException e) {
				LOG.debug("Reached end of printables");
				break;
			}
		} while (pageResult == Printable.PAGE_EXISTS);
		pageable.setNumberOfPages(pageIndex);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream output;
		try {
			output = new ObjectOutputStream(out);
			output.writeObject(pageable);
			returnValue = new ByteArrayInputStream(out.toByteArray());
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Serialize a pageable and produce a input stream.
	 * @param inputPageable Pageable to serialize.
	 * @return Input stream representing the serialized pageable.
	 */
	private InputStream serializePrintable(Printable inputPrintable) {
		InputStream returnValue = null;
		PrintableWrapper printable = new PrintableWrapper(inputPrintable);
		try {
			printPrintable(printable, new PageFormatWrapper(pageFormat), 0);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(out);
			output.writeObject(printable);
			returnValue = new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Perform a printable print method and conserves the graphic command list.
	 * @param printable Printable to print.
	 * @param pageFormat Page format.
	 * @param pageIndex Page to be printed.
	 */
	private int printPrintable(Printable printable, PageFormat pageFormat, int pageIndex) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		try {
			returnValue = ((PrintableWrapper)printable).print(graph, pageFormat, pageIndex, 1.0, 1.0);
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

	/**
	 * Sets the document flavor.
	 * @param docFlavor New document flavor to set.
	 */
	public void setDocFlavor(DocFlavor docFlavor){
		this.docFlavor = docFlavor;
	}
	
	/**
	 * 
	 * @return The original doc flavor for the print data.
	 */
	public DocFlavor getDocFlavor() {
		return docFlavor;
	}
	
	/**
	 * 
	 * @return The original attribute set for the document.
	 */
	public DocAttributeSet getDocAttributeSet() {
		return docAttributeSet;
	}
	
	/**
	 * @return The original print request attribute set for the print action.
	 */
	public PrintRequestAttributeSet getPrintRequestAttributeSet() {
		return printRequestAttributeSet;
	}
	
	/**
	 * 
	 * @return The underlying print service name.
	 */
	public String getPrintServiceName() {
		return printServiceName;
	}

	/**
	 * @return Current print job status
	 */
	public RemotePrintJobStatus getStatus() {
		return status;
	}

	/**
	 * Sets a new status for the print job
	 * @param status the status to set.
	 */
	public void setStatus(RemotePrintJobStatus status) {
		this.status = status;
	}

	/**
	 * @return the pageFormat
	 */
	public PageFormat getPageFormat() {
		if (pageFormat == null) {
			Paper paper = new Paper();
			paper.setSize(612, 828);
			paper.setImageableArea(0, 0, 612, 828);
			pageFormat = new PageFormat();
			pageFormat.setOrientation(PageFormat.PORTRAIT);
			pageFormat.setPaper(paper);
		}
		return pageFormat;
	}

	/**
	 * @param pageFormat the pageFormat to set
	 */
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
}
