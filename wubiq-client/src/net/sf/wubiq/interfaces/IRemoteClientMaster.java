/**
 * 
 */
package net.sf.wubiq.interfaces;

/**
 * @author Federico Alcantara
 *
 */
public interface IRemoteClientMaster extends IRemoteClient {
	/**
	 * Must return the object that is being decorated with the remote 
	 * communication object.
	 * @return Decorated object.
	 */
	Object decoratedObject();
}
