/**
 * 
 */
package net.sf.wubiq.interfaces;

import java.util.UUID;

/**
 * The minimal interface for the proxies used in communication. This 
 * interface is applicable for both clients and servers in their
 * master and slave roles.
 * @author Federico Alcantara
 *
 */
public interface IProxy {
	/**
	 * Initializes the object.
	 */
	void initialize();

	/**
	 * Unique object identification.
	 * @return The externally assigned object identification.
	 */
	UUID objectUUID();

}
