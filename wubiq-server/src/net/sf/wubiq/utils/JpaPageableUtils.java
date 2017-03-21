/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Set;
import java.util.TreeSet;

import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.dao.WubiqPrintJobPageDao;
import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.data.WubiqPrintJobPage;
import net.sf.wubiq.print.JpaPageable;
import net.sf.wubiq.wrappers.GraphicCommand;
import net.sf.wubiq.wrappers.GraphicsRecorder;
import net.sf.wubiq.wrappers.PageFormatWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum JpaPageableUtils {
	INSTANCE;
	private final Log LOG = LogFactory.getLog(JpaPageableUtils.class);
	
	/**
	 * Persist pageable object. Serialize pageable and then persist it to.
	 * @param job Print job.
	 * @param pageable Pageable value.
	 * @param printRequestAttributes Print request attributes.
	 * @return Serializable pageable.
	 */
	public JpaPageable persistPageable(WubiqPrintJob job, Pageable pageable, PrintRequestAttributeSet printRequestAttributes) {
		JpaPageable returnValue = new JpaPageable();
		int pageResult = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		
		do {
			try {
				Printable printable = pageable.getPrintable(pageIndex);
				PageFormat pageFormat = new PageFormatWrapper(pageable.getPageFormat(pageIndex));

				PageableUtils.INSTANCE.preparePageFormatAndAttributes(pageFormat, printRequestAttributes);

				Set<GraphicCommand> commands = new TreeSet<GraphicCommand>();
				pageResult = addPrintableToJpa(printable, pageFormat, pageIndex, commands);
				if (pageResult == Printable.PAGE_EXISTS) {
					WubiqPrintJobPage page = WubiqPrintJobPageDao.INSTANCE.save(job, pageFormat, pageIndex, commands);
					returnValue.add(page.getId(), pageFormat);
					pageIndex++;
				}
			} catch (IndexOutOfBoundsException e) {
				LOG.debug("Reached end of printables");
				break;
			}
		} while (pageResult == Printable.PAGE_EXISTS);
		return returnValue;
	}
	
	/**
	 * Persist a printable object.
	 * @param job Print job id.
	 * @param printable Printable to persist.
	 * @param printRequestAttributes Print request attribute.
	 * @return Created entity id.
	 */
	public Long persistPrintable(WubiqPrintJob job, Printable printable, PrintRequestAttributeSet printRequestAttributes) {
		Long returnValue = null;
		int pageResult = Printable.PAGE_EXISTS;
		PageFormat pageFormat = new PageFormatWrapper(PageableUtils.INSTANCE.getPageFormat(printRequestAttributes));
		PageableUtils.INSTANCE.preparePageFormatAndAttributes(pageFormat, printRequestAttributes);
		Set<GraphicCommand> commands = new TreeSet<GraphicCommand>();
		pageResult = addPrintableToJpa(printable, pageFormat, 0, commands);
		if (pageResult == Printable.PAGE_EXISTS) {
			WubiqPrintJobPage page = WubiqPrintJobPageDao.INSTANCE.save(job, pageFormat, 0, commands);
			returnValue = page.getId();
		}
		return returnValue;
	}
	
	/**
	 * Outputs a printable to stream as PNG file.
	 * @param printable Printable object.
	 * @param pageFormat Page format.
	 * @param pageIndex Page index.
	 * @param dpi Dots per inches (resolution). Minimal recommended 144.
	 * @param output Output stream to put the png.
	 * @return Status of printable.
	 */
	private int addPrintableToJpa(Printable printable, PageFormat pageFormat, int pageIndex, Set<GraphicCommand> commands) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		double resolution = 1d;
		int width = new Double(pageFormat.getWidth() * resolution).intValue();
		int height = new Double(pageFormat.getHeight() * resolution).intValue();
		float x = (float) (pageFormat.getImageableX());
		float y = (float) (pageFormat.getImageableY());
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graph = new GraphicsRecorder(commands, img.createGraphics());
		try {
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.scale(resolution, resolution);
			graph.setTransform(scaleTransform);
			graph.translate(x, y);
			graph.setClip(new Rectangle2D.Double(
					0,
					0,
					pageFormat.getPaper().getImageableWidth(), 
					pageFormat.getPaper().getImageableHeight()));
			graph.setBackground(Color.WHITE);
			graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
					(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
			returnValue = printable.print(graph, pageFormat, pageIndex);
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

}
