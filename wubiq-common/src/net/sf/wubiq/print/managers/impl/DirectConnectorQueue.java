/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.wubiq.adapters.PageableAdapter;
import net.sf.wubiq.adapters.PrintableAdapter;
import net.sf.wubiq.adapters.ReturnedData;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.wrappers.GraphicParameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles direct communication with printers.
 * @author Federico Alcantara
 *
 */
public class DirectConnectorQueue implements IDirectConnectorQueue {
	private static final Log LOG = LogFactory.getLog(DirectConnectorQueue.class);
	
	private String queueId;
	private Map<Long, IRemotePrintJob> printJobs;
	private long onProcess;
	private Set<IRemoteListener> listeners;
	private final Map<UUID, Object> registeredObjects;
	private Map<String, String> remoteDatas;
	private UUID objectUUID;

	public DirectConnectorQueue(String queueId) {
		this.queueId = queueId;
		printJobs = new ConcurrentHashMap<Long, IRemotePrintJob>();
		onProcess = -1l;
		listeners = Collections.synchronizedSet( new HashSet<IRemoteListener>());
		registeredObjects = new ConcurrentHashMap<UUID, Object>();
		remoteDatas = new ConcurrentHashMap<String, String>();
		objectUUID = UUID.randomUUID();
	}
	
	/**
	 * Adds a print job to the manager.
	 * @param jobId Job Id.
	 * @param remotePrintJob Remote print job.
	 */
	public synchronized void addPrintJob(long jobId, IRemotePrintJob remotePrintJob) {
		printJobs.put(jobId, remotePrintJob);
	}
	
