/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.wubiq.adapters.RemoteCommand;
import net.sf.wubiq.adapters.RemotePageableAdapter;
import net.sf.wubiq.adapters.RemotePrintableAdapter;
import net.sf.wubiq.adapters.ReturnedData;
import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.utils.PageableUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles direct communication with printers.
 * @author Federico Alcantara
 *
 */
public class DirectConnectorQueue implements IRemoteAdapter {
	private static final Log LOG = LogFactory.getLog(DirectConnectorQueue.class);
	
	private String queueId;
	private Map<Long, RemotePrintJob> printJobs;
	private long onProcess;
	private Set<IRemoteListener> listeners;

	public DirectConnectorQueue(String queueId) {
		this.queueId = queueId;
		printJobs = new ConcurrentHashMap<Long, RemotePrintJob>();
		onProcess = -1l;
		listeners = Collections.synchronizedSet( new HashSet<IRemoteListener>());
	}
	
	/**
	 * Adds a print job to the manager.
	 * @param jobId Job Id.
	 * @param remotePrintJob Remote print job.
	 */
	public synchronized void addPrintJob(long jobId, RemotePrintJob remotePrintJob) {
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
	
	public synchronized RemotePrintJob remotePrintJob(long jobId) {
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
		Thread start = new Thread(new Runnable() {
			public void run() {
				RemotePrintJob remotePrintJob = printJobs.get(jobId);
				if (remotePrintJob != null) {
					Object printData = remotePrintJob.getPrintDataObject();
					if (printData instanceof Printable) {
						PageFormat pageFormat = PageableUtils.INSTANCE.getPageFormat(remotePrintJob.getPrintRequestAttributeSet());
						RemotePrintableAdapter printable = new RemotePrintableAdapter((Printable)printData, queueId());
						DirectConnectUtils.INSTANCE.exportRemotePrintable(printable, pageFormat);
					} else if (printData instanceof Pageable) {
						RemotePageableAdapter pageable = new RemotePageableAdapter((Pageable) remotePrintJob.getPrintDataObject(),
								queueId());
						DirectConnectUtils.INSTANCE.exportRemotePageable(pageable);
					}
					sendCommand(new RemoteCommand(RemoteCommandType.NONE, "endPrintJob"));
				}
			}
		}, "ConnectorQueue:" + Long.toString(jobId));
		start.start();
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
	public String methodName() {
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
			} catch(InterruptedException e) {
				LOG.fatal(e.getMessage(), e);
				DirectConnectUtils.INSTANCE.notifyException(this, listeners(), e.getMessage());
			} catch (TimeoutException e) {
				DirectConnectUtils.INSTANCE.notifyTimeout(this, listeners());
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


}
