/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import javax.print.PrintService;
import javax.print.attribute.Attribute;

import net.sf.wubiq.common.AttributeOutputStream;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.utils.ClientLabels;
import net.sf.wubiq.utils.ClientPrintDirectUtils;
import net.sf.wubiq.utils.ClientProperties;
import net.sf.wubiq.utils.Is;
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
public class LocalPrintManager implements Runnable {
	private static final Log LOG = LogFactory.getLog(LocalPrintManager.class);
	private String host;
	private String port;
	private String applicationName;
	private String servletName;
	private String uuid;
	private boolean killManager;
	private boolean refreshServices;
	private boolean debugMode;
	
	public LocalPrintManager() {
	}
	
	public LocalPrintManager(String host) {
		this.host = host;
	}
	
	public LocalPrintManager(String host, String port) {
		this(host);
		this.port = port;
	}
	
	public LocalPrintManager(String host, String port, String applicationName) {
		this(host, port);
		this.applicationName = applicationName;
	}
	
	@Override
	public void run() {
		if (killManager) {
			killManager();
			System.out.println(ClientLabels.get("client.closing_local_manager"));
		} else {
			refreshServices = true;
			while (!isKilled()) {
				try {
					if (refreshServices) {
						registerPrintServices();
						refreshServices = false;
					}
					String[] pendingJobs = getPendingJobs();
					for (String pendingJob : pendingJobs) {
						processPendingJob(pendingJob);
						Thread.sleep(3000);
					}
					Thread.sleep(10000);
				} catch (ConnectException e) {
					LOG.debug(e.getMessage());
					refreshServices = false;
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
					break;
				}
			}
			killManager();
		}
	}
	
	/**
	 * Kills the manager.
	 */
	protected void killManager() {
		doLog("Kill Manager");
		try {
			askServer(CommandKeys.KILL_MANAGER);
		} catch (ConnectException e) {
			doLog(e.getMessage());
		}
	}

	/**
	 * Allow manager to re-register.
	 */
	protected void bringAlive() {
		doLog("Bring alive");
		try {
			askServer(CommandKeys.BRING_ALIVE);
		} catch (ConnectException e) {
			doLog(e.getMessage());
		}
	}
	
