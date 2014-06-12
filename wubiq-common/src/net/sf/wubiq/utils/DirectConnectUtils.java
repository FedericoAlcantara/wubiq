/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import net.sf.wubiq.adapters.RemotePageableAdapter;
import net.sf.wubiq.adapters.RemotePrintableAdapter;
import net.sf.wubiq.enums.NotificationType;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.impl.DirectConnectorQueue;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Utilities for handling direct connections.
 * @author Federico Alcantara
 *
 */
public enum DirectConnectUtils {
	INSTANCE;
	private final int CONNECTION_TIMEOUT_SECONDS = 10;
	private final int CONNECTION_RETRY_MILLISECONDS = 100;
	
	private static final Log LOG = LogFactory.getLog(DirectConnectUtils.class);

	/**
	 * Validates if the connection is timed out.
	 * @param timeout Timeout.
	 */
	public int checkTimeout(int timeout) throws InterruptedException, TimeoutException {
		int returnValue = timeout + CONNECTION_RETRY_MILLISECONDS;
		Thread.sleep(CONNECTION_RETRY_MILLISECONDS);
		if ((returnValue / 1000) > CONNECTION_TIMEOUT_SECONDS) {
			throw new TimeoutException();
		}
		return returnValue;
	}

	/**
	 * Send a notification to all listeners about a timeout ocurrence.
	 * @param fiscalPrinter FiscalPrinter originating the time out.
	 * @param listeners Set of listeners to be notified.
	 */
	public void notifyTimeout(IRemoteAdapter adapter, Set<IRemoteListener> listeners) {
		for (IRemoteListener listener : listeners) {
			listener.notify(adapter.queueId(), NotificationType.TIMEOUT, ServerLabels.get("server.exception.timeout"));
		}
	}

	/**
	 * Notifies an exception.
	 * @param adapter Associated adapter.
	 * @param listeners Listeners.
	 * @param message Message to notify.
	 */
	public void notifyException(IRemoteAdapter adapter, Set<IRemoteListener> listeners, String message) {
		for (IRemoteListener listener : listeners) {
			listener.notify(adapter.queueId(), NotificationType.UNDETERMINED_EXCEPTION, message);
		}
	}
	
	/**
	 * Direct connector.
	 * @param queueId Id of the queue.
	 * @return Direct connector instance. Never null.
	 */
	public DirectConnectorQueue directConnector(String queueId) {
		IDirectConnectPrintJobManager manager = 
				(IDirectConnectPrintJobManager) RemotePrintJobManagerFactory
					.getRemotePrintJobManager(queueId);
		DirectConnectorQueue queue = manager.directConnector(queueId);
		return queue;
	}
	
	/**
	 * Serializes a given object.
	 * @param object Object to be encoded.
	 * @return Encoded object. Null if object can't be serialized.
	 */
	public String serialize(Object object) {
		String returnValue = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutput = new ObjectOutputStream(output);
			objectOutput.writeObject(object);
			output.flush();
			returnValue = toHex(output.toByteArray());
			
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}
	
	/**
	 * De serializes a given string.
	 * @param serialized String representing the object.
	 * @return Object deserialized. Null if can't be converted.
	 */
	public Object deserialize(String serialized) {
		Object returnValue = null;
		if (serialized != null &&
				!"".equals(serialized)) {
			returnValue = deserialize(new ByteArrayInputStream(serialized.getBytes()));
		}
		return returnValue;
	}

	/**
	 * De serializes a given string.
	 * @param serialized String representing the object.
	 * @return Object deserialized. Null if can't be converted.
	 */
	public Object deserialize(InputStream serialized) {
		Object returnValue = null;
		if (serialized != null) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				IOUtils.INSTANCE.copy(serialized, output);
				output.flush();
				ByteArrayInputStream input = new ByteArrayInputStream(fromHex(output.toString()));
				ObjectInputStream objectInput = new ObjectInputStream(input);
				returnValue = objectInput.readObject();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		return returnValue;
	}
	

	/**
	 * Converts data to hex.
	 * @param data data to be converted.
	 * @return Hex data in format 0xx.
	 */
	private String toHex(byte[] data) {
		return DatatypeConverter.printHexBinary(data);
	}
	
	/**
	 * Converts hex representation into a byte array
	 * @param hexData Data to be converted.
	 * @return Byte[] representing the data.
	 */
	private byte[] fromHex(String hexData) {
		byte[] returnValue = DatatypeConverter.parseHexBinary(hexData);
		return returnValue;
	}

	/**
	 * 
	 * @param clazz Class that must contain the given method.
	 * @param name Name of the method.
	 * @param parameterTypes Array of parameter types of the method sought.
	 * @return the method that should be called. Null if the method is not found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Method findMethod(Class clazz, String name, Class[] parameterTypes) {
		Method method = null;
		try {
			method = clazz.getDeclaredMethod(name, parameterTypes);
		} catch (SecurityException e) {
			LOG.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			method = null;
		}
		if (method == null) { // let's try manual lookup on current class
			for (Method readMethod : clazz.getDeclaredMethods()) {
				if (readMethod.getName().equals(name) &&
						readMethod.getParameterTypes().length == 
						parameterTypes.length) {
					method = readMethod;
					break;
				}
			}
		}
		if (method == null) { // let's try super class 
			Class superclass = clazz.getSuperclass();
			if (superclass != null) {
				method = findMethod(superclass, name, parameterTypes);
			}
		}
		return method;
	}

	/**
	 * Exports a pageable.
	 * @param pageable Pageable to export.
	 */
	public void exportRemotePageable(RemotePageableAdapter pageable) {
		int result = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		PageFormat pageFormat = pageable.getPageFormat(pageIndex);
		do {
			RemotePrintableAdapter printable = (RemotePrintableAdapter) pageable.getPrintable(pageIndex);
			result = doExportRemotePrintable(printable, pageFormat, pageIndex);
			pageIndex++;
		} while (result == Printable.PAGE_EXISTS);
	}
	
	public void exportRemotePrintable(RemotePrintableAdapter printable, PageFormat pageFormat) {
		int result = Printable.PAGE_EXISTS;
		int pageIndex = 0;
		do {
			result = doExportRemotePrintable(printable, pageFormat, pageIndex);
			pageIndex++;
		} while (result == Printable.PAGE_EXISTS);
	}
	
	/**
	 * Export remote printable.
	 * @param printable Printable to export.
	 * @param pageFormat Page format to use.
	 * @param pageIndex Page to export.
	 * @return Status of the operation.
	 */
	public int doExportRemotePrintable(RemotePrintableAdapter printable, PageFormat pageFormat, int pageIndex) {
		int returnValue = Pageable.UNKNOWN_NUMBER_OF_PAGES;
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		try {
			AffineTransform scaleTransform = new AffineTransform();
			scaleTransform.scale(1, 1);
			graph.setTransform(scaleTransform);
			graph.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			graph.setClip(new Rectangle2D.Double(
					0,
					0,
					pageFormat.getPaper().getImageableWidth(), 
					pageFormat.getPaper().getImageableHeight()));
			graph.setBackground(Color.WHITE);
			graph.clearRect(0, 0, (int)Math.rint(pageFormat.getPaper().getImageableWidth()),
					(int)Math.rint(pageFormat.getPaper().getImageableHeight()));
			returnValue = printable.print(graph, pageFormat, pageIndex);
			graph.dispose();
		} catch (PrinterException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnValue;
	}

}
