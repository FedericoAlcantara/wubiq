/**
 * 
 */
package net.sf.wubiq.interfaces;

import net.sf.wubiq.enums.NotificationType;

/**
 * Listens to remote communication.
 * @author Federico Alcantara
 *
 */
public interface IRemoteListener {
	/**
	 * Receives a notification about a problem.
	 * @param queueId Queue id associated with the problem.
	 * @param message Message of the notification.
	 */
	public void notify(String queueId, NotificationType notificationType, String message);
	
}
