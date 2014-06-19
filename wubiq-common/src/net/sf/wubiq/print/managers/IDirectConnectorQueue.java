/**
 * 
 */
package net.sf.wubiq.print.managers;

import java.util.Collection;
import java.util.UUID;

import net.sf.wubiq.adapters.ReturnedData;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.print.jobs.IRemotePrintJob;

/**
 * Represents the contract for direct connectors.
 * @author Federico Alcantara
 *
 */
public interface IDirectConnectorQueue extends IAdapter {
	String queueId();
	
	/**
	 * Adds a print job to the manager.
	 * @param jobId Id of the job being processed.
	 * @param remotePrintJob Remote print job.
	 */
	void addPrintJob(long jobId, IRemotePrintJob remotePrintJob);
	
	/**
	 * Removes the print job from the queue.
	 * @param jobId Id of the job being processed.
	 * @return True if removed properly.
	 */
	boolean removePrintJob(long jobId);
	
	/**
	 * Returns the associated remote print job.
	 * @param jobId Id of the job being processed.
	 * @return Found remote print job or null.
	 */
	IRemotePrintJob remotePrintJob(long jobId);
	
	/**
	 * List of pending print jobs.
	 * @return Collection of print jobs.
	 */
	Collection<Long> printJobs();
	
	/**
	 * Starts the print job.
	 * @param jobId 
	 */
	void startPrintJob(final long jobId);
	
	/**
	 * Sends a command to the remote printer.
	 * @param remoteCommand Command to send. Must never be null.
	 */
	void sendCommand(RemoteCommand remoteCommand);


	/**
	 * Queues the returned data on this printer.
	 * @param data Data to be queued
	 */
	void queueReturnedData(ReturnedData data);
	
	
	/**
	 * Checks if there is a command ready to be sent.
	 * @return True if there is a command ready to sent, false otherwise.
	 */
	boolean isCommandToSendReady();
	
	/**
	 * Gets the command to send to the physical fiscal printer.
	 * @return Data or null if nothing is pending.
	 */
	RemoteCommand getCommandToSend();
	
	/**
	 * Resets the command to send.
	 */
	void resetCommandToSend();
	
	/**
	 * Waits for the response to become available, and returns it to the caller.
	 * @return Returned data.
	 */
	Object returnData();

	/**
	 * Execute a method on the object referenced within the remote command.
	 * @param jobId Id of the job being processed.
	 * @param remoteCommand Remote command to be processed.
	 * @return Serialized output.
	 */
	String callCommand(Long jobId, RemoteCommand remoteCommand);
	
	/**
	 * Registers a given object according to its object type.
	 * @param jobId Id of the job being processed.
	 * @param objectUUID Type of object to be registered.
	 * @param object Object to be registered.
	 * @return Previously registered object or null.
	 */
	Object registerObject(Long jobId, UUID objectUUID, Object object);
	
	/**
	 * Called from server to get the data saved by a previous command.
	 * @param jobId Id of the job being processed.
	 * @param dataUUID identifier of the data to be retrieve.
	 * @return String containing data or exception. The null value is represented by an special
	 * string.
	 */
	String getRemoteData(Long jobId, String dataUUID);
	
	/**
	 * Removes the remote data.
	 * @param jobId Id of the job being processed.
	 * @param dataUUID identifier of the data to be retrieve.
	 */
	void removeRemoteData(Long jobId, String dataUUID);
	
	/**
	 * Gets the adapter.
	 * @param jobId Id of the job being processed.
	 * @param objectUUID Id of the adapter
	 * @return Adapter object.
	 */
	Object getAdapter(Long jobId, UUID objectUUID);
	
}