	/**
	 * Process a single pending job.
	 * @param jobId Id of the job to be printed.
	 */
	protected void processPendingJob(String jobId) throws ConnectException {
		StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(jobId);
		doLog("Process Pending Job:" + jobId);
		InputStream stream = null;
		try {
			String printServiceName = askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter.toString());
			doLog("Job(" + jobId + ") printServiceName:" + printServiceName);
			String attributesData = askServer(CommandKeys.READ_PRINT_ATTRIBUTES, parameter.toString());
			doLog("Job(" + jobId + ") attributesData:" + attributesData);
			stream = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter.toString());
			doLog("Job(" + jobId + ") stream:" + stream);
			doLog("Job(" + jobId + ") print pdf");
			ClientPrintDirectUtils.print(jobId, printServiceName, PrintServiceUtils.convertToAttributes(attributesData), stream);
			doLog("Job(" + jobId + ") printed.");
			askServer(CommandKeys.CLOSE_PRINT_JOB, parameter.toString());
			doLog("Job(" + jobId + ") close print job.");
		} catch (ConnectException e) {
			throw e;
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (IOException e) {
				doLog(e.getMessage());
			}
		}
	}
	
	/**
	 * Ask to the server for a list of all pending jobs for this client instance.
	 * @return A list of pending jobs, or a zero element String array. Never null.
	 */
	protected String[] getPendingJobs() throws ConnectException {
		doLog("Get Pending Jobs");
		String[]returnValue = new String[]{};
		String pendingJobResponse = askServer(CommandKeys.PENDING_JOBS);
		if (!Is.emptyString(pendingJobResponse)
				&& pendingJobResponse.startsWith(ParameterKeys.PENDING_JOB_SIGNATURE)) {
			returnValue = pendingJobResponse.substring(ParameterKeys.PENDING_JOB_SIGNATURE.length())
				.split(ParameterKeys.CATEGORIES_SEPARATOR);
		}
		doLog("pending jobs:" + pendingJobResponse);
		return returnValue;
	}
	
	/**
	 * Registers the probable computer name, and initializes remote print services for this client instance.
	 */
	protected void registerComputerName() throws ConnectException {
		doLog("Register Computer Name");
		// Gather computer Name.
		StringBuffer computerName = new StringBuffer(ParameterKeys.COMPUTER_NAME)
		.append(ParameterKeys.PARAMETER_SEPARATOR); 
		try {
			computerName.append(InetAddress.getLocalHost().getHostName());
			doLog("Register computer name:" + computerName);
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		}
		askServer(CommandKeys.REGISTER_COMPUTER_NAME, computerName.toString());
	}

	/**
	 * Registers all valid local print services to the remote server.
	 */
	protected void registerPrintServices() throws ConnectException {
		registerComputerName();
		// Gather printServices.
		doLog("Register Print Services");
		for (PrintService printService: PrintServiceUtils.getPrintServices()) {
			doLog("Print service:" + printService.getName());
			StringBuffer printServiceRegister = new StringBuffer(ParameterKeys.PRINT_SERVICE_NAME)
					.append(ParameterKeys.PARAMETER_SEPARATOR)
					.append(printService.getName());
			StringBuffer categories = new StringBuffer("");
			for (Class<? extends Attribute> category : PrintServiceUtils.getCategories(printService)) {
				if (categories.length() > 0) {
					categories.append(ParameterKeys.CATEGORIES_SEPARATOR);
				}
				categories.append(category.getName())
					.append(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER);
				String attributes = "";
				try {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					AttributeOutputStream encoder = new AttributeOutputStream(stream);
					encoder.writeAttributes(PrintServiceUtils.getCategoryAttributes(printService, category));
					encoder.close();
					attributes = stream.toString();
				} catch (Exception e) {
					doLog(e.getMessage());
				}

				categories.append(attributes);
			}
			categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
				.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
			askServer(CommandKeys.REGISTER_PRINT_SERVICE, printServiceRegister.toString(), categories.toString());
		}
	}

	/**
	 * Asks the server if this instance of the client should be closed. This will allow for remote cancellation of client.
	 * (Not yet implemented).
	 * @return
	 */
	protected boolean isKilled() {
		boolean returnValue = false;
		try {
			if (askServer(CommandKeys.IS_KILLED).equals("1")) {
				returnValue = true;
			}
		} catch (ConnectException e) {
			doLog(e.getMessage());
		}
		doLog("Is Killed?" + returnValue);
		return returnValue;
	}
	
	/**
	 * Send command to server and returns its response as a string.
	 * @param command Command to send to the server.
	 * @param parameters List of parameters to be sent.
	 * @return Response from server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	protected String askServer(String command, String...parameters) throws ConnectException {
		return (String)pollServer(command, parameters);
	}
		
	/**
	 * Send command to server and returns it response as an object.
	 * @param command Command to send to the server.
	 * @param parameters List of parameters to be sent.
	 * @return Response from server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	protected Object pollServer(String command, String... parameters) throws ConnectException {
		Object returnValue = "";
		URL webUrl = null;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			webUrl = new URL(getEncodedUrl(command, parameters));
			connection = (HttpURLConnection) webUrl.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
			Object content = connection.getContent();
			if (connection.getContentType() != null) {
				if (connection.getContentType().equals("application/pdf")) {
					returnValue = (InputStream)content;
				} else if (connection.getContentType().equals("text/html")) {
					reader = new BufferedReader(new InputStreamReader((InputStream)content));
					StringBuffer value = new StringBuffer("");
					while (reader.ready()) {
						value.append(reader.readLine());
					}
					returnValue = value.toString();
				}
			}
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage(), e);
		} catch (UnknownServiceException e) {
			doLog(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {			
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Properly forms the url and encode the parameters so that servers can receive them correctly.
	 * @param command Command to be encoded as part of the url.
	 * @param parameters Arrays of parameters in the form parameterName=parameterValue that will be appended to the url.
	 * @return Url string with parameterValues encoded.
	 */
	protected String getEncodedUrl(String command, String... parameters) {
		StringBuffer url = new StringBuffer(hostServletUrl())
			.append('?')
			.append(ParameterKeys.UUID)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(getUuid());
		if (!Is.emptyString(command)) {
			url.append('&')
			.append(ParameterKeys.COMMAND)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(command);
		}
		for (String parameter: parameters) {
			String parameterString = parameter;
			if (parameter.contains("=")) {
				String parameterName = parameter.substring(0, parameter.indexOf("="));
				String parameterValue = parameter.substring(parameter.indexOf("=") + 1);
				try {
					parameterValue = URLEncoder.encode(parameterValue, "UTF-8");
					parameterString = parameterName + "=" + parameterValue;
				} catch (UnsupportedEncodingException e) {
					LOG.error(e.getMessage());
				}
			}
			url.append('&')
					.append(parameterString);
		}
		return url.toString();
	}
	/**
	 * Properly concatenates host, port, applicationName and servlet name.
	 * @return Concatenated strings.
	 */
	protected String hostServletUrl() {
		StringBuffer buffer = new StringBuffer(getHost());
		if (!Is.emptyString(getPort())) {
			buffer.append(':')
					.append(getPort());
		}
		if (!Is.emptyString(getApplicationName())) {
			appendWebChar(buffer, '/')
					.append(getApplicationName());
		}
		if (!Is.emptyString(getServletName())) {
			appendWebChar(buffer, '/')
					.append(getServletName());
		}
		return buffer.toString();
	}
	
	/**
	 * @param host the host to set
	 */
	protected void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param port the port to set
	 */
	protected void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param applicationName the applicationName to set
	 */
	protected void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * @param servletName the servletName to set
	 */
	protected void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * @return the servletName
	 */
	public String getServletName() {
		return servletName;
	}
	
	/**
	 * @param uuid the uuid to set
	 */
	protected void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}
		

	/**
	 * @param debugMode the debugMode to set
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * @return the debugMode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Appends a character to the buffer only if the buffer doesn't end with that character.
	 * @param buffer Buffer to be appended.
	 * @param webChar Character to be appended.
	 * @return The StringBuffer ending with the char.
	 */
	private StringBuffer appendWebChar(StringBuffer buffer, char webChar) {
		if (buffer.length() == 0) {
			buffer.append(webChar);
		} else if (buffer.charAt(buffer.length() - 1) != webChar) {
			buffer.append(webChar);
		}
		return buffer;
	}
	
	private void doLog(Object message) {
		if (isDebugMode()) {
			LOG.info(message);
		} else {
			LOG.debug(message);
		}
	}

	/**
	 * Parses the command line and starts an instance of a client.
	 * @param args Command line arguments.
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception {
		LocalPrintManager manager = new LocalPrintManager();
		Options options = new Options();
		options.addOption("?", "help", false, ClientLabels.get("client.command_line_help"));
		options.addOption("k", "kill", false, ClientLabels.get("client.command_line_kill"));
		options.addOption("h", "host", true, ClientLabels.get("client.command_line_host"));
		options.addOption("p", "port", true, ClientLabels.get("client.command_line_port"));
		options.addOption("a", "app", true, ClientLabels.get("client.command_line_app"));
		options.addOption("s", "servlet", true, ClientLabels.get("client.command_line_servlet"));
		options.addOption("u", "uuid", true, ClientLabels.get("client.command_line_uuid"));
		options.addOption("v", "verbose", false, ClientLabels.get("client.command_line_verbose"));
		
		// Set values based on wubiq-client.properties
		manager.setHost(ClientProperties.getHost());
		manager.setPort(ClientProperties.getPort());
		manager.setApplicationName(ClientProperties.getApplicationName());
		manager.setServletName(ClientProperties.getServletName());
		manager.setUuid(ClientProperties.getUuid());
		
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar wubiq-client.jar", options, true);
			} else {
				if (line.hasOption("kill")) {
					manager.killManager = true;
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
				if (line.hasOption("debug")) {
					manager.setDebugMode(true);
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
