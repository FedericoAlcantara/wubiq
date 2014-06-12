/**
 * 
 */
package net.sf.wubiq.adapters;

import java.io.Serializable;


/**
 * Returned data. Also can represent an exception.
 * @author Federico Alcantara
 *
 */
public class ReturnedData implements Serializable {
	private static final long serialVersionUID = 1L;
	private Object data;
	
	private boolean exception;
	
	private boolean runtimeException;
	
	public ReturnedData(Object data) {
		this.data = data;
		this.exception = false;
		this.runtimeException = false;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @return the exception
	 */
	public boolean isException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(boolean exception) {
		this.exception = exception;
	}

	/**
	 * @return the runtimeException
	 */
	public boolean isRuntimeException() {
		return runtimeException;
	}

	/**
	 * @param runtimeException the runtimeException to set
	 */
	public void setRuntimeException(boolean runtimeException) {
		this.runtimeException = runtimeException;
	}

}
