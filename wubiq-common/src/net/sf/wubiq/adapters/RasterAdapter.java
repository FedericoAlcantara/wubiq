/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.image.Raster;
import java.util.Set;
import java.util.UUID;

import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * @author Federico Alcantara
 *
 */
public class RasterAdapter extends Raster implements IAdapter, IProxyMaster {

	public RasterAdapter() {
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
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
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
