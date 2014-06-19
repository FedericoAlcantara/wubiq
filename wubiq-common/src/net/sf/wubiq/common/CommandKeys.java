/**
 * 
 */
package net.sf.wubiq.common;

/**
 * Constants used for client/server commands recognition.
 * @author Federico Alcantara
 *
 */
public final class CommandKeys {
	private CommandKeys() {
	}
	public static final String KILL_MANAGER = "k1";
	public static final String IS_KILLED = "k2";
	public static final String IS_ACTIVE = "a1";
	public static final String IS_REFRESHED = "a2";
	public static final String BRING_ALIVE = "b1";
	public static final String REGISTER_COMPUTER_NAME = "c1";
	public static final String REGISTER_PRINT_SERVICE = "p0";
	public static final String REGISTER_PRINT_SERVICE_V2 = "p0b";
	public static final String REGISTER_MOBILE_PRINT_SERVICE = "pm";
	public static final String SHOW_PRINT_SERVICES = "p1";
	public static final String PENDING_JOBS = "j0";
	public static final String READ_PRINT_SERVICE_NAME = "p2";
	public static final String READ_PRINT_REQUEST_ATTRIBUTES = "a3";
	public static final String READ_PRINT_JOB_ATTRIBUTES = "a4";
	public static final String READ_DOC_ATTRIBUTES = "a5";
	public static final String READ_DOC_FLAVOR="f0";
	public static final String READ_PRINT_JOB = "j1";
	public static final String CLOSE_PRINT_JOB = "j2";
	public static final String PRINT_TEST_PAGE = "t1";
	public static final String SERVER_TIMESTAMP = "STS";
	public static final String PRINT_SERVICE_PENDING_JOBS = "j3";
	public static final String DIRECT_CONNECT = "dc";
}
