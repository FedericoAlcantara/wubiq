/**
 * 
 */
package net.sf.wubiq.interfaces;

import net.sf.wubiq.clients.DirectPrintManager;

/**
 * Defined the minimal interface for a client.
 * It applies to both master and slave roles.
 * @author Federico Alcantara
 *
 */
public interface IProxyClient extends IProxy {
	/**
	 * Returns the manager in charge of the communication.
	 * @return Saved manager instance.
	 */
	DirectPrintManager manager();

}
