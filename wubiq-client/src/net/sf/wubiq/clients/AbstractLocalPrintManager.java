/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.utils.ClientLabels;
import net.sf.wubiq.utils.ClientProperties;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.Is;

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
public abstract class AbstractLocalPrintManager implements Runnable {
	private static final Log LOG = LogFactory.getLog(AbstractLocalPrintManager.class);
	private String applicationName;
	private String servletName;
	private String uuid;
	private boolean killManager;
	private boolean refreshServices;
	private boolean debugMode;
	private long checkPendingJobInterval = 3000;
	private long printingJobInterval = 1000;
	private int connectionErrorRetries = -1;
	private int connectionErrorCount = 0;
	private boolean cancelManager = false;
	private Set<String> connections;
	private Set<URL> urls;
	private URL preferredURL;

	public AbstractLocalPrintManager() {
		connections = new HashSet<String>();
		preferredURL = null;
	}
	
	@Override
	public void run() {
		if (isKillManager()) {
			LOG.info(ClientLabels.get("client.closing_local_manager"));
			killManager();
		} else {
			if (isActive()) {
				killManager();
			}
			if (!isActive()) {
				bringAlive();
				setRefreshServices(true);
				double nextCheckInterval = getCheckPendingJobInterval();
				while (!isKilled()) {
					try {
						if (needsRefresh()) {
							registerPrintServices();
							setRefreshServices(false);
							setConnectionErrorCount(0);
						}
						String[] pendingJobs = getPendingJobs();
						for (String pendingJob : pendingJobs) {
							processPendingJob(pendingJob);
							Thread.sleep(getPrintingJobInterval());
							nextCheckInterval = fastReadingTime();
						}
						Thread.sleep((long)nextCheckInterval);
						nextCheckInterval = nextCheckInterval * accelerationRate();
						if (nextCheckInterval > getCheckPendingJobInterval()) {
							nextCheckInterval = getCheckPendingJobInterval();
						}
					} catch (ConnectException e) {
						if (getConnectionErrorRetries() >= 0) {
							setConnectionErrorCount(getConnectionErrorCount() + 1);
							if (getConnectionErrorCount() > getConnectionErrorRetries()) {
								break;
							}
						}
						LOG.debug(e.getMessage());
						setRefreshServices(true);
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							LOG.debug(e.getMessage());
						}
					} catch (InterruptedException e) {
						LOG.error(e.getMessage(), e);
						break;
					}
				}
				LOG.info(ClientLabels.get("client.closing_local_manager"));
				killManager();
			} else {
				LOG.info(ClientLabels.get("client.another_process_is_running"));
			}
		}
	}
	
	//******************
	// Throttle control.
	private double fastReadingTime() {
		return 10;
	}
	
	private double accelerationRate() {
		return 1.1; // This value must produce at least a value of one.
	}
	//******************

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
	protected abstract void processPendingJob(String jobId) throws ConnectException;
		
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
	protected abstract void registerPrintServices() throws ConnectException;
	
	/**
	 * Asks the server if this instance of the client should be closed. This will allow for remote cancellation of client.
	 * (Not yet implemented).
	 * @return
	 */
	protected boolean isKilled() {
		boolean returnValue = isCancelManager();
		if (!returnValue) {
			try {
				if (askServer(CommandKeys.IS_KILLED).equals("1")) {
					returnValue = true;
				}
			} catch (ConnectException e) {
				doLog(e.getMessage());
			}
		}
		doLog("Is Killed?" + returnValue);
		return returnValue;
	}
	
	/**
	 * Asks the server if this instance of the client should be closed. This will allow for remote cancellation of client.
	 * (Not yet implemented).
	 * @return
	 */
	protected boolean isActive() {
		boolean returnValue = false;
		try {
			if (askServer(CommandKeys.IS_ACTIVE).equals("1")) {
				returnValue = true;
			}
		} catch (ConnectException e) {
			doLog(e.getMessage());
		}
		doLog("Is Active?" + returnValue);
		return returnValue;
	}

	/**
	 * Asks the server if this instance of the client needs to be refreshed with the list of print services.
	 * @return
	 */
	protected boolean needsRefresh() {
		boolean returnValue = true;
		try {
			if (askServer(CommandKeys.IS_REFRESHED).equals("1") && !isRefreshServices()) {
				returnValue = false;
			}
		} catch (ConnectException e) {
			doLog(e.getMessage());
		}
		doLog("Needs Refresh?" + returnValue);
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
		String url = null;
		URL webUrl = null;
		HttpURLConnection connection = null;
		Object content = null;
		BufferedReader reader = null;
		List<URL> actualURLs = new ArrayList<URL>();
		if (preferredURL != null) {
			actualURLs.add(preferredURL);
		} else {
			actualURLs.addAll(getUrls());
		}
		try {
			for (URL address : actualURLs) {
				try {
					url = getEncodedUrl(address, command, parameters);
					doLog("URL:" + url);
					webUrl = new URL(url);
					connection = (HttpURLConnection) webUrl.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					connection.setRequestMethod("POST");
					content = connection.getContent();
				} catch (IOException e) {
					LOG.debug(e.getMessage() + ":" + address);
					connection = null;
				}
			}
			if (connection == null) {
				url = null;
				throw new IOException("Couldn't connect to any of the addresses:" + getUrls());
			}
	
			if (connection.getContentType() != null) {
				if (connection.getContentType().equals("application/pdf")) {
					returnValue = (InputStream)content;
				} else if (connection.getContentType().equals("text/html")) {
					InputStream inputStream = (InputStream) content;
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					IOUtils.INSTANCE.copy(inputStream, output);
					returnValue = new String(output.toByteArray());
					output.close();
					inputStream.close();
				}
			}
		} catch (MalformedURLException e) {
			preferredURL = null;
			LOG.error(e.getMessage() + "->" + url);
		} catch (UnknownServiceException e) {
			preferredURL = null;
			doLog(e.getMessage());
		} catch (FileNotFoundException e) {
			doLog(e.getMessage());
		} catch (IOException e) {
			preferredURL = null;
			LOG.error(e.getMessage());
			throw new ConnectException(e.getMessage());
		} catch (Exception e) {
			preferredURL = null;
			LOG.error(e.getMessage() + "->" + url);
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
	
	protected Set<URL> getUrls() {
		if (urls == null) {
			urls = new LinkedHashSet<URL>();
			for (String connection : getConnections()) {
				if (!Is.emptyString(connection.trim())) {
					URL url = hostServletUrl(connection);
					if (url != null) {
						urls.add(url);
					}
				}
			}
		}
		return urls;
	}
	/**
	 * Properly forms the url and encode the parameters so that servers can receive them correctly.
	 * @param command Command to be encoded as part of the url.
	 * @param parameters Arrays of parameters in the form parameterName=parameterValue that will be appended to the url.
	 * @return Url string with parameterValues encoded.
	 */
	protected String getEncodedUrl(URL address, String command, String... parameters) {
		StringBuffer parametersQuery = new StringBuffer("")
				.append(ParameterKeys.UUID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(getUuid());
			if (!Is.emptyString(command)) {
				parametersQuery.append('&')
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
				parametersQuery.append('&')
						.append(parameterString);
			}
		String returnValue = "";
		try {
			URI uri = new URI(address.getProtocol(), address.getHost() + ":" + address.getPort(), address.getPath(), parametersQuery.toString(), null);
			returnValue = uri.toASCIIString();
		} catch (URISyntaxException e) {
			LOG.error(e.getMessage(), e);
		}
		if (command.equals(CommandKeys.PRINT_TEST_PAGE) ||
				command.equals(CommandKeys.SHOW_PRINT_SERVICES)) {
			returnValue = returnValue.replace("wubiq.do", "wubiq-print-test.do");
		}

		return returnValue;
	}
	
	/**
	 * Creates a valid url with the given connection.
	 * @param connection Connection to encapsulate.
	 * @return Valid URL or null.
	 */
	private URL hostServletUrl(String connection) {
		URL returnValue = null;
		StringBuffer buffer = new StringBuffer("");
		if (!Is.emptyString(connection)) {
			buffer.append(connection);
		}
		if (!Is.emptyString(getApplicationName())) {
			appendWebChar(buffer, '/')
					.append(getApplicationName());
		}
		if (!Is.emptyString(getServletName())) {
			appendWebChar(buffer, '/')
					.append(getServletName());
		}
		if (buffer.length() > 0) {
			try {
				returnValue = new URL(buffer.toString());
			} catch (MalformedURLException e) {
				LOG.error(e.getMessage());
			}
		}
		return returnValue;
	}
	
	/**
	 * Creates a connection based on host, port. Will be annotated as deprecated soon.
	 * @param host Host.
	 * @param port Port.
	 * @return String containing the equivalent connection. Might be blank, never null.
	 */
	protected String hostPortConnection(String host, String port) {
		StringBuffer connection = new StringBuffer("");
		if (host != null) {
			if (!Is.emptyString(host.trim())) {
				connection.append(host.trim());
				if (port != null && !Is.emptyString(port.trim())) {
					connection.append(':')
						.append(port.trim());
				}
			}
		}
		return connection.toString();
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
		return applicationName.trim();
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
		return servletName.trim();
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
		return uuid.trim();
	}
		

	/**
	 * @return the connections
	 */
	public Set<String> getConnections() {
		return connections;
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
	 * @return the checkPendingJobInterval
	 */
	public long getCheckPendingJobInterval() {
		return checkPendingJobInterval;
	}

	/**
	 * @param checkPendingJobInterval the checkPendingJobInterval to set
	 */
	public void setCheckPendingJobInterval(long checkPendingJobInterval) {
		this.checkPendingJobInterval = checkPendingJobInterval;
	}

	/**
	 * @param checkPendingJobIntervalStr the checkPendingJobInterval to set
	 */
	public void setCheckPendingJobInterval(String checkPendingJobIntervalStr) {
		try {
			this.checkPendingJobInterval = Long.parseLong(checkPendingJobIntervalStr);
		}
		catch (Exception e) {
			doLog(e.getMessage());
		}
	}

	/**
	 * @return the printingJobInterval
	 */
	public long getPrintingJobInterval() {
		return printingJobInterval;
	}

	/**
	 * @param printingJobInterval the printingJobInterval to set
	 */
	public void setPrintingJobInterval(long printingJobInterval) {
		this.printingJobInterval = printingJobInterval;
	}

	/**
	 * @param printingJobIntervalStr the printingJobInterval to set
	 */
	public void setPrintingJobInterval(String printingJobIntervalStr) {
		try {
			this.printingJobInterval = Long.parseLong(printingJobIntervalStr);
		} catch (Exception e) {
			doLog(e.getMessage());
		}
	}

	/**
	 * @return the connectionErrorRetries
	 */
	public int getConnectionErrorRetries() {
		return connectionErrorRetries;
	}

	/**
	 * @param connectionErrorRetries the connectionErrorRetries to set
	 */
	public void setConnectionErrorRetries(int connectionErrorRetries) {
		this.connectionErrorRetries = connectionErrorRetries;
	}

	/**
	 * @return the cancelManager
	 */
	public boolean isCancelManager() {
		return cancelManager;
	}

	/**
	 * @param cancelManager the cancelManager to set
	 */
	public void setCancelManager(boolean cancelManager) {
		this.cancelManager = cancelManager;
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
	
	protected void doLog(Object message) {
		if (isDebugMode()) {
			LOG.info(message);
		} else {
			LOG.debug(message);
		}
	}
	
	/**
	 * Initializes the remote connection manager. Tries to set the list of possible connections.
	 * @param manager Manager to configure.
	 */
	protected static void initializeDefault(AbstractLocalPrintManager manager) {
		// Set values based on wubiq-client.properties
		manager.setApplicationName(ClientProperties.getApplicationName());
		manager.setServletName(ClientProperties.getServletName());
		manager.setUuid(ClientProperties.getUuid());
		String connectionsString = ClientProperties.getConnections();
		addConnectionsString(manager, connectionsString);
	}
	
	/**
	 * Adds a list of comma separated connections to the connections available.
	 * @param manager Manager containing the connections.
	 * @param connectionsString Comma separated list of connections.
	 */
	protected static void addConnectionsString(AbstractLocalPrintManager manager, String connectionsString) {
		if (!Is.emptyString(connectionsString)) {
			for (String connection : connectionsString.split("[,;\n]")) {
				if (!Is.emptyString(connection.trim())) {
					manager.getConnections().add(connection);
				}
			}
		}
	}

	/**
	 * @return the killManager
	 */
	public boolean isKillManager() {
		return killManager;
	}

	/**
	 * @param killManager the killManager to set
	 */
	public void setKillManager(boolean killManager) {
		this.killManager = killManager;
	}

	/**
	 * @return the refreshServices
	 */
	public boolean isRefreshServices() {
		return refreshServices;
	}

	/**
	 * @param refreshServices the refreshServices to set
	 */
	public void setRefreshServices(boolean refreshServices) {
		this.refreshServices = refreshServices;
	}

	/**
	 * @return the connectionErrorCount
	 */
	public int getConnectionErrorCount() {
		return connectionErrorCount;
	}

	/**
	 * @param connectionErrorCount the connectionErrorCount to set
	 */
	public void setConnectionErrorCount(int connectionErrorCount) {
		this.connectionErrorCount = connectionErrorCount;
	}
}
