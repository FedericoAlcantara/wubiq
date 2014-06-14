/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IRemoteClient;

/**
 * Creates a remote handler for GraphicsDevice.
 * @author Federico Alcantara
 *
 */
public class GraphicsDeviceRemote extends GraphicsDevice implements IRemoteClient {
	private DirectPrintManager manager;
	private GraphicsDevice graphicsDevice;
	private UUID objectUUID;
	
	public GraphicsDeviceRemote(DirectPrintManager manager, 
			GraphicsDevice graphicsDevice) {
		this.manager = manager;
		this.graphicsDevice = graphicsDevice;
		objectUUID = UUID.randomUUID();
		this.manager.registerObject(objectUUID, this);
	}

	@Override
	public GraphicsConfiguration[] getConfigurations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		// TODO Auto-generated method stub
		return null;
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
	
	/**
	 * @see net.sf.wubiq.interfaces.IClientRemote#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}

}
