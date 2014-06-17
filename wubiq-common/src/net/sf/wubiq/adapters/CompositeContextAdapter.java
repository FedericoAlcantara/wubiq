/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
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
public class CompositeContextAdapter implements CompositeContext, IAdapter, IProxy {
	public static final String[] FILTERED_METHODS = new String[]{
		"compose"
	};
	
	public CompositeContextAdapter() {
		initialize();
	}
	
	@Override
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
		RasterAdapter srcAdapter = (RasterAdapter)
				Enhancer.create(RasterAdapter.class,
						new ProxyAdapterMaster(
								queue(),
								src,
								RasterAdapter.FILTERED_METHODS));

		RasterAdapter dstInAdapter = (RasterAdapter)
				Enhancer.create(RasterAdapter.class,
						new ProxyAdapterMaster(
								queue(),
								dstIn,
								RasterAdapter.FILTERED_METHODS));
		WritableRasterAdapter dstOutAdapter = (WritableRasterAdapter)
				Enhancer.create(WritableRasterAdapter.class,
						new ProxyAdapterMaster(
								queue(),
								dstOut,
								WritableRasterAdapter.FILTERED_METHODS));
		queue().sendCommand(new RemoteCommand(objectUUID(), "composeRemote",
				new GraphicParameter(UUID.class, srcAdapter.objectUUID()),
				new GraphicParameter(UUID.class, dstInAdapter.objectUUID()),
				new GraphicParameter(UUID.class, dstOutAdapter.objectUUID())));
		queue().returnData();
	}

	@Override
	public void dispose() {
	}

	
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


}
