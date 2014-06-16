/**
 * 
 */
package net.sf.wubiq.adapters;

import java.awt.font.FontRenderContext;
import java.util.Set;
import java.util.UUID;

import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Establish and manages the communication between the server and the client at printable level.
 * @author Federico Alcantara
 *
 */
public class FontRenderContextAdapter extends FontRenderContext 
		implements IAdapter {
	private IDirectConnectorQueue queue;
	
	private UUID objectUUID;
	
	public FontRenderContextAdapter(IDirectConnectorQueue queue, UUID objectUUID) {
		this.objectUUID = objectUUID;
		this.queue = queue;
		queue.registerObject(objectUUID, this);
	}
	
	
	
	/* *****************************************
	 * IRemoteAdapter interface implementation
	 * *****************************************
	 */
	
	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return queue;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
		queue.addListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return queue.removeListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#listeners()
	 */
	public Set<IRemoteListener> listeners() {
		return queue.listeners();
	}
	
	/* **************************************
	 * SUPPORT ROUTINES
	 * *************************************
	 */

	/**
	 * @return The invoking method name.
	 */
	private String methodName() {
		if (Thread.currentThread().getStackTrace().length >= 3) {
			return Thread.currentThread().getStackTrace()[2].getMethodName();
		} else { 
			return null;
		}
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

	
}
