/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
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
import net.sf.wubiq.proxies.ProxyAdapterMaster;
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
	private long onProcess;
	private Set<IRemoteListener> listeners;
	private UUID objectUUID;
	private Map<Long, JobBucket> jobBuckets;

	private class JobBucket {
		private Map<UUID, Object> registeredObjects;
		private Map<String, String> remoteDatas;
		private IRemotePrintJob printJob;
	}
	
	public DirectConnectorQueue(String queueId) {
		this.queueId = queueId;
		onProcess = -1l;
		listeners = new HashSet<IRemoteListener>();
		jobBuckets = new HashMap<Long, JobBucket>();
		objectUUID = UUID.randomUUID();
	}
	
	/**
	 * Adds a print job to the manager.
	 * @param jobId Job Id.
	 * @param remotePrintJob Remote print job.
	 */
	public synchronized void addPrintJob(long jobId, IRemotePrintJob remotePrintJob) {
		jobBucket(jobId).printJob = remotePrintJob;
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
		jobBucket(jobId).registeredObjects = null;
		jobBucket(jobId).remoteDatas = null;
		jobBucket(jobId).printJob = null;
		jobBuckets.remove(jobId);
		return true;
	}
	
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		return jobBucket(jobId).printJob;
	}
	
	/**
	 * List of pending print jobs.
	 * @return Collection of print jobs.
	 */
	public synchronized Collection<Long> printJobs() {
		return jobBuckets.keySet();
	}
	
	/**
	 * Starts the print job.
	 * @param jobId 
	 */
	public synchronized void startPrintJob(final long jobId) {
		registeredObjects(jobId).put(objectUUID, this);
		IRemotePrintJob remotePrintJob = jobBucket(jobId).printJob;
		if (remotePrintJob != null) {
			Object printData = remotePrintJob.getPrintDataObject();
			if (printData instanceof Printable) {
				PrintableAdapter remote = (PrintableAdapter)
						Enhancer.create(PrintableAdapter.class,
								new ProxyAdapterMaster(
										jobId,
										this,
										printData,
										PrintableAdapter.FILTERED_METHODS));
				sendCommand(new RemoteCommand(null, "createPrintable",
						new GraphicParameter(UUID.class, remote.objectUUID())));
			} else if (printData instanceof Pageable) {
				PageableAdapter remote = (PageableAdapter)
						Enhancer.create(PageableAdapter.class, 
								new ProxyAdapterMaster(
										jobId,
										this,
										printData,
										PageableAdapter.FILTERED_METHODS));
				sendCommand(new RemoteCommand(null, "createPageable",
						new GraphicParameter(UUID.class, remote.objectUUID())));
				// no returnedData() here, because this creation objects starts a new connection handshake sequence.
			}
		}
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#registerObject(net.sf.wubiq.enums.RemoteCommandType, java.lang.Object)
	 */
	public synchronized Object registerObject(Long jobId, UUID objectUUID, Object object) {
		return registeredObjects(jobId).put(objectUUID, object);
	}
	
	/**
	 * Finds the map of registered objects for the job.
	 * @param jobId Id of the job to get the registered object from.
	 * @return Map of registered objects.
	 */
	private synchronized Map<UUID, Object> registeredObjects(Long jobId) {
		Map<UUID, Object> registeredObjects = jobBucket(jobId).registeredObjects;
		if (registeredObjects == null) {
			registeredObjects = new HashMap<UUID, Object>();
			jobBucket(jobId).registeredObjects = registeredObjects;
		}
		return registeredObjects;
	}
	
	/**
	 * Finds the map of registered objects for the job.
	 * @param jobId Id of the job to get the registered object from.
	 * @return Map of registered objects.
	 */
	private synchronized Map<String, String> remoteDatas(Long jobId) {
		Map<String, String> remoteDatas = jobBucket(jobId).remoteDatas;
		if (remoteDatas == null) {
			remoteDatas = new HashMap<String, String>();
			jobBucket(jobId).remoteDatas = remoteDatas;
		}
		return remoteDatas;
	}

	/**
	 * Returns the job bucket. The bucket contains the objects related to the print job.
	 * @param jobId Id of the job.
	 * @return Existing or newly created bucket.
	 */
	private synchronized JobBucket jobBucket(Long jobId) {
		JobBucket jobBucket = jobBuckets.get(jobId);
		if (jobBucket == null) {
			jobBucket = new JobBucket();
			jobBuckets.put(jobId, jobBucket);
		}
		return jobBucket;
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
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public boolean removeListener(IRemoteListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
		return true;
	}

	@Override
	public synchronized Set<IRemoteListener> listeners() {
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
	
	public synchronized void resetCommandToSend() {
		commandToSendReady = false;
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
	public String callCommand(final Long jobId, final RemoteCommand printerCommand, final String dataUUID) {
		remoteDatas(jobId).remove(dataUUID);
		remoteDatas(jobId).put(dataUUID, DirectConnectKeys.DIRECT_CONNECT_NOT_READY);
		Thread returnData = new Thread(new Runnable() {
			public void run() {
				remoteDatas(jobId).put(dataUUID, doCallCommand(jobId, printerCommand));
				
			}
		}, printerCommand.getObjectUUID() + "-" + printerCommand.getMethodName());
		returnData.start();
		return "";
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getRemoteData()
	 */
	public synchronized String getRemoteData(Long jobId, String dataUUID) {
		String returnValue = remoteDatas(jobId).get(dataUUID);
		if (returnValue == null) {
			returnValue = DirectConnectKeys.DIRECT_CONNECT_NOT_READY;
		}
		return returnValue;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#removeRemoteData(java.lang.Long, java.lang.String)
	 */
	public synchronized void removeRemoteData(Long jobId, String dataUUID) {
		remoteDatas(jobId).remove(dataUUID);
	}
	
	/**
	 * @param jobId
	 * @param adapterUUID
	 * @return
	 */
	public synchronized Object getAdapter(Long jobId, UUID adapterUUID) {
		return registeredObjects(jobId).get(adapterUUID);
	}
	
	@SuppressWarnings("rawtypes")
	private String doCallCommand(Long jobId, RemoteCommand printerCommand) {
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
			Object methodObject = registeredObjects(jobId).get(printerCommand.getObjectUUID());
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
	 * @see net.sf.wubiq.interfaces.IAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

}
