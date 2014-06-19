/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.RenderingHints;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;

/**
 * @author Federico Alcantara
 *
 */
public class RenderingHintsRemote extends RenderingHints implements IProxyClient {

	public RenderingHintsRemote() {
		super(null, null);
		initialize();
	}

	public static final String[] FILTERED_METHODS = new String[]{
	};
	
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
