/**
 * 
 */
package net.sf.wubiq.interfaces;

import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Minimal interfaces for proxied server classes (adapters).
 * @author Federico Alcantara
 *
 */
public interface IProxyAdapter {
	
	/**
	 * Associated connector queue.
	 * @return Queue connector.
	 */
	IDirectConnectorQueue queue();
}
