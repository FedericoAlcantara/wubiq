/**
 * 
 */
package net.sf.wubiq.interfaces;

import java.util.Set;

/**
 * Extends pageable for handling exception listeners.
 * @author Federico Alcantara
 *
 */
public interface IRemoteAdapter {
	
	String queueId();
	
	/**
	 * Adds a listener to the pageable.
	 * @param listener Listener to be added.
	 */
	void addListener(IRemoteListener listener);
	
	/**
	 * Removes a listener from the pageable.
	 * @param listener Listener to be removed.
	 * @return True if the listener was removed.
	 */
	boolean removeListener(IRemoteListener listener);
	
	/**
	 * Gather all the registered listeners.
	 * @return Must return a non-null collection.
	 */
	public Set<IRemoteListener> listeners();
} 
