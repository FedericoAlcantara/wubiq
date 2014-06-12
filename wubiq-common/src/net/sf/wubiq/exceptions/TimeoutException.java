/**
 * 
 */
package net.sf.wubiq.exceptions;


/**
 * Represents a timeout exception. Thrown when it is not possible to connect to the pageable within a specific time frame.
 * @author Federico Alcantara
 *
 */
public class TimeoutException extends Exception {
	private static final long serialVersionUID = 1L;

	public TimeoutException() {
		super("Timeout");
	}
}
