/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import net.sf.wubiq.utils.GraphicsUtils;

/**
 * Represents a Printable wrapper for handling direct printing
 * and fix some printers misbehaviors. 
 * @author Federico Alcantara
 *
 */
public class PrintableDirectWrapper implements Printable {
	private Printable printable;
	
	public PrintableDirectWrapper(Printable printable) {
		this.printable = printable;
	}

	/**
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics directGraphics = new GraphicsDirectWrapper((Graphics2D)graphics);
		GraphicsUtils.INSTANCE.scaleGraphics((Graphics2D)directGraphics, pageFormat, true);
		((Graphics2D) directGraphics).translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		return printable.print(directGraphics, pageFormat, pageIndex);
	}

}
