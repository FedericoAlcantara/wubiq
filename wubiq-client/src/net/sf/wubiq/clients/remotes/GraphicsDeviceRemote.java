/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.proxies.ProxyClientMaster;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * Creates a remote handler for GraphicsDevice.
 * @author Federico Alcantara
 *
 */
public class GraphicsDeviceRemote extends GraphicsDevice implements IProxyClient, IProxyMaster {
	
	public static final String[] FILTERED_METHODS = new String[]{
		"graphicsDevice"
	};
	GraphicsConfigurationRemote[] remotes;
	
	public GraphicsDeviceRemote() {
		initialize();
	}

	/**
	 * @see java.awt.GraphicsDevice#getConfigurations()
	 */
	public GraphicsConfiguration[] getConfigurations() {
		return null;
	}
	
	/**
	 * Create the configurations remote.
	 * @return Configurations unique ids.
	 */
	public UUID[] getConfigurationsRemote() {
		GraphicsConfiguration[] configurations = graphicsDevice().getConfigurations();
		UUID[] uuids = new UUID[configurations.length];
		remotes = new GraphicsConfigurationRemote[uuids.length];
		for (int index = 0; index < uuids.length; index++) {
			remotes[index] = (GraphicsConfigurationRemote) 
					Enhancer.create(GraphicsConfigurationRemote.class,
							new ProxyClientMaster(
									jobId(),
									manager(),
									configurations[index],
									GraphicsConfigurationRemote.FILTERED_METHODS));
			uuids[index] = remotes[index].objectUUID();
		}
		manager().readFromRemote(new RemoteCommand(objectUUID(), "createConfiguration", 
				new GraphicParameter(UUID[].class, uuids)));
		return uuids;
	}

	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return null;
	}
	
	/**
	 * Creates a remote of the default configuration
	 * @return
	 */
	public UUID getDefaultConfigurationRemote() {
		GraphicsConfigurationRemote remote = (GraphicsConfigurationRemote)
				Enhancer.create(GraphicsConfigurationRemote.class,
				new ProxyClientMaster(
						jobId(),
						manager(),
						graphicsDevice().getDefaultConfiguration(),
						GraphicsConfigurationRemote.FILTERED_METHODS));
		return remote.objectUUID();
	}

	@Override
	public String getIDstring() {
		return null;
	}

	@Override
	public int getType() {
		return 0;
	}
	
	private GraphicsDevice graphicsDevice() {
		return (GraphicsDevice) decoratedObject();
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
