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
	public static final String KILL_MANAGER = "killManager";
	public static final String IS_KILLED = "isKilled";
	public static final String IS_ACTIVE = "isActive";
	public static final String IS_REFRESHED = "isRefreshed";
	public static final String BRING_ALIVE = "bringAlive";
	public static final String REGISTER_COMPUTER_NAME = "registerComputerName";
	public static final String REGISTER_PRINT_SERVICE = "registerPrintService";
	public static final String REGISTER_MOBILE_PRINT_SERVICE = "registerMobilePrintService";
	public static final String SHOW_PRINT_SERVICES = "showPrintServices";
	public static final String PENDING_JOBS = "pendingJobs";
	public static final String READ_PRINT_SERVICE_NAME = "readPrintService";
	public static final String READ_DOC_FLAVOR = "readDocFlavor";
	public static final String READ_PRINT_ATTRIBUTES = "readPrintAttributes";
	public static final String READ_PRINT_JOB = "readPrintJob";
	public static final String CLOSE_PRINT_JOB = "closePrintJob";
	public static final String PRINT_TEST_PAGE = "printTestPage";
}
