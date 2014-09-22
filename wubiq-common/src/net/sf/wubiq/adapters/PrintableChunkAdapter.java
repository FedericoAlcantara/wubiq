/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.print.attribute.HashPrintRequestAttributeSet;

import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.utils.PageableUtils;
import net.sf.wubiq.wrappers.GraphicCommand;
import net.sf.wubiq.wrappers.GraphicsChunkRecorder;
import net.sf.wubiq.wrappers.PageFormatWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class PrintableChunkAdapter implements Printable, IAdapter, IProxyMaster {
	private static final Log LOG = LogFactory.getLog(PrintableChunkAdapter.class);
	public static final String[] FILTERED_METHODS = new String[]{
		"printable",
		"print",
		"graphicCommands",
		"endPrintable",
		"graphicCommands",
		"setPageable",
		"setPageFormat",
		"getPageFormat"
	};

	private static GraphicsChunkRecorder graphicsRecorder = new GraphicsChunkRecorder();
	private static Map<Integer, Set<GraphicCommand>> graphicCommands = new TreeMap<Integer, Set<GraphicCommand>>();
	private Pageable pageable;
	private PageFormat pageFormat;
	
	public PrintableChunkAdapter() {
		initialize();
	}
	
	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}
	
	public void setPageFormat(PageFormat pageFormat) {
		this.pageFormat = pageFormat;
	}
	
	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		graphicCommands.put(pageIndex, new TreeSet<GraphicCommand>());
		graphicsRecorder.initialize(graphicCommands.get(pageIndex), (Graphics2D)graphics);
		return printable().print(graphicsRecorder, pageFormat, pageIndex);
	}
	
	/**
	 * Starts the collection of graphics commands.
	 * @param pageIndex Page index.
	 * @throws PrinterException
	 */
	/*
	public int print(int pageIndex) throws PrinterException {
		long start = new Date().getTime();
		PageFormat pageFormat = getPageFormat(pageIndex);
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		AffineTransform scaleTransform = new AffineTransform();
		scaleTransform.scale(1, 1);
		graph.setTransform(scaleTransform);
		graph.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		graph.setClip(new Rectangle2D.Double(
				0,
				0,
				pageFormat.getPaper().getImageableWidth(), 
				pageFormat.getPaper().getImageableHeight()));
		graph.setBackground(Color.WHITE);
		graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
				(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
		int returnValue = print(graph, pageFormat, pageIndex);
		LOG.debug("Page " + pageIndex + " generation took:" + (new Date().getTime() - start) + "ms");
		return returnValue;
	}
	*/

	/**
	 * Starts the printing process. In reality it starts the collection of graphics commands.
	 * @param pageIndex Page index for the printable.
	 * @param pageFormat Format of the page.
	 * @param transform Initial transform.
	 * @param backgroundColor Background color.
	 * @param font Initial font.
	 * @return Printable.PAGE_EXISTS if the page is rendered successfully or Printable.NO_SUCH_PAGE if pageIndex specifies a non-existent page.
	 * @throws PrinterException
	 */
	public int print(int pageIndex, PageFormatWrapper pageFormat, 
			AffineTransform transform, 
			Color backgroundColor, Font font) throws PrinterException {
		long start = new Date().getTime();
		BufferedImage img = new BufferedImage((int)Math.round(pageFormat.getWidth()), 
				(int)Math.round(pageFormat.getHeight()), BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D graph = img.createGraphics();
		graph.setTransform(transform);
		graph.setClip(new Rectangle2D.Double(
				0,
				0,
				pageFormat.getWidth(), 
				pageFormat.getHeight()));
		graph.setBackground(backgroundColor);
		graph.clearRect(0,
				0, 
				(int)Math.round(pageFormat.getWidth()), 
				(int)Math.round(pageFormat.getHeight()));
		graph.setFont(font);
		int returnValue = print(graph, pageFormat, pageIndex);
		LOG.debug("Page " + pageIndex + " generation took:" + (new Date().getTime() - start) + "ms");
		return returnValue;
	}
	
	public Set<GraphicCommand> graphicCommands(int pageIndex) {
		return graphicCommands.get(pageIndex);
	}
	
	public Printable printable() {
		return (Printable) decoratedObject();
	}
	
	public PageFormatWrapper getPageFormat(int pageIndex) {
		if (pageable != null) {
			return new PageFormatWrapper(pageable.getPageFormat(pageIndex));
		} else {
			if (pageFormat == null) {
				pageFormat = PageableUtils.INSTANCE.getPageFormat(new HashPrintRequestAttributeSet());
			}
			return new PageFormatWrapper(pageFormat);
		}
	}

	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	@Override
	public void initialize(){
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#jobId()
	 */
	@Override
	public Long jobId() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	@Override
	public UUID objectUUID() {
		return null;
	}
	
	/* *****************************************
	 * IAdapter interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return null;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return false;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#listeners()
	 */
	@Override
	public Set<IRemoteListener> listeners() {
		return null;
	}
	
	/* *****************************************
	 * IProxyMaster interface implementation
	 * *****************************************
	 */

	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	@Override
	public Object decoratedObject() {
		return null;
	}
}
