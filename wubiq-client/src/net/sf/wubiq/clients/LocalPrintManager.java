/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.ClientLabels;
import net.sf.wubiq.utils.ClientPrintDirectUtils;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keeps gathering printer information and reports it to the server.
 * This is the main element of the client.
 * If the connection to the client fails, it keeps trying until the program is closed.
 * As soon as connection is established identifies itself, sends the probable computer name
 * and, if defined, a unique id (UUID). If no unique id is defined, the uuid is automatically
 * generated.
 * @author Federico Alcantara
 *
 */
public class LocalPrintManager extends AbstractLocalPrintManager {
	private static final Log LOG = LogFactory.getLog(LocalPrintManager.class);
	private Map<String, PrintService>printServicesName;
	private long lastServerTimestamp = -1;
	private Set<Long> registeredJobs;
	private Set<String> registeredPrintServices;
	
	public LocalPrintManager() {
		super();
	}
	
	
	public LocalPrintManager(String applicationName, String connections) {
		setApplicationName(applicationName);
	}
	

	/**
	 * Process a single pending job.
	 * @param jobId Id of the job to be printed.
	 */
	@Override
	protected synchronized void processPendingJob(String jobId, String printServiceName) throws ConnectException {
		Long jobIdLong = Long.parseLong(jobId);
		// This will prevent to that a print job is taken more than once.
		if (getRegisteredJobs().contains(jobIdLong)) {
			return;
		}
		String parameter = printJobPollString(jobId);
		Object printData = null;
		boolean closePrintJob = false;
		try {
			if (Is.emptyString(printServiceName)) { // this job is already closed
				try {
					askServer(CommandKeys.CLOSE_PRINT_JOB, parameter);
					doLog("Job(" + jobId + ") closing print job.", 0);
				} catch (Exception e) {
					doLog(e.getMessage()); // this is not a desirable to show error
				}
				return;
			}
			// This will prevent to that more than one job is sent to the same print service while printing.
			if (getRegisteredPrintServices().contains(printServiceName)) {
				return;
			}
			doLog("Process Pending Job: " + jobId, 0);
			doLog("Job(" + jobId + ") printServiceName:" + printServiceName, 5);
			String printRequestAttributesData = askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printRequestAttributes:" + printRequestAttributesData, 5);
			String printJobAttributesData = askServer(CommandKeys.READ_PRINT_JOB_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printJobAttributes:" + printJobAttributesData, 5);
			String docAttributesData = askServer(CommandKeys.READ_DOC_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") docAttributes:" + docAttributesData, 5);
			String docFlavorData = askServer(CommandKeys.READ_DOC_FLAVOR, parameter);
			doLog("Job(" + jobId + ") docFlavor:" + docFlavorData, 5);
			
			// We must read the input stream to set the type of connection at job level.
			printData = pollServer(CommandKeys.READ_PRINT_JOB, parameter);
			// printData must come blank for directConnect to work.
			String isDirectConnectData = "false";
			try {
				isDirectConnectData = askServer(CommandKeys.READ_IS_DIRECT_CONNECT, parameter);
			} catch (ConnectException e) {
				isDirectConnectData = "false";
			}
			doLog("Job(" + jobId + ") isDirectConnect:" + isDirectConnectData, 5);
			String isCompressionEnabledData = "false";
			try {
				isCompressionEnabledData = askServer(CommandKeys.READ_IS_COMPRESSED, parameter);
			} catch (ConnectException e) {
				isCompressionEnabledData = "false";
			}
			doLog("Job(" + jobId + ") serverSupportsCompression:" + isCompressionEnabledData, 5);

			PrintService printService = getPrintServicesName().get(printServiceName);
			PrintRequestAttributeSet printRequestAttributeSet = PrintServiceUtils.convertToPrintRequestAttributeSet(printRequestAttributesData);
			PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(printJobAttributesData);
			DocAttributeSet docAttributeSet = PrintServiceUtils.convertToDocAttributeSet(docAttributesData);
			DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(docFlavorData);
			
			boolean isDirectConnect = "true".equalsIgnoreCase(isDirectConnectData);
			boolean serverSupportsCompression = "true".equalsIgnoreCase(isCompressionEnabledData);
			boolean forceSerialized = false;
			
			if (forceSerializedBySystem()) {
				isDirectConnect = false;
			}

			if (isDirectConnect) {
				if (!docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PAGEABLE) &&
						!docFlavor.equals(DocFlavor.SERVICE_FORMATTED.PRINTABLE)) {
					if (PrintServiceUtils.supportDocFlavor(printService, docFlavor)) {
						forceSerialized = true; // this is different this is telling to handle as pageable with direct connect
						docFlavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
					}
				}
			}
			if (isDirectConnect) {
				// Only same print service requests are put in queue
				DirectPrintManager manager = null;
				if ("true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION)) 
						|| forceSerialized) {
					// This automatically starts the service.
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					IOUtils.INSTANCE.copy((InputStream)printData, out);
					printData = new ByteArrayInputStream(out.toByteArray());
					manager = createDirectPrintManager(
							jobId,
							printService,
							printServiceName,
							printRequestAttributeSet,
							printJobAttributeSet,
							docAttributeSet,
							isDebugMode(),
							getDebugLevel(),
							serverSupportsCompression,
							docFlavor,
							lastSessionId(),
							(InputStream)printData);
				} else {
					if (serverSupportsCompression) {
						directServerNotSerialized(jobId, DirectConnectCommand.START);
					} else {
						directServer(jobId, DirectConnectCommand.START, parameter);
					}
					manager = createDirectPrintManager (
							jobId,
							printService,
							printServiceName,
							printRequestAttributeSet,
							printJobAttributeSet,
							docAttributeSet,
							isDebugMode(),
							getDebugLevel(),
							serverSupportsCompression,
							lastSessionId());
				}
				manager.setConnections(getConnections());
				manager.setApplicationName(getApplicationName());
				manager.setServletName(getServletName());
				manager.setUuid(getUuid());
				manager.setLocalPrintManager(this);
				runManager(manager, printServiceName, jobId);
			} else {
				// Single job, single print service other jobs or print service wait in queue.
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtils.INSTANCE.copy((InputStream)printData, out);
				printData = new ByteArrayInputStream(out.toByteArray());
				print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, (InputStream)printData);
				closePrintJob = true;
			}
		} catch (ConnectException e) {
			closePrintJob = false;
			doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
			LOG.error(e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
			LOG.error(e.getMessage(), e);
		} catch (Exception e) {
			doLog("Job(" + jobId + ") failed:" + e.getMessage(), 0);
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (printData != null && printData instanceof InputStream) {
					((InputStream)printData).close();
			}
			} catch (IOException e) {
				doLog(e.getMessage());
			}
			try {
				if (closePrintJob) {
					askServer(CommandKeys.CLOSE_PRINT_JOB, parameter);
					doLog("Job(" + jobId + ") closing print job.", 0);
				}
			} catch (Exception e) {
				doLog(e.getMessage()); // this is not a desirable to show error
			}
		}
	}
 	
	/**
	 * Closes the print job.
	 * @param jobId Job id to close.
	 */
	protected void closePrintJob(String jobId) {
		try {
			askServer(CommandKeys.CLOSE_PRINT_JOB, printJobPollString(jobId));
			doLog("Job(" + jobId + ") closing print job.", 0);
		} catch (Exception e) {
			doLog(e.getMessage()); // this is not a desirable to show error
		}
	}
	
	/**
	 * Checks the system property and validates if it is set to use old routines.
	 * @return True if force the use of old routines.
	 */
	protected boolean forceSerializedBySystem() {
		return "true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION));
	}
	
	/**
	 * Creates a Direct print manager. This is a method to be intercepted by tests.
	 * @param jobIdString Id of the print job.
	 * @param printService Print service to print the job to.
	 * @param printServiceName Name of the print service as registered in wubiq.
	 * @param printRequestAttributeSet Print request attributes.
	 * @param printJobAttributeSet Print job attributes.
	 * @param docAttributeSet Document attributes.
	 * @param debugMode Debug mode
	 * @param debugLevel Debug Level
	 * @param serverSupportsCompression Indicates if the direct print manager supports compression.
	 * @param docFlavor Doc flavor of the data to be printed.
	 * @param printData Data to be printed.
	 * @param jobSessionId Job session id.
	 * @return A new instance of Direct print manager.
	 * @throws IOException
	 */
	protected DirectPrintManager createDirectPrintManager(String jobIdString, PrintService printService,
			String printServiceName,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			boolean debugMode,
			int debugLevel,
			boolean serverSupportsCompression,
			DocFlavor docFlavor,
			String jobSessionId,
			InputStream printData) {
		return new DirectPrintManager(
				jobIdString,
				printService,
				printServiceName,
				printRequestAttributeSet,
				printJobAttributeSet,
				docAttributeSet,
				isDebugMode(),
				getDebugLevel(),
				serverSupportsCompression,
				docFlavor,
				jobSessionId,
				printData);
	}
	
	/**
	 * Creates a Direct print manager. This is a method to be intercepted by tests.
	 * @param jobIdString Id of the print job.
	 * @param printService Print service to print the job to.
	 * @param printServiceName Name of the print service as registered in wubiq.
	 * @param printRequestAttributeSet Print request attributes.
	 * @param printJobAttributeSet Print job attributes.
	 * @param docAttributeSet Document attributes.
	 * @param debugMode Debug mode
	 * @param debugLevel Debug Level
	 * @param serverSupportsCompression Indicates if the direct print manager supports compression.
	 */
	protected DirectPrintManager createDirectPrintManager(String jobIdString, PrintService printService,
			String printServiceName,
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet,
			boolean debugMode,
			int debugLevel,
			boolean serverSupportsCompression, 
			String jobSessionId) {
		return new DirectPrintManager (
				jobIdString,
				printService,
				printServiceName,
				printRequestAttributeSet,
				printJobAttributeSet,
				docAttributeSet,
				isDebugMode(),
				getDebugLevel(),
				serverSupportsCompression, 
				jobSessionId);
	}
	
	/**
	 * Runs a stand alone manager.
	 * @param manager Manager to be run.
	 * @param printServiceName Name of the service.
	 * @param jobId Job id.
	 */
	protected void runManager(DirectPrintManager manager, String printServiceName, String jobId) {
		Thread thread = new Thread(manager, printServiceName + "(" + jobId + ")");
		registerJob(Long.parseLong(jobId));
		holdPrintService(printServiceName);		
		thread.start();
	}
	
	/**
	 * Registers a print job, so it can't be re-processed again.
	 * @param jobId Job id.
	 */
	private synchronized void registerJob(Long jobId) {
		doLog("Registering job: " + jobId, 3);
		getRegisteredJobs().add(jobId);
	}
	
	/**
	 * Unregisters and notifies that job has finished.
	 * @param jobId Job id to unregister.
	 */
	protected synchronized void closePrintJob(Long jobId) {
		closePrintJob(jobId.toString());
	}

	/**
	 * Unregisters and notifies that job has finished.
	 * @param jobId Job id to unregister.
	 */
	protected synchronized void unRegisterJob(Long jobId) {
		getRegisteredJobs().remove(jobId);
		doLog("Un-registered job: " + jobId, 3);
	}
	
	/**
	 * @param printServiceName
	 */
	private synchronized void holdPrintService(String printServiceName) {
		doLog("Holding print service: " + printServiceName, 3);
		getRegisteredPrintServices().add(printServiceName);
	}
	
	/**
	 * Releases the print service, so other print jobs can be served.
	 * @param printServiceName Print service name to release.
	 */
	protected synchronized void releasePrintService(String printServiceName) {
		getRegisteredPrintServices().remove(printServiceName);
		doLog("Released print service: " + printServiceName, 3);
	}
	
	/**
	 * Performs the printing. This is a needed method for fulfilling tests requirements. 
	 * @param jobId Identifying job id.
	 * @param printService PrintService to print to.
	 * @param printRequestAttributeSet Attributes to be set on the print service.
	 * @param printJobAttributeSet Attributes for the print job.
	 * @param docAttributeSet Attributes for the document.
	 * @param docFlavor Document flavor.
	 * @param printData Document as input stream to sent to the print service.
	 * @throws IOException if service is not found and no default service.
	 */
	protected void print(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet, 
			DocAttributeSet docAttributeSet,
			DocFlavor docFlavor,
			InputStream printData) throws IOException {
		ClientPrintDirectUtils.print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
	}
		
	/**
	 * Creates the print job poll string.
	 * @param jobId Id of the print job to pull from the server.
	 * @return String (url) for polling the server.
	 */
	private String printJobPollString(String jobId) {
		StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(jobId);
		return parameter.toString();
	}
	
	/**
	 * Registers all valid local print services to the remote server.
	 */
	protected void registerPrintServices() throws ConnectException {
		boolean reload = false;
		Map<String, PrintService>newPrintServices = new HashMap<String, PrintService>();
		for (PrintService printService: PrintServiceUtils.getPrintServices()) {
			if ("Microsoft XPS document writer".equalsIgnoreCase(printService.getName())) {
				continue; // This is a hanger printer
			}
			String printServiceName = PrintServiceUtils.cleanPrintServiceName(printService);
			newPrintServices.put(printServiceName, printService);
			if (!getPrintServicesName().containsKey(printServiceName)) {
				reload = true;
			}
		}
		if (newPrintServices.size() != getPrintServicesName().size()) {
			reload = true;
		}

		long serverTimestamp = -2l;
		try {
			serverTimestamp = Long.parseLong(askServer(CommandKeys.SERVER_TIMESTAMP));
		} catch (NumberFormatException e) {
			serverTimestamp = -2l;
		}
		if (serverTimestamp != lastServerTimestamp) {
			reload = true;
		}
		if (!reload) {
			try {
				askServer(CommandKeys.IS_ACTIVE);
			} catch (ConnectException e) {
				getPrintServicesName().clear();
				reload = true;
				return;
			}
		}
		if (reload) {
			registerComputerName();
			// Gather printServices.
			doLog("Register Print Services", 5);
			getPrintServicesName().clear();
			for (PrintService printService: PrintServiceUtils.getPrintServices()) {
				if ("Microsoft XPS document writer".equalsIgnoreCase(printService.getName())) {
					continue; // This is a hanger printer
				}
				String printServiceName = PrintServiceUtils.serializeServiceName(printService, isDebugMode());
				getPrintServicesName().put(PrintServiceUtils.cleanPrintServiceName(printService), printService);
				doLog("Print service:" + printServiceName, 5);
				StringBuffer printServiceRegister = new StringBuffer(printServiceName); 
				StringBuffer categories = new StringBuffer(PrintServiceUtils.serializeServiceCategories(printService, isDebugMode()));
				boolean forceSerialization = "true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION))
						|| forceSerializedBySystem();
				categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
					.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
				askServer(printServiceType(), printServiceRegister.toString(), categories.toString(), 
						ParameterKeys.PRINT_SERVICE_DOC_FLAVORS
							+ParameterKeys.PARAMETER_SEPARATOR
							+DirectConnectUtils.INSTANCE.serialize(printService.getSupportedDocFlavors()),
						DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER 
							+ ParameterKeys.PARAMETER_SEPARATOR
							+ (forceSerialization ?
									"FALSE" : "TRUE"),
						DirectConnectKeys.DIRECT_CONNECT_CLIENT_VERSION
							+ ParameterKeys.PARAMETER_SEPARATOR
							+ Labels.VERSION);
				PrinterType printerType = PrintServiceUtils.printerType(printService);
				doLog(printServiceName + " -> " + printerType, 0);				
			}
			lastServerTimestamp = serverTimestamp;
		}
	}
		
	/**
	 * Print service type.
	 * @return Type of print service.
	 */
	protected String printServiceType() {
		return CommandKeys.REGISTER_PRINT_SERVICE_V2;
	}

	protected Map<String, PrintService> getPrintServicesName() {
		if (printServicesName == null) {
			printServicesName = new HashMap<String, PrintService>();
		}
		return printServicesName;
	}
	/**
	 * @return the registeredJobs
	 */
	private Set<Long> getRegisteredJobs() {
		if (registeredJobs == null) {
			registeredJobs = new HashSet<Long>();
		}
		return registeredJobs;
	}

	/**
	 * @return The registered print services
	 */
	private Set<String> getRegisteredPrintServices() {
		if (registeredPrintServices == null) {
			registeredPrintServices = new HashSet<String>();
		}
		return registeredPrintServices;
	}
	
	/**
	 * Parses the command line and starts an instance of a client.
	 * @param args Command line arguments.
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception {
		LocalPrintManager manager = new LocalPrintManager();
		System.out.println(ClientLabels.get("client.version", Labels.VERSION));
		System.out.println("http://sourceforge.net/projects/wubiq\n");
		Options options = new Options();
		options.addOption("?", "help", false, ClientLabels.get("client.command_line_help"));
		options.addOption("k", "kill", false, ClientLabels.get("client.command_line_kill"));
		options.addOption("h", "host", true, ClientLabels.get("client.command_line_host"));
		options.addOption("p", "port", true, ClientLabels.get("client.command_line_port"));
		options.addOption("c", "connections", true, ClientLabels.get("client.command_line_connections"));
		options.addOption("a", "app", true, ClientLabels.get("client.command_line_app"));
		options.addOption("s", "servlet", true, ClientLabels.get("client.command_line_servlet"));
		options.addOption("u", "uuid", true, ClientLabels.get("client.command_line_uuid"));
		options.addOption("g", "groups", true, ClientLabels.get("client.command_line_groups"));
		options.addOption("v", "verbose", false, ClientLabels.get("client.command_line_verbose"));
		options.addOption("l", "logLevel", true, ClientLabels.get("client.command_line_loglevel"));
		options.addOption("i", "interval", true, ClientLabels.get("client.command_line_interval"));
		options.addOption("w", "wait", true, ClientLabels.get("client.command_line_wait"));
		
		initializeDefault(manager);
		
		CommandLineParser parser = new GnuParser();
		String host = null;
		String port = null;
		String connectionsString = null;
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar wubiq-client.jar", "java properties:\n"
						+ "-D" + PropertyKeys.WUBIQ_PRINTERS_PHOTO + " " + ClientLabels.get("PropertyKeys.WUBIQ_PRINTERS_PHOTO") + ".\n"
						+ "-D" + PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ + " " + ClientLabels.get("PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ") + ".\n"
						+ "-D" + PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX + " " + ClientLabels.get("PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX") + ".\n"
						+ "-D" + PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT + " " + ClientLabels.get("PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT") + ".\n"
						+ "-D" + PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL + " " + ClientLabels.get("PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL") + ".\n"
						+ "\n"
						+ "-D" + PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES + " " + ClientLabels.get("PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES") + ".\n"
						+ "-D" + PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION + " " + ClientLabels.get("PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION") + ".\n"
						+ "\n"
						+ "", options, "\n For more information visit wubiq's site: http://sourceforge.net/projects/wubiq", true);
			}
			if (line.hasOption("kill")) {
				manager.setKillManager(true);
			}
			if (line.hasOption("host")) {
				host = line.getOptionValue("host");
			}
			if (line.hasOption("port")) {
				port = line.getOptionValue("port");
			}
			if (line.hasOption("connections")) {
				connectionsString = line.getOptionValue("connections");
			}
			if (line.hasOption("app")) {
				manager.setApplicationName(line.getOptionValue("app"));
			}
			if (line.hasOption("servlet")) {
				manager.setServletName(line.getOptionValue("servlet"));
			}
			if (line.hasOption("uuid")) {
				manager.setUuid(line.getOptionValue("uuid"));
			}
			if (line.hasOption("groups")) {
				manager.setGroups(line.getOptionValue("groups"));
			}
			if (line.hasOption("verbose")) {
				manager.setDebugMode(true);
			}
			if (line.hasOption("logLevel")) {
				String level = line.getOptionValue("logLevel");
				try {
					manager.setDebugLevel(Integer.parseInt(level));
				} catch (NumberFormatException e) {
					manager.setDebugLevel(0);
				}
			}
			if (line.hasOption("interval")) {
				manager.setCheckPendingJobInterval(line.getOptionValue("interval"));
			}
			if (line.hasOption("wait")) {
				manager.setPrintingJobInterval(line.getOptionValue("interval"));
			}
			addConnectionsString(manager, manager.hostPortConnection(host, port));
			addConnectionsString(manager, connectionsString);
			Thread r = new Thread(manager, "LocalPrintManager");
			r.start();
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar wubiq-client.jar", options, true);
		}
	}

}
