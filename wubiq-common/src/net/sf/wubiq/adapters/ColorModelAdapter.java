/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.image.ColorModel;
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
public class ColorModelAdapter extends ColorModel implements IAdapter,
		IProxyMaster {
	public static final String[] FILTERED_METHODS = new String[]{
		
	};

	public ColorModelAdapter() {
		super(0);
		initialize();
	}

	/**
	 * @see java.awt.image.ColorModel#getAlpha(int)
	 */
	@Override
	public int getAlpha(int arg0) {
		return 0;
	}

	/**
	 * @see java.awt.image.ColorModel#getBlue(int)
	 */
	@Override
	public int getBlue(int arg0) {
		return 0;
	}

	/**
	 * @see java.awt.image.ColorModel#getGreen(int)
	 */
	@Override
	public int getGreen(int arg0) {
		return 0;
	}

	/**
	 * @see java.awt.image.ColorModel#getRed(int)
	 */
	@Override
	public int getRed(int arg0) {
		return 0;
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
