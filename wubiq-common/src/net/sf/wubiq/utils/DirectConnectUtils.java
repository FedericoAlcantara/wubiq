/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import net.sf.wubiq.enums.NotificationType;
import net.sf.wubiq.exceptions.TimeoutException;
import net.sf.wubiq.interfaces.IAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.wrappers.GraphicParameter;

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
	 * @param adapter Associated adapter.
	 * @param listeners Set of listeners to be notified.
	 */
	public void notifyTimeout(IAdapter adapter, Set<IRemoteListener> listeners) {
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
	public void notifyException(IAdapter adapter, Set<IRemoteListener> listeners, String message) {
		for (IRemoteListener listener : listeners) {
			listener.notify(adapter.queue().queueId(), NotificationType.UNDETERMINED_EXCEPTION, message);
		}
	}
	
	/**
	 * Serialize an object and creates an stream for reading it.
	 * @param object Object to be serialized representation.
	 * @return A tuple of objects where the first contains the input stream and the second the length. 
	 */
	public Object[] serializeObject(Object object) {
		Object[] returnValue = new Object[2];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		GZIPOutputStream zipped = null;
		try {
			ObjectOutputStream objectOutput = new ObjectOutputStream(output);
			objectOutput.writeObject(object);
			objectOutput.flush();
			output.flush();
			ByteArrayOutputStream zippedOutput = new ByteArrayOutputStream();
			zipped = new GZIPOutputStream(zippedOutput);
			IOUtils.INSTANCE.copy(new ByteArrayInputStream(output.toByteArray()), zipped);
			zipped.flush();
			returnValue[0] = new ByteArrayInputStream(zippedOutput.toByteArray());
			returnValue[1] = zippedOutput.toByteArray().length;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (zipped != null) {
				try {
					zipped.close();
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
		}
		return returnValue;
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
	 * De serializes a given string.
	 * @param serialized String representing the object.
	 * @return Object de-serialized. Null if can't be converted.
	 */
	public Object deserializeObject(InputStream serialized) {
		Object returnValue = null;
		if (serialized != null) {
			GZIPInputStream unzipped = null;
			try {
				unzipped = new GZIPInputStream(serialized);
				ObjectInputStream objectInput = new ObjectInputStream(unzipped);
				returnValue = objectInput.readObject();
			} catch (IOException e) {
				LOG.debug(e.getMessage());
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				if (unzipped != null) {
					try {
						unzipped.close();
					} catch (IOException e) {
						LOG.debug(e.getMessage());
					}
				}
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
	 * Converts a method to a compatible graphic parameters.
	 * This is necessary because arguments might have null values.
	 * @param method Method to assess.
	 * @param args Arguments of objects.
	 * @return Array of graphic parameters.
	 */
	public GraphicParameter[] convertToGraphicParameters(Method method, Object... args) {
		GraphicParameter[] parameters = new GraphicParameter[method.getParameterTypes().length];
		for (int index = 0; index < parameters.length; index++) {
			parameters[index] = new GraphicParameter(method.getParameterTypes()[index], args[index]);
		}
		return parameters;
	}
	
	/**
	 * Converts a byte array to integer.
	 * @param encodedValue Value to be converted.
	 * @return returns a integer representation.
	 */
	public int byteArrayToInt4(byte[] encodedValue) {
	    int index = 0;
	    int value = encodedValue[index++] << Byte.SIZE * 3;
	    value ^= (encodedValue[index++] & 0xFF) << Byte.SIZE * 2;
	    value ^= (encodedValue[index++] & 0xFF) << Byte.SIZE * 1;
	    value ^= (encodedValue[index++] & 0xFF);
	    return value;
	}

	/**
	 * Converts an integer into a byte array of four bytes.
	 * @param value Value to be converted.
	 * @return Array of bytes representing the integer.
	 */
	public byte[] intToByteArray4(int value) {
	    int index = 0;
	    byte[] encodedValue = new byte[Integer.SIZE / Byte.SIZE];
	    encodedValue[index++] = (byte) (value >> Byte.SIZE * 3);
	    encodedValue[index++] = (byte) (value >> Byte.SIZE * 2);   
	    encodedValue[index++] = (byte) (value >> Byte.SIZE);   
	    encodedValue[index++] = (byte) value;
	    return encodedValue;
	}

}
