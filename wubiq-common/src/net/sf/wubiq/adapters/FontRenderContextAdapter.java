/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.font.FontRenderContext;
import java.util.Set;
import java.util.UUID;

import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxy;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class FontRenderContextAdapter extends FontRenderContext 
		implements IAdapter, IProxy {
	
	public static final String[] FILTERED_METHODS = new String[]{
	};	
	
	public FontRenderContextAdapter() {
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
	 * IAdapter interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return null;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return false;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#listeners()
	 */
	@Override
	public Set<IRemoteListener> listeners() {
		return null;
	}
}
