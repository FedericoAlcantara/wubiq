/**
 * 
 */
package net.sf.wubiq.clients;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.remotes.PageableRemote;
import net.sf.wubiq.clients.remotes.PrintableChunkRemote;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.proxies.ProxyClientSlave;
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

	private String jobIdString;
	private Long jobId;
	private PrintService printService;
	private String printServiceName;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private PrintJobAttributeSet printJobAttributeSet;
	private DocAttributeSet docAttributeSet;
	private boolean serverSupportsCompression;
	private DocFlavor docFlavor;
	private InputStream printData;
	private boolean printing;
	private boolean printSerialized;
	private Map<UUID, Object> registeredObjects;
	private LocalPrintManager localPrintManager;
	
	
	protected DirectPrintManager (String jobIdString, PrintService printService,
			String printServiceName,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			boolean debugMode,
			int debugLevel,
			boolean serverSupportsCompression,
			DocFlavor docFlavor,
			InputStream printData) {
		this(jobIdString, printService, printServiceName, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, 
				debugMode, debugLevel, serverSupportsCompression);
		this.docFlavor = docFlavor;
		this.printData = printData;
		this.printSerialized = true;
	}

	/**
	 * Creates an instances of direct print manager.
	 * @param jobIdString Id of the job.
	 * @param printService PrintService to print to.
	 * @param printServiceName The name of the print service as registered. 
	 * @param printRequestAttributeSet Attributes to be set on the print service.
	 * @param printJobAttributeSet Attributes for the print job.
	 * @param docAttributeSet Attributes for the document.
	 * @param debugMode The state of the the debug mode.
	 * @param debugLevel The debug Level.
	 * @return an instance of a DirectPrintManager.
	 */
	protected DirectPrintManager(String jobIdString, PrintService printService, 
			String printServiceName,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet,
			boolean debugMode,
			int debugLevel,
			boolean serverSupportsCompression){
		this.jobIdString = jobIdString;
		this.jobId = Long.parseLong(jobIdString);
		this.printService = printService;
		this.printServiceName = printServiceName;
		this.printRequestAttributeSet = printRequestAttributeSet;
		this.printJobAttributeSet = printJobAttributeSet;
		this.docAttributeSet = docAttributeSet;
		setDebugMode(debugMode);
		setDebugLevel(debugLevel);
		this.serverSupportsCompression = serverSupportsCompression;
	}
		
	/**
	 * Ask the server about commands.
	 * @throws ConnectException
	 */
	@Override
	public void run() {
		boolean closePrintJob = true;
		try {
			try {
				if (printSerialized) {
					printSerialized();
				} else {
					printDirect();
				}
				doLog("Job(" + jobId + ") printed.", 0);
			} catch (ConnectException e) {
				doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
				LOG.error(e.getMessage(), e);
				closePrintJob = false;
			} catch (IOException e) {
				doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
				LOG.error(e.getMessage(), e);
				closePrintJob = false;
			} catch (Exception e) {
				doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
				LOG.error(e.getMessage(), e);
			} finally {
				try {
					if (printData != null) {
						printData.close();
					}
				} catch (IOException e) {
					doLog(e.getMessage());
				}
			}
		} finally {
			try {
				if (closePrintJob) {
					localPrintManager.closePrintJob(jobId);
				}
			} catch (Exception e) {
				doLog(e.getMessage());
			} finally {
				localPrintManager.releasePrintService(printServiceName);
				localPrintManager.unRegisterJob(jobId);
			}
		}
	}
	
	/**
	 * @return the localPrintManager
	 */
	public LocalPrintManager getLocalPrintManager() {
		return localPrintManager;
	}

	/**
	 * @param localPrintManager the localPrintManager to set
	 */
	public void setLocalPrintManager(LocalPrintManager localPrintManager) {
		this.localPrintManager = localPrintManager;
	}

	protected void printSerialized() throws IOException {
		ClientPrintDirectUtils.print(jobIdString, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
	}
	
	protected void printDirect() throws ConnectException {
		printing = true;
		registeredObjects = new HashMap<UUID, Object>();
		int timeout = 0;
		while (printing) {
			Object response  = serverSupportsCompression
					? directServerNotSerialized(jobIdString, DirectConnectCommand.POLL)
					: directServer(jobIdString, DirectConnectCommand.POLL, "");
			if (response instanceof InputStream) {
				timeout = 0;
				Object object = serverSupportsCompression
						? DirectConnectUtils.INSTANCE.deserializeObject((InputStream) response)
						: DirectConnectUtils.INSTANCE.deserialize((InputStream)response);
				final RemoteCommand remoteCommand = (RemoteCommand) object;
				if (remoteCommand != null) {
					callCommand(remoteCommand);
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
	public void createPrintable(UUID objectUUID) throws PrinterException {
		PrintableChunkRemote remote = (PrintableChunkRemote) Enhancer.create(getPrintableChunkRemoteClass(), 
				new ProxyClientSlave(jobId, this, objectUUID, PrintableChunkRemote.FILTERED_METHODS));
		
		remote.setServerSupportsCompression(serverSupportsCompression);
		
		printPrintable(jobIdString, printService, printRequestAttributeSet, printJobAttributeSet, 
				docAttributeSet, 
				remote);
		printing = false;
	}
	
	/**
	 * Default class for remote printable chunk instance.
	 * @return Class to be used for remote printable.
	 */
	protected Class<? extends PrintableChunkRemote> getPrintableChunkRemoteClass() {
		return PrintableChunkRemote.class;
	}
	
	/**
	 * Prints a printable object.
	 * @param jobId Id of the object.
	 * @param printService Print service.
	 * @param printRequestAttributeSet Print request Attribute set.
	 * @param printJobAttributeSet Print Job attribute set.
	 * @param docAttributeSet Document attribute set.
	 * @param printable Printable.
	 */
	protected void printPrintable(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			Printable printable) throws PrinterException {
		ClientPrintDirectUtils.printPrintable(jobIdString, printService, printRequestAttributeSet, printJobAttributeSet, 
				docAttributeSet, 
				printable);
	}
	
	/**
	 * Creates a pageable object and starts the printing process.
	 */
	public void createPageable(UUID objectUUID) throws PrinterException {
		PageableRemote remote = (PageableRemote) Enhancer.create(getPageableRemoteClass(), 
				new ProxyClientSlave(jobId, this, objectUUID, getPageableFilteredMethods()));
		remote.setServerSupportsCompression(serverSupportsCompression);
		
		printPageable(jobIdString, printService, printRequestAttributeSet, printJobAttributeSet, 
				docAttributeSet, 
				remote);
		printing = false;
	}

	/**
	 * Default class to use for pageable remotes.
	 * @return Pageable class.
	 */
	protected Class<? extends PageableRemote> getPageableRemoteClass() {
		return PageableRemote.class;
	}
	
	/**
	 * Default filtered methods for pageable.
	 * @return String array with the names of the filtered methods.
	 */
	protected String[] getPageableFilteredMethods() {
		return PageableRemote.FILTERED_METHODS;
	}
	
	/**
	 * Prints a pageable object.
	 * @param jobId Id of the object.
	 * @param printService Print service.
	 * @param printRequestAttributeSet Print request Attribute set.
	 * @param printJobAttributeSet Print Job attribute set.
	 * @param docAttributeSet Document attribute set.
	 * @param pageable Pageable.
	 */
	protected void printPageable(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			Pageable pageable) throws PrinterException {
		ClientPrintDirectUtils.printPageable(jobIdString, printService, printRequestAttributeSet, printJobAttributeSet, 
				docAttributeSet, 
				pageable);
	}
	/**
	 * Registers an object for remote communication on the client side.
	 * @param objectUUID object unique id.
	 * @param object Object to be registered.
	 */
	public synchronized void registerObject(UUID objectUUID, Object object) {
		registeredObjects.put(objectUUID, object);
	}
	
	public void endPrintJob() {
		printing = false;
	}

	/**
	 * Calls the command on the physical printer.
	 * @param printerCommand Printer command to execute on the physical printer.
	 * @return True if an error ocurred.
	 */
	@SuppressWarnings("rawtypes")
	protected boolean callCommand(RemoteCommand printerCommand) {
		boolean returnValue = false;
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
			Object methodObject = null;
			if (printerCommand.getObjectUUID() == null) {
				methodObject = this;
			} else {
				methodObject = registeredObjects.get(printerCommand.getObjectUUID());
			}
			try {
				Method method = DirectConnectUtils.INSTANCE.findMethod(methodObject.getClass(), methodName, parameterTypes);
				if (method != null) {
					data = method.invoke(methodObject, parameterValues);
				}
			} catch (Exception e) {
				returnValue = true;
				e.printStackTrace();
				if (e.getCause() != null) {
					error = e.getCause().getMessage();
				} else {
					error = e.getMessage();
				}
			}
			try {
				if (error != null) {
					if (serverSupportsCompression) {
						Map<String, Object> parameters = new HashMap<String, Object>();
						parameters.put(DirectConnectKeys.DIRECT_CONNECT_DATA, error);
						directServerNotSerialized(jobIdString, DirectConnectCommand.EXCEPTION, parameters);
					} else {
						directServer(jobIdString, DirectConnectCommand.EXCEPTION, DirectConnectKeys.DIRECT_CONNECT_DATA 
								+ ParameterKeys.PARAMETER_SEPARATOR 
								+ DirectConnectUtils.INSTANCE.serialize(error));
					}
					throw new RuntimeException(error);
				} else {
					if (data != null) {
						if (serverSupportsCompression) {
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(DirectConnectKeys.DIRECT_CONNECT_DATA, data);
							directServerNotSerialized(jobIdString, DirectConnectCommand.DATA, parameters);
						} else {
							String serializedData = DirectConnectUtils.INSTANCE.serialize(data);
							directServer(jobIdString, DirectConnectCommand.DATA, DirectConnectKeys.DIRECT_CONNECT_DATA 
									+ ParameterKeys.PARAMETER_SEPARATOR 
									+ serializedData);
						}
					} else {
						if (serverSupportsCompression) {
							Map<String, Object> parameters = new HashMap<String, Object>();
							parameters.put(DirectConnectKeys.DIRECT_CONNECT_DATA, "");
							directServerNotSerialized(jobIdString, DirectConnectCommand.DATA, parameters);
						} else {
							directServer(jobIdString, DirectConnectCommand.DATA, "");
						}
					}
				}
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			}
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}
	
	/**
	 * Connects to the remote and tries to read its data.
	 * @param remoteCommand Remote command.
	 * @return Data read from remote.
	 */
	public Object readFromRemote(RemoteCommand remoteCommand) {
		Object returnValue = null;
		if (serverSupportsCompression) {
			InputStream remoteData = (InputStream) executeOnRemote(remoteCommand);
			returnValue = DirectConnectUtils.INSTANCE.deserializeObject(remoteData);
			if (returnValue != null) {
				if ((returnValue instanceof String) &&
						((String)returnValue).startsWith(DirectConnectKeys.DIRECT_CONNECT_NULL)) {
					returnValue = null;
				} else if ((returnValue instanceof String) &&
						((String)returnValue).startsWith(DirectConnectKeys.DIRECT_CONNECT_EXCEPTION)) {
					throw new RuntimeException(((String)returnValue).split(ParameterKeys.PARAMETER_SEPARATOR)[1]); 
				}
			}
		} else {
			String remoteData = (String) executeOnRemote(remoteCommand);
			if (remoteData.startsWith(DirectConnectKeys.DIRECT_CONNECT_NULL)) {
				returnValue = null;
			} else if (remoteData.startsWith(DirectConnectKeys.DIRECT_CONNECT_EXCEPTION)) {
				throw new RuntimeException(remoteData.split(ParameterKeys.PARAMETER_SEPARATOR)[1]); 
			} else {
				returnValue = DirectConnectUtils.INSTANCE.deserialize(remoteData);
			}
		}
		return returnValue;
	}
	
	/**
	 * Executes the remote command.
	 * @param remoteCommand Remote command to execute.
	 * @return Unique id of the data to retrieve.
	 */
	public Object executeOnRemote(RemoteCommand remoteCommand) {
		Object returnValue = null;
		try {
			if (serverSupportsCompression) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put(DirectConnectKeys.DIRECT_CONNECT_DATA, remoteCommand);
				returnValue = directServerNotSerialized(jobIdString, DirectConnectCommand.READ_REMOTE, parameters);
			} else {
				String serialized = DirectConnectUtils.INSTANCE.serialize(remoteCommand);
					returnValue = directServer(jobIdString, DirectConnectCommand.READ_REMOTE, DirectConnectKeys.DIRECT_CONNECT_DATA
							+ ParameterKeys.PARAMETER_SEPARATOR
							+ serialized);
			}
		} catch (ConnectException e) {
			throw new RuntimeException(e.getMessage());
		}
		return returnValue;
	}
	
	@Override
	protected void processPendingJob(String jobId, String printServiceName) throws ConnectException {
	}

	@Override
	protected void registerPrintServices() throws ConnectException {
	}
	
	@Override
	public void doLog(Object message, int logLevel) {
		super.doLog(message, logLevel);
	}
}
