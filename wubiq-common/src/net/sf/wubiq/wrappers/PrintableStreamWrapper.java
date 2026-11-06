/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.Iterator;

import javax.print.PrintException;

import net.sf.wubiq.dotmatrix.ESCPrinter;

/**
 * @author Federico Alcantara
 *
 */
public class PrintableStreamWrapper implements Printable {
	private Printable printable;
	private String url;
	
	public PrintableStreamWrapper(Printable printable, String url) {
		this.printable = printable;
		this.url = url;
	}
	
	@Override
	public int print(Graphics graph, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		Graphics2D graphics = (Graphics2D) graph;
		GraphicsStreamWrapper wrapper = new GraphicsStreamWrapper(graphics);
		int returnValue = printable.print(wrapper, pageFormat, 1);
		try {
			ESCPrinter printer = new ESCPrinter(url, 
					pageFormat.getPaper().getWidth() / 72 * 1440,
					pageFormat.getPaper().getHeight() / 72 * 1440,
					false);
			Iterator<TextField> it = wrapper.getTexts().iterator();
			while (it.hasNext()) {
				TextField textField = it.next();
				printer.advanceTo(textField);
				printer.print(textField);
			}
		} catch (PrintException e) {
			throw new PrinterException(e.getMessage());
		} catch (IOException e) {
			throw new PrinterException(e.getMessage());
		}
		return returnValue;
	}

}
