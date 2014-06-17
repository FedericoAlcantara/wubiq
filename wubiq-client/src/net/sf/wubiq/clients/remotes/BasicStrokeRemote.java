/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.BasicStroke;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;

/**
 * Graphics Adapter for communicating with remote.
 * @author Federico Alcantara
 *
 */
public class BasicStrokeRemote extends BasicStroke implements IProxyClient, IProxyMaster {
	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	public BasicStrokeRemote() {
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
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}

}
