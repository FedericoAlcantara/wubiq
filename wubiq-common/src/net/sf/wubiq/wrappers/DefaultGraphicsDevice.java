/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

/**
 * @author Federico Alcantara
 *
 */
public class DefaultGraphicsDevice extends GraphicsDevice {
	private transient GraphicsDevice device;
	
	public DefaultGraphicsDevice(GraphicsDevice device) {
		this.device = device;
	}

	/**
	 * @see java.awt.GraphicsDevice#getConfigurations()
	 */
	@Override
	public GraphicsConfiguration[] getConfigurations() {
		return device.getConfigurations();
	}

	/**
	 * @see java.awt.GraphicsDevice#getDefaultConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return device.getDefaultConfiguration();
	}

	/**
	 * @see java.awt.GraphicsDevice#getIDstring()
	 */
	@Override
	public String getIDstring() {
		return device.getIDstring();
	}

	/**
	 * @see java.awt.GraphicsDevice#getType()
	 */
	@Override
	public int getType() {
		return GraphicsDevice.TYPE_PRINTER;
	}

}