	/**
	 * Removes the print job from the queue.
	 * @param jobId Id of the printJob.
	 * @return True if removed properly.
	 */
	public synchronized boolean removePrintJob(long jobId) {
		if (jobId == onProcess) {
			resetProcess();
		}
		printJobs.remove(jobId);
		return true;
	}
	
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		return printJobs.get(jobId);
	}
	
	/**
	 * List of pending print jobs.
	 * @return Collection of print jobs.
	 */
	public synchronized Collection<Long> printJobs() {
		return printJobs.keySet();
	}
	
	/**
	 * Starts the print job.
	 * @param jobId 
	 */
	public synchronized void startPrintJob(final long jobId) {
		registeredObjects.clear();
		registeredObjects.put(objectUUID, this);
		IRemotePrintJob remotePrintJob = printJobs.get(jobId);
		if (remotePrintJob != null) {
			Object printData = remotePrintJob.getPrintDataObject();
			if (printData instanceof Printable) {
				PrintableAdapter remote = new PrintableAdapter((Printable)printData, queue());
				sendCommand(new RemoteCommand(null, "createPrintable",
						new GraphicParameter(UUID.class, remote.getObjectUUID())));
			} else if (printData instanceof Pageable) {
				PageableAdapter remote = new PageableAdapter((Pageable)printData, queueId());
				sendCommand(new RemoteCommand(null, "createPageable",
						new GraphicParameter(UUID.class, remote.getObjectUUID())));
				// no returnedData() here, because this creation objects starts a new connection handshake sequence.
			}
		}
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#registerObject(net.sf.wubiq.enums.RemoteCommandType, java.lang.Object)
	 */
	public synchronized Object registerObject(UUID objectUUID, Object object) {
		return registeredObjects.put(objectUUID, object);
	}
	
	private void resetProcess() {
		onProcess = -1l;
	}

	/***************************************
	 *  IRemoteAdapter Interface implementation
	 * *************************************
	 */
	
	public String queueId() {
		return queueId;
	}
	
	public IDirectConnectorQueue queue() {
		return this;
	}

	@Override
	public void addListener(IRemoteListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean removeListener(IRemoteListener listener) {
		listeners.remove(listener);
		return true;
	}

	@Override
	public Set<IRemoteListener> listeners() {
		return listeners;
	}

	
	/***************************************
	 * SUPPORT ROUTINES
	 * *************************************
	 */
	private boolean returnedDataReady;
	private ReturnedData returnedData;
	private RemoteCommand commandToSend;
	private boolean commandToSendReady;
	
	/**
	 * Sends a command to the remote printer.
	 * @param remoteCommand Command to send. Must never be null.
	 */
	public synchronized void sendCommand(RemoteCommand remoteCommand) {
		returnedDataReady = false;
		returnedData = null;
		commandToSend = remoteCommand;
		commandToSendReady = true;
	}


	/**
	 * Queues the returned data on this printer.
	 * @param data Data to be queued
	 */
	public synchronized void queueReturnedData(ReturnedData data) {
		commandToSendReady = false;
		commandToSend = null;
		returnedData = data;
		returnedDataReady = true;
	}
	
	/**
	 * Gather the current state of the returned data.
	 * @return True if there is a returned data ready to be read, false otherwise.
	 */
	private synchronized boolean isReturnedDataReady() {
		return returnedDataReady;
	}

	/**
	 * Retrieves the returned data.
	 * @return Data to return.
	 */
	private synchronized ReturnedData getReturnedData() {
		return returnedData;
	}

	/**
	 * Checks if there is a command ready to be sent.
	 * @return True if there is a command ready to sent, false otherwise.
	 */
	public synchronized boolean isCommandToSendReady() {
		return commandToSendReady;
	}
	
	/**
	 * Gets the command to send to the physical fiscal printer.
	 * @return Data or null if nothing is pending.
	 */
	public synchronized RemoteCommand getCommandToSend() {
		return commandToSend;
	}
	
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
	 * Waits for the response to become available.
	 * @return Returned data.
	 */
	public Object returnData() {
		Object returnValue = null;
		ReturnedData returnedData = null;
		int timeout = 0;
		while (!isReturnedDataReady()) {
			try {
				timeout = DirectConnectUtils.INSTANCE.checkTimeout(timeout);
			} catch (TimeoutException e) {
				DirectConnectUtils.INSTANCE.notifyTimeout(this, listeners());
			} catch (Exception e) {
				LOG.fatal(e.getMessage(), e);
				DirectConnectUtils.INSTANCE.notifyException(this, listeners(), e.getMessage());
			}
		}
		returnedData = getReturnedData();
		if (returnedData.isRuntimeException() ||
				returnedData.isException()) {
			String errorCode = (String)returnedData.getData();
			DirectConnectUtils.INSTANCE.notifyTimeout(this, listeners);
			throw new RuntimeException(errorCode);
		} else {
			returnValue = returnedData.getData();
		}
		return returnValue;
	}

	/**
	 * Calls the command in a new thread.
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#callCommand(net.sf.wubiq.enums.RemoteCommand)
	 */
	public String callCommand(final RemoteCommand printerCommand, final String dataUUID) {
		remoteDatas.remove(dataUUID);
		Thread returnData = new Thread(new Runnable() {
			public void run() {
				remoteDatas.put(dataUUID, doCallCommand(printerCommand));
				
			}
		}, printerCommand.getObjectUUID() + "-" + printerCommand.getMethodName());
		returnData.start();
		return "";
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getRemoteData()
	 */
	public String getRemoteData(String dataUUID) {
		String returnValue = remoteDatas.get(dataUUID);
		if (returnValue == null) {
			returnValue = DirectConnectKeys.DIRECT_CONNECT_NOT_READY;
		}
		return returnValue;
	}
	
	@SuppressWarnings("rawtypes")
	private String doCallCommand(RemoteCommand printerCommand) {
		String serializedData = DirectConnectKeys.DIRECT_CONNECT_NULL;
		String methodName = printerCommand.getMethodName();
		Class[] parameterTypes = new Class[printerCommand.getParameters().length];
		Object[] parameterValues = new Object[printerCommand.getParameters().length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = printerCommand.getParameters()[i].getParameterType();
			parameterValues[i] = printerCommand.getParameters()[i].getParameterValue();
		}
		try {
			Object data = null;
			String error = null;
			Object methodObject = registeredObjects.get(printerCommand.getObjectUUID());
			try {
				Method method = DirectConnectUtils.INSTANCE.findMethod(methodObject.getClass(), methodName, parameterTypes);
				if (method != null) {
					data = method.invoke(methodObject, parameterValues);
				}
			} catch (Exception e) {
				if (e.getCause() != null) {
					error = e.getCause().getMessage();
				} else {
					error = e.getMessage();
				}
			}
			if (error != null) {
				serializedData = DirectConnectKeys.DIRECT_CONNECT_EXCEPTION
						+ ParameterKeys.PARAMETER_SEPARATOR
						+ error;
			} else {
				if (data != null &&
						data instanceof Serializable) {
					// Don't try to serialized a non serializable.
					// Return value that we don't want to transfer back
					// will be returned as non-serializable.
					serializedData = DirectConnectUtils.INSTANCE.serialize(data);
				}
			}
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		}
		return serializedData;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

}
