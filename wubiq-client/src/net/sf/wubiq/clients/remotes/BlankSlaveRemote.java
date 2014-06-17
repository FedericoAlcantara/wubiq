/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;

/**
 * @author Federico Alcantara
 *
 */
public class BlankSlaveRemote implements IProxyClient {

	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	public BlankSlaveRemote() {
		initialize();
	}
	
	/* ***************************
	 * Proxied methods
	 * ***************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyClient#manager()
	 */
	public DirectPrintManager manager() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}

}
