/**
 * 
 */
package net.sf.wubiq.interfaces;

/**
 * The minimal interface for proxying a object which have the 
 * concrete states (master).
 * @author Federico Alcantara
 *
 */
public interface IProxyMaster {
	/**
	 * Performs the initialization of the client.
	 * Must be called before any other methods.
	 */
	void initialize();

	/**
	 * Must return the object that is being decorated with the remote 
	 * communication object.
	 * @return Decorated object.
	 */
	Object decoratedObject();
	
	/**
	 * Defines the decorated object. This object will receive the 
	 * surrogate calls from the implementation.
	 * @param decoratedObject Object to be decorated.
	 */
	void setDecoratedObject(Object decoratedObject);
}
