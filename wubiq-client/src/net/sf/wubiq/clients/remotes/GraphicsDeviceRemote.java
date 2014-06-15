/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IRemoteClientMaster;

/**
 * Creates a remote handler for GraphicsDevice.
 * @author Federico Alcantara
 *
 */
public class GraphicsDeviceRemote extends GraphicsDevice implements IRemoteClientMaster {
	private DirectPrintManager manager;
	private GraphicsDevice graphicsDevice;
	private UUID objectUUID;
	
	public static final String[] FILTERED_METHODS = new String[]{};
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClient#initialize()
	 */
	public void initialize() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClientMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return graphicsDevice;
	}
	
	public void setGraphicsDevice(GraphicsDevice graphicsDevice) {
		this.graphicsDevice = graphicsDevice;
	}

	@Override
	public GraphicsConfiguration[] getConfigurations() {
		return null;
	}

	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return null;
	}

	@Override
	public String getIDstring() {
		return null;
	}

	@Override
	public int getType() {
		return 0;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IClientRemote#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}

}
