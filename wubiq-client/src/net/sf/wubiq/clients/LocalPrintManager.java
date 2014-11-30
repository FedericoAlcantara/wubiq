/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

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
	protected void processPendingJob(String jobId) throws ConnectException {
		String parameter = printJobPollString(jobId);
		doLog("Process Pending Job: " + jobId, 0);
		InputStream printData = null;
		boolean closePrintJob = true;
		try {
			String printServiceName = askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter);
			doLog("Job(" + jobId + ") printServiceName:" + printServiceName, 5);
			String printRequestAttributesData = askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printRequestAttributes:" + printRequestAttributesData, 5);
			String printJobAttributesData = askServer(CommandKeys.READ_PRINT_JOB_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printJobAttributes:" + printJobAttributesData, 5);
			String docAttributesData = askServer(CommandKeys.READ_DOC_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") docAttributes:" + docAttributesData, 5);
			String docFlavorData = askServer(CommandKeys.READ_DOC_FLAVOR, parameter);
			doLog("Job(" + jobId + ") docFlavor:" + docFlavorData, 5);
			String isDirectConnectData = askServer(CommandKeys.READ_IS_DIRECT_CONNECT);
			doLog("Job(" + jobId + ") isDirectConnect:" + isDirectConnectData, 5);

			PrintService printService = getPrintServicesName().get(printServiceName);
			PrintRequestAttributeSet printRequestAttributeSet = PrintServiceUtils.convertToPrintRequestAttributeSet(printRequestAttributesData);
			PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(printJobAttributesData);
			DocAttributeSet docAttributeSet = PrintServiceUtils.convertToDocAttributeSet(docAttributesData);
			DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(docFlavorData);
			boolean isDirectConnect = "true".equalsIgnoreCase(isDirectConnectData);
			
			if ("true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION))  ||
					!isDirectConnect) {
				printData = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter);
				print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
			} else {
				directServer(jobId, DirectConnectCommand.START, parameter);
				DirectPrintManager manager = createDirectPrintManager(
						jobId,
						printService,
						printRequestAttributeSet,
						printJobAttributeSet,
						docAttributeSet,
						isDebugMode(),
						getDebugLevel());
				manager.setConnections(getConnections());
				manager.setApplicationName(getApplicationName());
				manager.setServletName(getServletName());
				manager.setUuid(getUuid());
				manager.handleDirectPrinting();
			}			
			doLog("Job(" + jobId + ") printed.", 0);
		} catch (ConnectException e) {
			closePrintJob = false;
			LOG.error(e.getMessage(), e);
			throw e;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (printData != null) {
					printData.close();
				}
			} catch (IOException e) {
				doLog(e.getMessage());
			}
			try {
				if (closePrintJob) {
					askServer(CommandKeys.CLOSE_PRINT_JOB, parameter);
					doLog("Job(" + jobId + ") closing print job.");
				}
			} catch (Exception e) {
				doLog(e.getMessage()); // this is not a desirable to show error
			}
		}
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
	 * Creates a direct print manager.
	 * @param jobId Id of the job.
	 * @param printService PrintService to print to.
	 * @param printRequestAttributeSet Attributes to be set on the print service.
	 * @param printJobAttributeSet Attributes for the print job.
	 * @param docAttributeSet Attributes for the document.
	 * @param debugMode The state of the the debug mode.
	 * @param debugLevel The debug Level.
	 * @return an instance of a DirectPrintManager.
	 */
	protected DirectPrintManager createDirectPrintManager(String jobId, PrintService printService, 
			PrintRequestAttributeSet printRequestAttributeSet,
			PrintJobAttributeSet printJobAttributeSet,
			DocAttributeSet docAttributeSet,
			boolean debugMode,
			int debugLevel) {
		return new DirectPrintManager(
				jobId,
				printService,
				printRequestAttributeSet,
				printJobAttributeSet,
				docAttributeSet,
				isDebugMode(),
				getDebugLevel());
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
				String printServiceName = PrintServiceUtils.serializeServiceName(printService, isDebugMode());
				getPrintServicesName().put(PrintServiceUtils.cleanPrintServiceName(printService), printService);
				doLog("Print service:" + printServiceName, 5);
				StringBuffer printServiceRegister = new StringBuffer(printServiceName); 
				StringBuffer categories = new StringBuffer(PrintServiceUtils.serializeServiceCategories(printService, isDebugMode()));
				categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
					.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
				askServer(CommandKeys.REGISTER_PRINT_SERVICE_V2, printServiceRegister.toString(), categories.toString(), 
						ParameterKeys.PRINT_SERVICE_DOC_FLAVORS
							+ParameterKeys.PARAMETER_SEPARATOR
							+DirectConnectUtils.INSTANCE.serialize(printService.getSupportedDocFlavors()),
						DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER 
							+ ParameterKeys.PARAMETER_SEPARATOR
							+ ("true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION)) ?
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

	protected Map<String, PrintService> getPrintServicesName() {
		if (printServicesName == null) {
			printServicesName = new HashMap<String, PrintService>();
		}
		return printServicesName;
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
