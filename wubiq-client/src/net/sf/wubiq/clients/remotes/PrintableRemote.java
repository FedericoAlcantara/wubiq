package net.sf.wubiq.clients.remotes;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.proxies.ProxyClientMaster;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.PageFormatWrapper;

/**
 * Wraps a printable object into a serializable class.
 * This object stores the commands sent to a graphic recorder.
 * Because it is serialized, the commands can later be de-serialized
 * into this object.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintableRemote implements Printable, IProxyClient {
	
	public static final String[] FILTERED_METHODS = new String[]{
		"print"
	};

	public PrintableRemote() {
		initialize();
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
		GraphicsRemote remote = (GraphicsRemote)Enhancer.create(GraphicsRemote.class, 
				new ProxyClientMaster(
						jobId(),
						manager(),
						graphics,
						GraphicsRemote.FILTERED_METHODS));
		return (Integer) manager().readFromRemote(new RemoteCommand(objectUUID(),
				"print",
				new GraphicParameter(PageFormatWrapper.class, pageFormat),
				new GraphicParameter(int.class, pageIndex),
				new GraphicParameter(UUID.class, remote.objectUUID())));
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
	 * IProxyClient interface implementation
	 * *****************************************
	 */
	@Override
	public DirectPrintManager manager() {
		// TODO Auto-generated method stub
		return null;
	}
}
