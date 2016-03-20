/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.print.PrintException;

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
import net.sf.wubiq.utils.PdfUtils;
import net.sf.wubiq.wrappers.GraphicParameter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public abstract class DirectConnectorQueueBase implements IDirectConnectorQueue {
	private static final Log LOG = LogFactory.getLog(DirectConnectorQueueBase.class);
	
	private String queueId;
	private Set<IRemoteListener> listeners;
	private UUID objectUUID;
	
	protected Map<Long, JobBucket> jobBuckets;
	
	protected class JobBucket {
		Map<UUID, Object> registeredObjects;
		Map<String, String> remoteDatas;
		IRemotePrintJob printJob;
	}

	/**
	 * Initializes the queue.
	 * @param queueId Id of the queue.
	 */
	protected DirectConnectorQueueBase(String queueId) {
		this.queueId = queueId;
		listeners = new HashSet<IRemoteListener>();
		objectUUID = UUID.randomUUID();
		jobBuckets = new TreeMap<Long, JobBucket>();
	}
	
	/**
	 * Gets this object UUID.
	 * @return Current object unique identification.
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#removePrintJob(long)
	 */
	@Override
	public synchronized boolean removePrintJob(long jobId) {
		IRemotePrintJob printJob = jobBucket(jobId).printJob;
		if (printJob != null) {
			PdfUtils.INSTANCE.closePageable(printJob.getPrintDataObject());
		}
		jobBucket(jobId).registeredObjects = null;
		jobBucket(jobId).remoteDatas = null;
		jobBucket(jobId).printJob = null;
		jobBuckets.remove(jobId);
		return true;
	}

	
	/***************************************
	 *  Objects registration and handling
	 * *************************************
	 */
	
	/**
	 * Returns the job bucket. The bucket contains the objects related to the print job.
	 * @param jobId Id of the job.
	 * @return Existing or newly created bucket.
	 */
	protected synchronized JobBucket jobBucket(Long jobId) {
		JobBucket jobBucket = jobBuckets.get(jobId);
		if (jobBucket == null) {
			jobBucket = new JobBucket();
			jobBuckets.put(jobId, jobBucket);
		}
		return jobBucket;
	}
	
	/**
	 * If required converts the stored data into appropriate type.
	 * @param returnValue Value to be returned.
	 */
	protected void manageConversion(IRemotePrintJob returnValue) {
		if (!returnValue.getOriginalDocFlavor().equals(returnValue.getDocFlavor())
				&& returnValue.getUsesDirectConnect()
				&& returnValue.getSupportsOnlyPageable()) {
			try {
				returnValue.setPrintDataObject(PdfUtils.INSTANCE.pdfToPageable(returnValue.getPrintData()));
			} catch (PrintException e) {
				LOG.fatal(ExceptionUtils.getMessage(e));
				throw new RuntimeException(e);
			} catch (IOException e) {
				LOG.fatal(ExceptionUtils.getMessage(e));
				throw new RuntimeException(e);
			}
			returnValue.setOriginalDocFlavor(returnValue.getDocFlavor()); // no more transformation for in memory queues.
		}
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#hasLocalPrintJob(java.lang.Long)
	 */
	@Override
	public boolean hasLocalPrintJob(Long jobId) {
		return jobBucket(jobId).printJob != null;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#registerObject(java.lang.Long, java.util.UUID, java.lang.Object)
	 */
	@Override
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

	/***************************************
	 *  IRemoteAdapter Interface implementation
	 * *************************************
	 */

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#queueId()
	 */
	@Override
	public String queueId() {
		return this.queueId;
	}
	
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxyAdapter#queue()
	 */
	@Override
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

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#startPrintJob(long)
	 */
	@Override
	public synchronized void startPrintJob(final long jobId) {
		registeredObjects(jobId).put(getObjectUUID(), this);
		IRemotePrintJob remotePrintJob = remotePrintJob(jobId, true);
		if (remotePrintJob != null) {
			Object printData = remotePrintJob.getPrintDataObject();
			if (!(printData instanceof Pageable) &&
					!(printData instanceof Printable)) {
				if (printData instanceof byte[]) {
					printData = new ByteArrayInputStream((byte[])printData);
				}
				if (printData instanceof ByteArrayInputStream) {
					((ByteArrayInputStream)printData).reset();
				}
				try {
					printData = PdfUtils.INSTANCE.pdfToPageable((InputStream)printData);
				} catch (PrintException e) {
					LOG.fatal(ExceptionUtils.getMessage(e));
					throw new RuntimeException(e);
				}
			}
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
	@Override
	public synchronized void sendCommand(RemoteCommand remoteCommand) {
		returnedDataReady = false;
		returnedData = null;
		commandToSend = remoteCommand;
		commandToSendReady = true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#queueReturnedData(net.sf.wubiq.adapters.ReturnedData)
	 */
	@Override
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
	@Override
	public synchronized boolean isCommandToSendReady() {
		return commandToSendReady;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getCommandToSend()
	 */
	@Override
	public synchronized RemoteCommand getCommandToSend() {
		return commandToSend;
	}
	
	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#resetCommandToSend()
	 */
	@Override
	public synchronized void resetCommandToSend() {
		commandToSendReady = false;
	}
	

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#returnData()
	 */
	@Override
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
				LOG.fatal(ExceptionUtils.getMessage(e), e);
				DirectConnectUtils.INSTANCE.notifyException(this, listeners(), ExceptionUtils.getMessage(e));
			}
		}
		returnedData = getReturnedData();
		if (returnedData.isRuntimeException() ||
				returnedData.isException()) {
			String errorCode = (String)returnedData.getData();
			DirectConnectUtils.INSTANCE.notifyTimeout(this, listeners());
			throw new RuntimeException(errorCode);
		} else {
			returnValue = returnedData.getData();
		}
		return returnValue;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#callCommand(java.lang.Long, net.sf.wubiq.enums.RemoteCommand, boolean clientSupportsCompression)
	 */
	@SuppressWarnings("rawtypes")
	@Override
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
			Object methodObject = getAdapter(jobId,printerCommand.getObjectUUID());
			try {
				Method method = DirectConnectUtils.INSTANCE.findMethod(methodObject.getClass(), methodName, parameterTypes);
				if (method != null) {
					data = method.invoke(methodObject, parameterValues);
				}
			} catch (Exception e) {
				error = ExceptionUtils.getMessage(ExceptionUtils.getRootCause(e));
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
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getRemoteData(java.lang.Long, java.lang.String)
	 */
	@Override
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
	@Override
	public synchronized void removeRemoteData(Long jobId, String dataUUID) {
		remoteDatas(jobId).remove(dataUUID);
	}

	/**
	 * @see net.sf.wubiq.print.managers.IDirectConnectorQueue#getAdapter(java.lang.Long, java.util.UUID)
	 */
	@Override
	public synchronized Object getAdapter(Long jobId, UUID adapterUUID) {
		return registeredObjects(jobId).get(adapterUUID);
	}

}
