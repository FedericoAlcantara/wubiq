package net.sf.wubiq.clients.remotes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * Wraps a printable object into a serializable class.
 * This object stores the commands sent to a graphic recorder.
 * Because it is serialized, the commands can later be de-serialized
 * into this object.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintableRemote implements Printable {
	DirectPrintManager manager;
	
	public PrintableRemote(DirectPrintManager manager) {
		this.manager = manager;
	}

	/**
	 * This a two stage print method. 
	 * First it records graphics command into itself by using a GraphicRecorder object.
	 * After it is de-serialized, then it sends the previously saved command to the
	 * graphics provided by the local printer object.
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		new GraphicsRemote(manager, (Graphics2D)graphics);
		return (Integer) manager.readFromRemote(new RemoteCommand(RemoteCommandType.PRINTABLE,
				"print",
				new GraphicParameter(PageFormat.class, pageFormat),
				new GraphicParameter(int.class, pageIndex)));
	}
	
	
}
