/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IProxy;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.proxies.ProxyAdapterSlave;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsDeviceAdapter extends GraphicsDevice implements
		IAdapter, IProxy {
	public static final String[] FILTERED_METHODS = new String[]{
		"getConfigurations",
		"getDefaultConfiguration"
	};
	
	public GraphicsDeviceAdapter() {
		initialize();
	}
	
	/**
	 * @see java.awt.GraphicsDevice#getConfigurations()
	 */
	@Override
	public GraphicsConfiguration[] getConfigurations() {
		queue().sendCommand(new RemoteCommand(objectUUID(), "getConfigurationsRemote"));
		UUID[] remoteUUIDS = (UUID[]) queue().returnData();
		GraphicsConfigurationAdapter[] returnValue = 
				new GraphicsConfigurationAdapter[remoteUUIDS.length];
		for (int index = 0; index < returnValue.length; index++) {
			returnValue[index] = (GraphicsConfigurationAdapter)
					Enhancer.create(GraphicsConfigurationAdapter.class,
							new ProxyAdapterSlave(
									queue(),
									remoteUUIDS[index],
									GraphicsConfigurationAdapter.FILTERED_METHODS));
		}
		return returnValue;
	}

	/**
	 * @see java.awt.GraphicsDevice#getDefaultConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		queue().sendCommand(new RemoteCommand(objectUUID(), "getDefaultConfigurationRemote"));
		UUID remoteUUID = (UUID)queue().returnData();
		GraphicsConfigurationAdapter remote = (GraphicsConfigurationAdapter)
				Enhancer.create(GraphicsConfigurationAdapter.class,
				new ProxyAdapterSlave(
						queue(),
						remoteUUID,
						GraphicsConfigurationAdapter.FILTERED_METHODS));
		return remote;
	}

	@Override
	public String getIDstring() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
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
