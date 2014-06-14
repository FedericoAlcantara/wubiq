/**
 * 
 */
package net.sf.wubiq.interfaces;

import java.util.UUID;

/**
 * Client Remote interface
 * @author Federico Alcantara
 *
 */
public interface IRemoteClient {
	/**
	 * Unique identification of the object instance.
	 * @return UUID of the object
	 */
	UUID getObjectUUID();
}
