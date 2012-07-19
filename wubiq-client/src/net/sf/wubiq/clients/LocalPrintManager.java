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
import net.sf.wubiq.common.ParameterKeys;
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
	}
	
	public LocalPrintManager(String host) {
		setHost(host);
	}
	
	public LocalPrintManager(String host, String port) {
		this(host);
		setPort(port);
	}
	
	public LocalPrintManager(String host, String port, String applicationName) {
		this(host, port);
		setApplicationName(applicationName);
	}
		
	/**
	 * Process a single pending job.
	 * @param jobId Id of the job to be printed.
	 */
	protected void processPendingJob(String jobId) throws ConnectException {
		String parameter = printJobPollString(jobId);
		doLog("Process Pending Job:" + jobId);
		InputStream printData = null;
		try {
			String printServiceName = askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter);
			doLog("Job(" + jobId + ") printServiceName:" + printServiceName);
			String printRequestAttributesData = askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printRequestAttributes:" + printRequestAttributesData);
			String printJobAttributesData = askServer(CommandKeys.READ_PRINT_JOB_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") printJobAttributes:" + printJobAttributesData);
			String docAttributesData = askServer(CommandKeys.READ_DOC_ATTRIBUTES, parameter);
			doLog("Job(" + jobId + ") docAttributes:" + docAttributesData);
			String docFlavorData = askServer(CommandKeys.READ_DOC_FLAVOR, parameter);
			doLog("Job(" + jobId + ") docFlavor:" + docFlavorData);
			
			printData = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter);
			PrintService printService = getPrintServicesName().get(printServiceName);
			PrintRequestAttributeSet printRequestAttributeSet = PrintServiceUtils.convertToPrintRequestAttributeSet(printRequestAttributesData);
			PrintJobAttributeSet printJobAttributeSet = (PrintJobAttributeSet) PrintServiceUtils.convertToPrintJobAttributeSet(printJobAttributesData);
			DocAttributeSet docAttributeSet = PrintServiceUtils.convertToDocAttributeSet(docAttributesData);
			DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(docFlavorData);
			ClientPrintDirectUtils.print(jobId, printService, printRequestAttributeSet, printJobAttributeSet, docAttributeSet, docFlavor, printData);
			doLog("Job(" + jobId + ") printed.");
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
			doLog("Register Print Services");
			getPrintServicesName().clear();
			for (PrintService printService: PrintServiceUtils.getPrintServices()) {
				String printServiceName = PrintServiceUtils.serializeServiceName(printService, isDebugMode());
				getPrintServicesName().put(PrintServiceUtils.cleanPrintServiceName(printService), printService);
				doLog("Print service:" + printServiceName);
				StringBuffer printServiceRegister = new StringBuffer(printServiceName); 
				StringBuffer categories = new StringBuffer(PrintServiceUtils.serializeServiceCategories(printService, isDebugMode()));
				categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
					.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
				askServer(CommandKeys.REGISTER_PRINT_SERVICE, printServiceRegister.toString(), categories.toString(), 
						PrintServiceUtils.serializeDocumentFlavors(printService, isDebugMode()));
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
		options.addOption("a", "app", true, ClientLabels.get("client.command_line_app"));
		options.addOption("s", "servlet", true, ClientLabels.get("client.command_line_servlet"));
		options.addOption("u", "uuid", true, ClientLabels.get("client.command_line_uuid"));
		options.addOption("v", "verbose", false, ClientLabels.get("client.command_line_verbose"));
		options.addOption("i", "interval", false, ClientLabels.get("client.command_line_interval"));
		options.addOption("w", "wait", false, ClientLabels.get("client.command_line_wait"));
		
		initializeDefault(manager);
		
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar wubiq-client.jar", options, true);
			} else {
				if (line.hasOption("kill")) {
					manager.setKillManager(true);
				}
				if (line.hasOption("host")) {
					manager.setHost(line.getOptionValue("host"));
				}
				if (line.hasOption("port")) {
					manager.setPort(line.getOptionValue("port"));
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
				if (line.hasOption("interval")) {
					manager.setCheckPendingJobInterval(line.getOptionValue("interval"));
				}
				if (line.hasOption("wait")) {
					manager.setPrintingJobInterval(line.getOptionValue("interval"));
				}
				Thread r = new Thread(manager);
				r.start();
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar wubiq-client.jar", options, true);
		}
	}

}
