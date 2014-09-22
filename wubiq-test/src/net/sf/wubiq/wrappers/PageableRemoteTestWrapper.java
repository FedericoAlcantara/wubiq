/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.Printable;
import java.io.Serializable;

import net.sf.wubiq.clients.remotes.PageableRemote;
import net.sf.wubiq.clients.remotes.PrintableChunkRemote;

/**
 * @author Federico Alcantara
 *
 */
public class PageableRemoteTestWrapper extends PageableRemote implements Serializable {
	private static final long serialVersionUID = 1L;
		
	private LocalManagerTestWrapper testManager;

	@Override
	public int getNumberOfPages() {
		int returnValue = super.getNumberOfPages();
		testManager.getTestData().setDirectPageableNumberOfPages(returnValue);
		return returnValue;
	}
	
	@Override
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		Printable returnValue = super.getPrintable(pageIndex);
		((PrintableChunkRemoteTestWrapper)returnValue).setTestManager(testManager);
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.clients.remotes.PageableRemote#getPrintableClass()
	 */
	@Override
	protected Class<? extends PrintableChunkRemote> getPrintableClass() {
		return PrintableChunkRemoteTestWrapper.class;
	}
	
	@Override
	protected String[] getPrintableFilteredMethods() {
		return PrintableChunkRemoteTestWrapper.FILTERED_METHODS();
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
		String[] returnValue = new String[PageableRemote.FILTERED_METHODS.length + 3];
		int count;
		for (count = 0; count < PageableRemote.FILTERED_METHODS.length; count++) {
			returnValue[count] = PageableRemote.FILTERED_METHODS[count];
		}
		returnValue[count++] = "FILTERED_METHODS";
		returnValue[count++] = "getTestManager";
		returnValue[count++] = "setTestManager";
		
		return returnValue;
	}

}
