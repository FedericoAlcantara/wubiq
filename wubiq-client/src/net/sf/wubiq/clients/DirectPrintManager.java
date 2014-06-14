/**
 * 
 */
package net.sf.wubiq.clients;

import java.awt.print.Pageable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.clients.remotes.PageableRemote;
import net.sf.wubiq.clients.remotes.PrintableRemote;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.enums.RemoteCommandType;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.utils.ClientPrintDirectUtils;
import net.sf.wubiq.utils.DirectConnectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class DirectPrintManager extends AbstractLocalPrintManager {
	private static final Log LOG = LogFactory.getLog(DirectPrintManager.class);

	private String jobId;
	private PrintService printService;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private PrintJobAttributeSet printJobAttributeSet;
	private DocAttributeSet docAttributeSet;
	private DocFlavor docFlavor;
	
	private boolean printing;
	private Map<RemoteCommandType, Object> registeredObjects;
	
	
	public DirectPrintManager(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet,
			DocFlavor docFlavor){
		this.jobId = jobId;
		this.printService = printService;
		this.printRequestAttributeSet = printRequestAttributeSet;
		this.printJobAttributeSet = printJobAttributeSet;
		this.docAttributeSet = docAttributeSet;
		this.docFlavor = docFlavor;
		this.registeredObjects = new ConcurrentHashMap<RemoteCommandType, Object>();
	}
		
	/**
	 * Ask the server about commands.
	 * @throws ConnectException
	 */
	public void handleDirectPrinting() throws ConnectException {
		printing = true;
		registeredObjects.clear();
		registeredObjects.put(RemoteCommandType.NONE, this);
		int timeout = 0;
		while (printing) {
			Object response  = directServer(DirectConnectCommand.POLL);
			if (response instanceof InputStream) {
				timeout = 0;
				final RemoteCommand remoteCommand = 
						(RemoteCommand) DirectConnectUtils.INSTANCE.deserialize((InputStream)response);
				if (remoteCommand != null) {
					// Run it in a separate thread, so it won't block the communication
					Thread callCommand = new Thread(new Runnable() {
						public void run() {
							callCommand(remoteCommand);
						}
					}, remoteCommand.getRemoteCommandType() + "-" + remoteCommand.getMethodName());
					callCommand.start();
				}
			} else {
				try {
					timeout = DirectConnectUtils.INSTANCE.checkTimeout(timeout);
				} catch (TimeoutException e) {
					throw new ConnectException(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Creates a printable object and starts the local printing process.
	 */
	public void createPrintable() {
		registeredObjects.put(RemoteCommandType.PRINTABLE, new PrintableRemote(this));
	}
	
	/**
	 * Creates a pageable object and starts the printing process.
	 */
	public void createPageable() {
		registeredObjects.put(RemoteCommandType.PAGEABLE, new PageableRemote(this));
		ClientPrintDirectUtils.printPageable(jobId, printService, printRequestAttributeSet, printJobAttributeSet, 
				docAttributeSet, 
				docFlavor,
				(Pageable)registeredObjects.get(RemoteCommandType.PAGEABLE));
	}
	
	/**
	 * Registers an object
	 * @param remoteCommandType
	 * @param object
	 */
	public void registerObject(RemoteCommandType remoteCommandType, Object object) {
		registeredObjects.put(remoteCommandType, object);
	}
	
	public void endPrintJob() {
		printing = false;
	}
	
	/**
	 * @return the readValue
	 */
	private synchronized String getRemoteData(UUID uuid) throws ConnectException, TimeoutException {
		int timeout = 0;
		String remoteData = null;
		do {
			remoteData = (String)directServer(DirectConnectCommand.POLL_REMOTE_DATA, 
					ParameterKeys.DIRECT_CONNECT_DATA_UUID 
					+ ParameterKeys.PARAMETER_SEPARATOR
					+ uuid.toString());
			timeout = DirectConnectUtils.INSTANCE.checkTimeout(timeout);
		} while (remoteData == null || 
				remoteData.equals(ParameterKeys.DIRECT_CONNECT_NOT_READY));
		return remoteData;
	}


	/**
	 * Calls the command on the physical printer.
	 * @param printerCommand Printer command to execute on the physical printer.
	 */
	@SuppressWarnings("rawtypes")
	protected void callCommand(RemoteCommand printerCommand) {
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
			Object methodObject = registeredObjects.get(printerCommand.getRemoteCommandType());
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
			try {
				if (error != null) {
					directServer(DirectConnectCommand.EXCEPTION, ParameterKeys.DIRECT_CONNECT_DATA 
							+ ParameterKeys.PARAMETER_SEPARATOR 
							+ error);
				} else {
					if (data != null) {
						String serializedData = DirectConnectUtils.INSTANCE.serialize(data);
						directServer(DirectConnectCommand.DATA, ParameterKeys.DIRECT_CONNECT_DATA 
								+ ParameterKeys.PARAMETER_SEPARATOR 
								+ serializedData);
					} else {
						directServer(DirectConnectCommand.DATA);
					}
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Connects to the remote and tries to read its data.
	 * @param remoteCommand Remote command.
	 * @return Data read from remote.
	 */
	public Object readFromRemote(RemoteCommand remoteCommand) {
		Object returnValue = null;
		try {
			UUID uuid = executeOnRemote(remoteCommand);
			String remoteData = null;
			int timeout = 0;
			while ((remoteData = getRemoteData(uuid)) == null) {
				timeout = DirectConnectUtils.INSTANCE.checkTimeout(timeout);
			}
			if (remoteData.startsWith(ParameterKeys.DIRECT_CONNECT_NULL)) {
				returnValue = null;
			} else if (remoteData.startsWith(ParameterKeys.DIRECT_CONNECT_EXCEPTION)) {
				throw new RuntimeException(remoteData.split(ParameterKeys.PARAMETER_SEPARATOR)[1]); 
			} else {
				returnValue = DirectConnectUtils.INSTANCE.deserialize(remoteData);
			}
		} catch (ConnectException e) {
			throw new RuntimeException(e.getMessage());
		} catch (TimeoutException e) {
			throw new RuntimeException(e.getMessage());
		}
		return returnValue;
	}
	
	/**
	 * Executes the remote command.
	 * @param remoteCommand Remote command to execute.
	 * @return Unique id of the data to retrieve.
	 */
	public UUID executeOnRemote(RemoteCommand remoteCommand) {
		String serialized = DirectConnectUtils.INSTANCE.serialize(remoteCommand);
		UUID returnValue = UUID.randomUUID();
		try {
			// Just read it, will return empty.
			directServer(DirectConnectCommand.READ_REMOTE, ParameterKeys.DIRECT_CONNECT_DATA
					+ ParameterKeys.PARAMETER_SEPARATOR
					+ serialized,
					ParameterKeys.DIRECT_CONNECT_DATA_UUID
					+ ParameterKeys.PARAMETER_SEPARATOR
					+ returnValue.toString()
					);
		} catch (ConnectException e) {
			throw new RuntimeException(e.getMessage());
		}
		return returnValue;
	}
	
	@Override
	protected void processPendingJob(String jobId) throws ConnectException {
	}

	@Override
	protected void registerPrintServices() throws ConnectException {
	}
}
