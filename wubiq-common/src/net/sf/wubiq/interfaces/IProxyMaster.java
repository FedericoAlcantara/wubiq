/**
 * 
 */
package net.sf.wubiq.interfaces;

/**
 * The minimal interface for proxying objects which implement
 * the master role.
 * @author Federico Alcantara
 *
 */
public interface IProxyMaster extends IProxy {
	/**
	 * Must return the object that is being decorated with the remote 
	 * communication object.
	 * @return Decorated object.
	 */
	Object decoratedObject();
	
}
