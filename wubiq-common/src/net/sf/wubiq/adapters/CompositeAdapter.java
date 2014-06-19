/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxy;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.proxies.ProxyAdapterMaster;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class CompositeAdapter implements Composite, IProxy, IAdapter {
	
	public static final String[] FILTERED_METHODS = new String[]{
		"createContext"
	};
	
	public CompositeAdapter() {
		initialize();
	}
	
	/**
	 * @see java.awt.Composite#createContext(java.awt.image.ColorModel, java.awt.image.ColorModel, java.awt.RenderingHints)
	 */
	@Override
	public CompositeContext createContext(ColorModel colorModel, ColorModel colorModel2,
			RenderingHints hints) {
		ColorModelAdapter colorModelAdapter = (ColorModelAdapter)
				Enhancer.create(ColorModelAdapter.class,
						new ProxyAdapterMaster(
								jobId(),
								queue(),
								colorModel,
								ColorModelAdapter.FILTERED_METHODS));
		
		ColorModelAdapter colorModelAdapter2 = (ColorModelAdapter)
				Enhancer.create(ColorModelAdapter.class,
						new ProxyAdapterMaster(
								jobId(),
								queue(),
								colorModel2,
								ColorModelAdapter.FILTERED_METHODS));
		
		RenderingHintsAdapter renderingAdapter = (RenderingHintsAdapter)
				Enhancer.create(RenderingHintsAdapter.class,
						new ProxyAdapterMaster(
								jobId(),
								queue(),
								hints,
								RenderingHintsAdapter.FILTERED_METHODS));
		queue().sendCommand(new RemoteCommand(objectUUID(),
				"createContextRemote",
				new GraphicParameter(UUID.class, colorModelAdapter.objectUUID()),
				new GraphicParameter(UUID.class, colorModelAdapter2.objectUUID()),
				new GraphicParameter(UUID.class, renderingAdapter.objectUUID())));
		UUID remoteUUID = (UUID) queue().returnData();
		
		CompositeContextAdapter adapter = (CompositeContextAdapter)
				Enhancer.create(CompositeContextAdapter.class,
						new ProxyAdapterMaster(
								jobId(),
								queue(),
								remoteUUID,
								CompositeContextAdapter.FILTERED_METHODS));
		return adapter;
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
