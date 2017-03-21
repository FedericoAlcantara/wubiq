/**
 * 
 */
package net.sf.wubiq.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.wrappers.GraphicCommand;
import net.sf.wubiq.wrappers.PrintableWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class JpaPrintable extends PrintableWrapper implements Printable, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(JpaPrintable.class);
	
	private PageFormat pageFormat;
	private Set<GraphicCommand> commands;
	
	public JpaPrintable() {
	}
	
	public JpaPrintable(PageFormat pageFormat, Set<GraphicCommand> commands) {
		this();
		this.pageFormat = pageFormat;
		this.commands = commands;
	}
	
	@Override
	public int print(Graphics graphic, PageFormat pageablePageFormat, int pageIndex)
			throws PrinterException {
		int returnValue = Printable.NO_SUCH_PAGE;
		if (commands != null && !commands.isEmpty()) {
			returnValue = Printable.PAGE_EXISTS;
			Graphics2D graph = (Graphics2D)graphic;
			long start = new Date().getTime();
			printerType = PrintServiceUtils.printerType(graph.getDeviceConfiguration().getDevice().getIDstring());
			Point2D scaleValue = GraphicsUtils.INSTANCE.scaleGraphics(graph, pageFormat, false);
			executeGraphics(graph, pageFormat, scaleValue.getX(), scaleValue.getY(), pageIndex);
			LOG.debug("Page " + pageIndex + " printing took:" + (new Date().getTime() - start) + "ms");
		}
		return returnValue;
	}

	@Override
	protected Set<GraphicCommand> getGraphicCommands(int pageIndex) {
		return commands;
	}
}
