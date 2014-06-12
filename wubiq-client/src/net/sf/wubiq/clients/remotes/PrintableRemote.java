package net.sf.wubiq.clients.remotes;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import net.sf.wubiq.clients.DirectPrintManager;

/**
 * Wraps a printable object into a serializable class.
 * This object stores the commands sent to a graphic recorder.
 * Because it is serialized, the commands can later be deserialized
 * into this object.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintableRemote implements Printable {
	private int returnValue;
	DirectPrintManager manager;
	
	public PrintableRemote(DirectPrintManager manager) {
		this.manager = manager;
	}

	/**
	 * This a two stage print method. 
	 * First it records graphics command into itself by using a GraphicRecorder object.
	 * After it is deserialized, then it sends the previously saved command to the
	 * graphics provided by the local printer object.
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		return returnValue;
	}
	
	public void setReturnValue(int returnValue) {
		this.returnValue = returnValue;
	}
	
}
