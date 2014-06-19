/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;

/**
 * Graphics Adapter for communicating with remote.
 * @author Federico Alcantara
 *
 */
public class BlankMasterRemote implements IProxyClient, IProxyMaster {
	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	public BlankMasterRemote() {
		initialize();
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

	/* *****************************************
	 * IProxyMaster interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	@Override
	public Object decoratedObject() {
		return null;
	}
	

}
