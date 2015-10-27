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
import java.util.TreeMap;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.adapters.PageableAdapter;
import net.sf.wubiq.adapters.PrintableChunkAdapter;
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
	
	/**
	 * Creates the unique instance of DirectConnectorQueue. For each queue (Print Service) a new instance is created.
	 * @param queueId
	 */
	protected DirectConnectorQueue(String queueId) {
		this.queueId = queueId;
		onProcess = -1l;
		listeners = new HashSet<IRemoteListener>();
		jobBuckets = new TreeMap<Long, JobBucket>();
		objectUUID = UUID.randomUUID();
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#addPrintJob(long, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	public synchronized void addPrintJob(long jobId, IRemotePrintJob remotePrintJob) {
		jobBucket(jobId).printJob = remotePrintJob;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#removePrintJob(long)
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
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#remotePrintJob(long)
	 */
	public synchronized IRemotePrintJob remotePrintJob(long jobId) {
		return jobBucket(jobId).printJob;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#printJobs()
	 */
	public synchronized Collection<Long> printJobs() {
		return jobBuckets.keySet();
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#startPrintJob(long)
	 */
	public synchronized void startPrintJob(final long jobId) {
		registeredObjects(jobId).put(objectUUID, this);
		IRemotePrintJob remotePrintJob = jobBucket(jobId).printJob;
		if (remotePrintJob != null) {
			Object printData = remotePrintJob.getPrintDataObject();
			if (printData instanceof Printable) {
				PrintableChunkAdapter remote = (PrintableChunkAdapter)
						Enhancer.create(PrintableChunkAdapter.class,
								new ProxyAdapterMaster(
										jobId,
										this,
										printData,
										PrintableChunkAdapter.FILTERED_METHODS));
				remote.setPageFormat(remotePrintJob.getPageFormat());
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#registerObject(java.lang.Long, java.util.UUID, java.lang.Object)
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
	
	/**
	 * Resets the process.
	 */
	private void resetProcess() {
		onProcess = -1l;
	}

	/***************************************
	 *  IRemoteAdapter Interface implementation
	 * *************************************
	 */
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#queueId()
	 */
	public String queueId() {
		return queueId;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyAdapter#queue()
	 */
	public IDirectConnectorQueue queue() {
		return this;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
		return true;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IAdapter#listeners()
	 */
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#sendCommand(net.sf.wubiq.enums.RemoteCommand)
	 */
	public synchronized void sendCommand(RemoteCommand remoteCommand) {
		returnedDataReady = false;
		returnedData = null;
		commandToSend = remoteCommand;
		commandToSendReady = true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#queueReturnedData(net.sf.wubiq.adapters.ReturnedData)
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#isCommandToSendReady()
	 */
	public synchronized boolean isCommandToSendReady() {
		return commandToSendReady;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getCommandToSend()
	 */
	public synchronized RemoteCommand getCommandToSend() {
		return commandToSend;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#resetCommandToSend()
	 */
	public synchronized void resetCommandToSend() {
		commandToSendReady = false;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#returnData()
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getRemoteData(java.lang.Long, java.lang.String)
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getAdapter(java.lang.Long, java.util.UUID)
	 */
	public synchronized Object getAdapter(Long jobId, UUID adapterUUID) {
		return registeredObjects(jobId).get(adapterUUID);
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#callCommand(java.lang.Long, net.sf.wubiq.enums.RemoteCommand, boolean clientSupportsCompression)
	 */
	@SuppressWarnings("rawtypes")
	public Object callCommand(Long jobId, RemoteCommand printerCommand, boolean clientSupportsCompression) {
		Object serializedData = DirectConnectKeys.DIRECT_CONNECT_NULL;
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
					serializedData = data;
				}
			}
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		}
		return clientSupportsCompression
				? serializedData
				: DirectConnectUtils.INSTANCE.serialize(serializedData);
	}
	
	/**
	 * Gets this object UUID.
	 * @return Current object unique identification.
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

}
