/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

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

import com.gargoylesoftware.htmlunit.BinaryPage;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Keeps gathering printer information and reports it to the server.
 * @author Federico Alcantara
 *
 */
public class LocalPrintManager implements Runnable {
	private static final Log LOG = LogFactory.getLog(LocalPrintManager.class);
	private WebClient client;
	private Object page;
	private String host;
	private String port;
	private String applicationName;
	private String servletName;
	private String uuid;
	private boolean killManager;
	private boolean refreshServices;
	
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
	 * Kills the manager
	 */
	protected void killManager() {
		try {
			askServer(CommandKeys.KILL_MANAGER);
		} catch (ConnectException e) {
			LOG.debug(e.getMessage());
		}
	}
	
	/**
	 * Process pending job.
	 * @param jobId Id of the job to be printed.
	 */
	protected void processPendingJob(String jobId) throws ConnectException {
		StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(jobId);
		InputStream stream = null;
		try {
			String printServiceName = askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter.toString());
			String attributesData = askServer(CommandKeys.READ_PRINT_ATTRIBUTES, parameter.toString());
			stream = (InputStream)pollServer(CommandKeys.READ_PRINT_JOB, parameter.toString());
			ClientPrintDirectUtils.printPdf(jobId, printServiceName, PrintServiceUtils.convertToAttributes(attributesData), stream);
			askServer(CommandKeys.CLOSE_PRINT_JOB, parameter.toString());
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
				LOG.debug(e.getMessage());
			}
		}
	}

	/**
	 * Gets a list of pending jobs.
	 * @return A list of pending jobs
	 */
	protected String[] getPendingJobs() throws ConnectException {
		String[]returnValue = new String[]{};
		String pendingJobResponse = askServer(CommandKeys.PENDING_JOBS);
		if (!Is.emptyString(pendingJobResponse)
				&& pendingJobResponse.startsWith(ParameterKeys.PENDING_JOB_SIGNATURE)) {
			returnValue = pendingJobResponse.substring(ParameterKeys.PENDING_JOB_SIGNATURE.length())
				.split(ParameterKeys.CATEGORIES_SEPARATOR);
		}
		return returnValue;
	}
	
	/**
	 * Registers and initializes remote print services.
	 */
	protected void registerComputerName() throws ConnectException {
		// Gather computer Name.
		StringBuffer computerName = new StringBuffer(ParameterKeys.COMPUTER_NAME)
		.append(ParameterKeys.PARAMETER_SEPARATOR); 
		try {
			computerName.append(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		}
		askServer(CommandKeys.REGISTER_COMPUTER_NAME, computerName.toString());
	}

	/**
	 * Register Local print services in remote.
	 */
	protected void registerPrintServices() throws ConnectException {
		registerComputerName();
		// Gather printServices.
		for (PrintService printService: PrintServiceUtils.getPrintServices()) {
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
					LOG.error(e.getMessage());
				}

				categories.append(attributes);
			}
			categories.insert(0, ParameterKeys.PARAMETER_SEPARATOR)
				.insert(0, ParameterKeys.PRINT_SERVICE_CATEGORIES);
			askServer(CommandKeys.REGISTER_PRINT_SERVICE, printServiceRegister.toString(), categories.toString());
		}
	}

	/**
	 * If true the manager must exit.
	 * @return
	 */
	protected boolean isKilled() {
		boolean returnValue = false;
		try {
			if (askServer(CommandKeys.IS_KILLED).equals("1")) {
				returnValue = true;
			}
		} catch (ConnectException e) {
			LOG.debug(e.getMessage());
		}
		return returnValue;
	}
	
	/**
	 * Send command to server and returns it response as a string.
	 * @param command Command to send to the server.
	 * @param parameters List of parameters to be sent.
	 * @return Response from server.
	 * @throws Exception
	 */
	protected String askServer(String command, String...parameters) throws ConnectException {
		return (String)pollServer(command, parameters);
	}
	
	/**
	 * Send command to server and returns it response as an object.
	 * @param command Command to send to the server.
	 * @param parameters List of parameters to be sent.
	 * @return Response from server.
	 * @throws Exception
	 */
	protected Object pollServer(String command, String... parameters) throws ConnectException {
		Object returnValue = "";
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
			url.append('&')
					.append(parameter);
		}
		
		try {
			page = getClient().getPage(url.toString());
			if (page instanceof HtmlPage) {
				returnValue = ((HtmlPage)page).asText();
			} else if (page instanceof TextPage) {
				returnValue = ((TextPage)page).getContent();
			} else if (page instanceof BinaryPage) {
				returnValue = ((BinaryPage)page).getInputStream();
			} else if (page instanceof UnexpectedPage) {
				returnValue = ((UnexpectedPage)page).getInputStream();
			}
		} catch (ConnectException e) {
			LOG.debug(e.getMessage());
			refreshServices = true;
			throw e;
		} catch (FailingHttpStatusCodeException e) {
			LOG.error(e.getMessage());
		} catch (MalformedURLException e) {
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}
		return returnValue;
	}

	/**
	 * Properly concatenates host, port, applicationName and servlet name.
	 * @return Concatenated strings.
	 */
	public String hostServletUrl() {
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
	 * Sets the new client.
	 * @param client the new client.
	 */
	protected void setClient(WebClient client) {
		this.client = client;
	}
	
	/**
	 * @return the client
	 */
	protected WebClient getClient() {
		if (client == null) {
			client = new WebClient();
		}
		return client;
	}

	/**
	 * @return the page
	 */
	protected Object getPage() {
		return page;
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
	
	public static void main (String[] args) throws Exception {
		LocalPrintManager manager = new LocalPrintManager();
		Options options = new Options();
		options.addOption("?", "help", false, ClientLabels.get("client.command_line_help"));
		options.addOption("k", "kill", false, ClientLabels.get("client.command_line_kill"));
		options.addOption("host", true, ClientLabels.get("client.command_line_host"));
		options.addOption("p", "port", true, ClientLabels.get("client.command_line_port"));
		options.addOption("a", "app", true, ClientLabels.get("client.command_line_app"));
		options.addOption("s", "servlet", true, ClientLabels.get("client.command_line_servlet"));
		options.addOption("u", "uuid", true, ClientLabels.get("client.command_uuid"));
		
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
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar wubiq-client.jar", options, true);
		}
		Thread r = new Thread(manager);
		r.start();
	}

}
