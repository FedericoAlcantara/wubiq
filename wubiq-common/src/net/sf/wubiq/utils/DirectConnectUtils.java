/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import net.sf.wubiq.enums.NotificationType;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
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
	public int checkTimeout(int timeout) throws TimeoutException {
		int returnValue = timeout + CONNECTION_RETRY_MILLISECONDS;
		try {
			Thread.sleep(CONNECTION_RETRY_MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException (e);
		}
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
			listener.notify(adapter.queue().queueId(), NotificationType.TIMEOUT, ServerLabels.get("server.exception.timeout"));
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
			listener.notify(adapter.queue().queueId(), NotificationType.UNDETERMINED_EXCEPTION, message);
		}
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
	public String toHex(byte[] data) {
		return DatatypeConverter.printHexBinary(data);
	}
	
	/**
	 * Converts hex representation into a byte array
	 * @param hexData Data to be converted.
	 * @return Byte[] representing the data.
	 */
	public byte[] fromHex(String hexData) {
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
	 * Direct connector.
	 * @param queueId Id of the queue.
	 * @return Direct connector instance. Never null.
	 */
	public IDirectConnectorQueue directConnector(String queueId) {
		IDirectConnectPrintJobManager manager = 
				(IDirectConnectPrintJobManager) RemotePrintJobManagerFactory
					.getRemotePrintJobManager(queueId);
		IDirectConnectorQueue queue = manager.directConnector(queueId);
		return queue;
	}

	/**
	 * Serializes a given image into a hex one.
	 * @param img Image to serialized.
	 * @return A String representing a image.
	 */
	public String serializeImage(RenderedImage img) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(img, "png", output);
			output.flush();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return toHex(output.toByteArray());
	}
	
	/**
	 * Deserialize a previously serialized image.
	 * @param imgTxt Text representing the image.
	 * @return Rendered image.
	 */
	public RenderedImage deserializeImage(String imgTxt) {
		ByteArrayInputStream input = new ByteArrayInputStream(imgTxt.getBytes());
		RenderedImage returnValue = null;
		try {
			returnValue = ImageIO.read(input);
			return returnValue;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}
	
	/**
	 * Sets the field value.
	 * @param object Object containing the field.
	 * @param fieldName Name of the field to set.
	 * @param value Value to set in the field.
	 */
	public void setField(Object object, String fieldName, Object value) {
		try {
			Field field = object.getClass().getField(fieldName);
			field.setAccessible(true);
			field.set(object, value);
		} catch (Exception e) {
			LOG.info(object.getClass().getName() + " must define a field 'public " +
					value.getClass().getSimpleName() + " " + fieldName);
		}

	}
}
