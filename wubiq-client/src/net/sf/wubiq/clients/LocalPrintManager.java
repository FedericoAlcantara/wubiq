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
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.ClientLabels;
import net.sf.wubiq.utils.ClientPrintDirectUtils;
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

			PrintService printService = getPrintServicesName().get(printServiceName);
			PrintRequestAttributeSet printRequestAttributeSet = PrintServiceUtils.convertToPrintRequestAttributeSet(printRequestAttributesData);
			PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(printJobAttributesData);
			DocAttributeSet docAttributeSet = PrintServiceUtils.convertToDocAttributeSet(docAttributesData);
			DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(docFlavorData);
			
			if ("true".equalsIgnoreCase(System.getProperty(DirectConnectKeys.DIRECT_CONNECT_FORCE_SERIALIZATION_PROPERTY))) {
				printData = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter);
				ClientPrintDirectUtils.print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
			} else {
				directServer(jobId, DirectConnectCommand.START, parameter);
				DirectPrintManager manager = new DirectPrintManager(
						jobId,
						printService,
						printRequestAttributeSet,
						printJobAttributeSet,
						docAttributeSet,
						docFlavor);
				manager.setConnections(getConnections());
				manager.setApplicationName(getApplicationName());
				manager.setServletName(getServletName());
				manager.setUuid(getUuid());
				manager.handleDirectPrinting();
			}			
			doLog("Job(" + jobId + ") printed.", 0);
		} catch (ConnectException e) {
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
				askServer(CommandKeys.CLOSE_PRINT_JOB, parameter);
				doLog("Job(" + jobId + ") closing print job.");
			} catch (Exception e) {
				doLog(e.getMessage()); // this is not a desirable to show error
			}
		}
	}
	
	/**
	 * Process a single pending job.
	 * @param jobId Id of the job to be printed.
	 */
	protected void OLD_processPendingJob(String jobId) throws ConnectException {
		String parameter = printJobPollString(jobId);
		doLog("Process Pending Job: " + jobId, 0);
		InputStream printData = null;
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
			
			printData = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter);
			PrintService printService = getPrintServicesName().get(printServiceName);
			PrintRequestAttributeSet printRequestAttributeSet = PrintServiceUtils.convertToPrintRequestAttributeSet(printRequestAttributesData);
			PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(printJobAttributesData);
			DocAttributeSet docAttributeSet = PrintServiceUtils.convertToDocAttributeSet(docAttributesData);
			DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(docFlavorData);
			ClientPrintDirectUtils.print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
			doLog("Job(" + jobId + ") printed.", 0);
		} catch (ConnectException e) {
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
				askServer(CommandKeys.CLOSE_PRINT_JOB, parameter);
				doLog("Job(" + jobId + ") closing print job.");
			} catch (Exception e) {
				doLog(e.getMessage()); // this is not a desirable to show error
			}
		}
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
				askServer(CommandKeys.REGISTER_PRINT_SERVICE, printServiceRegister.toString(), categories.toString(), 
						PrintServiceUtils.serializeDocumentFlavors(printService, isDebugMode()),
						DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER 
							+ ParameterKeys.PARAMETER_SEPARATOR
							+ ("true".equalsIgnoreCase(System.getProperty(DirectConnectKeys.DIRECT_CONNECT_FORCE_SERIALIZATION_PROPERTY)) ?
									"FALSE" : "TRUE"));
				PrinterType printerType = PrintServiceUtils.printerType(printService);
				LOG.info(printServiceName + " -> " + printerType);
			}
			lastServerTimestamp = serverTimestamp;
		}
	}

	private Map<String, PrintService> getPrintServicesName() {
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
						+ "-Dwubiq.printers.photo For specifying laser or photo capable printers\n"
						+ "-Dwubiq.printers.dotmatrixhq For high quality dot matrix printers (24pin) \n"
						+ "-Dwubiq.printers.dotmatrix For normal quality dot matrix printers (9pin) \n"
						+ "-Dwubiq.fonts.dotmatrix.default Specify default fonts for dot matrix printers\n"
						+ "-Dwubiq.fonts.dotmatrix.force.logical If true printing on dot matrix will only be done with java's logical fonts.\n"
						+ "\n"
						+ "-D" + DirectConnectKeys.DIRECT_CONNECT_FORCE_SERIALIZATION_PROPERTY + " If true communications between server and client uses old serialized implementation.\n"
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
			Thread r = new Thread(manager);
			r.start();
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar wubiq-client.jar", options, true);
		}
	}

}
