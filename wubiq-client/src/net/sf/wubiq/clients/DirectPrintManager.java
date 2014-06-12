/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.ConnectException;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.adapters.RemoteCommand;
import net.sf.wubiq.clients.remotes.PageableRemote;
import net.sf.wubiq.clients.remotes.PrintableRemote;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.utils.DirectConnectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class DirectPrintManager extends AbstractLocalPrintManager {
	private static final Log LOG = LogFactory.getLog(DirectPrintManager.class);
	private PageableRemote pageable;
	private PrintableRemote printable;

	private PrintService printService;
	private PrintRequestAttributeSet printRequestAttributeSet;
	private PrintJobAttributeSet printJobAttributeSet;
	private DocAttributeSet docAttributeSet;
	private DocFlavor docFlavor;
	
	private boolean printing;
	
	public DirectPrintManager(PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet,
			DocFlavor docFlavor){
		this.printService = printService;
		this.printRequestAttributeSet = printRequestAttributeSet;
		this.printJobAttributeSet = printJobAttributeSet;
		this.docAttributeSet = docAttributeSet;
		this.docFlavor = docFlavor;
	}
		
	/**
	 * Ask the server about commands.
	 * @throws ConnectException
	 */
	public void handleDirectPrinting() throws ConnectException {
		printing = true;
		int timeout = 0;
		while (printing) {
			Object response  = directServer(DirectConnectCommand.POLL);
			if (response instanceof InputStream) {
				timeout = 0;
				RemoteCommand remoteCommand = 
						(RemoteCommand) DirectConnectUtils.INSTANCE.deserialize((InputStream)response);
				if (remoteCommand != null) {
					callCommand(remoteCommand);
				}
			} else {
				try {
					timeout = DirectConnectUtils.INSTANCE.checkTimeout(timeout);
				} catch (InterruptedException e) {
					LOG.fatal(e.getMessage(), e);
				} catch (TimeoutException e) {
					throw new ConnectException(e.getMessage());
				}
			}
		}
	}
	
	public void endPrintJob() {
		printing = false;
	}
	
	/**
	 * Calls the command on the physical printer.
	 * @param printerCommand Printer command to execute on the physical printer.
	 */
	@SuppressWarnings("rawtypes")
	protected void callCommand(RemoteCommand printerCommand) {
		String methodName = printerCommand.getMethodName();
		Class[] parameterTypes = new Class[printerCommand.getParameters().length];
		for (int i = 0; i < parameterTypes.length; i++) {
			parameterTypes[i] = printerCommand.getParameters()[i].getClass();
		}
		Method method = DirectConnectUtils.INSTANCE.findMethod(this.getClass(), methodName, parameterTypes);
		if (method != null) {
			try {
				Object data = null;
				String error = null;
				DirectConnectCommand errorCommand = null;
				Object methodObject = this;
				try {
					switch(printerCommand.getRemoteCommandType()) {
						case PAGEABLE: 
							if (pageable == null) {
								pageable = new PageableRemote(this);
							}
							methodObject = pageable;
							break;
						case PRINTABLE:
							if (printable == null) {
								printable = new PrintableRemote(this);
							}
							break;
						case GRAPHICS:
					}
					data = method.invoke(methodObject, (Object [])printerCommand.getParameters());
				} catch (Exception e) {
					errorCommand = DirectConnectCommand.EXCEPTION;
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
	}

	@Override
	protected void processPendingJob(String jobId) throws ConnectException {
	}

	@Override
	protected void registerPrintServices() throws ConnectException {
	}
}
