/**
 * 
 */
package net.sf.wubiq.interfaces;

/**
 * The minimal interface for objects which have its states
 * remotely (slave).
 * @author Federico Alcantara
 *
 */
public interface IProxySlave {
	/**
	 * Performs the initialization of the client.
	 * Must be called before any other methods.
	 */
	void initialize();

}
