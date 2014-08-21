/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.io.Serializable;
import java.util.Set;

import net.sf.wubiq.clients.remotes.PrintableChunkRemote;

/**
 * @author Federico Alcantara
 *
 */
public class PrintableChunkRemoteTestWrapper extends PrintableChunkRemote implements Serializable {
	private static final long serialVersionUID = 1L;

	private LocalManagerTestWrapper testManager;
	
	@Override
	protected void executeGraphics(Graphics2D graph, PageFormat pageFormat,
			Set<GraphicCommand> graphicCommands) {
		super.executeGraphics(graph, pageFormat, graphicCommands);
		testManager.getTestData().setDirectPrintableGraphicsCommandCount(graphicCommands.size());
	}
	
	/**
	 * @return the testManager
	 */
	public LocalManagerTestWrapper getTestManager() {
		return testManager;
	}

	/**
	 * @param testManager the testManager to set
	 */
	public void setTestManager(LocalManagerTestWrapper manager) {
		this.testManager = manager;
	}
	
	/**
	 * Provides the methods to be ignored by the proxy.
	 * @return List of filtered methods.
	 */
	public static String[] FILTERED_METHODS() {
		String[] returnValue = new String[PrintableChunkRemote.FILTERED_METHODS.length + 3];
		int count;
		for (count = 0; count < PrintableChunkRemote.FILTERED_METHODS.length; count++) {
			returnValue[count] = PrintableChunkRemote.FILTERED_METHODS[count];
		}
		returnValue[count++] = "FILTERED_METHODS";
		returnValue[count++] = "getTestManager";
		returnValue[count++] = "setTestManager";
		
		return returnValue;
	}
	
}
