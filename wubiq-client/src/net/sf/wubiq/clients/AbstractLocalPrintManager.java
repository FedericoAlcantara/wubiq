/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.utils.ClientLabels;
import net.sf.wubiq.utils.ClientProperties;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import net.sf.wubiq.utils.WebUtils;

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
	private String groups;
	private boolean killManager;
	private boolean refreshServices;
	private boolean debugMode;
	private long checkPendingJobInterval = 6000; // every 6 seconds
	private long printingJobInterval = 2000; // 2 second
	private int connectionErrorRetries = -1; // never end
	private int connectionErrorCount = 0;
	private boolean cancelManager = false;
	private Set<String> connections;
	private Set<URL> urls;
	protected URL preferredURL;
	private boolean connected = false;
	private String sessionId;
	private String lastSessionId;
			
	/**
	 * Level of detail for debug output. between 0 and 5.
	 * 0 - Messages with this level always are shown.
	 * 3 - Use for messages regarding connection.
	 * 5 - Very fine grained messages such as handshake of printing.
	 */
	private int debugLevel;
	
	public AbstractLocalPrintManager() {
		connections = new HashSet<String>();
		preferredURL = null;
		setDebugLevel(0);
		String retries = System.getProperty(PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES);
		if (!Is.emptyString(retries)) {
			try {
				connectionErrorRetries = Integer.parseInt(retries);
			} catch (NumberFormatException e) {
				connectionErrorRetries = -1;
			}
		}
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
				Set<String> holdPrintServices = new HashSet<String>();
				while (!isKilled()) {
					try {
						if (needsRefresh()) {
							registerPrintServices();
							setRefreshServices(false);
							setConnectionErrorCount(0);
						}
						String[] pendingJobs = getPendingJobs();
						holdPrintServices.clear();
						for (String pendingJob : pendingJobs) {
							try {
								String printServiceName = jobPrintServiceName(pendingJob);
								if (holdPrintServices.contains(printServiceName)) {
									continue;
								}
								holdPrintServices.add(printServiceName);
								processPendingJob(pendingJob, printServiceName);
							} catch (Throwable e) {
								doLog(e.getMessage(), 0);
							}
							Thread.sleep(getPrintingJobInterval());
							//nextCheckInterval = fastReadingTime();
						}
						Thread.sleep((long)nextCheckInterval);
						//nextCheckInterval = nextCheckInterval * accelerationRate();
						//if (nextCheckInterval > getCheckPendingJobInterval()) {
							//nextCheckInterval = getCheckPendingJobInterval();
						//}
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
							Thread.sleep(getCheckPendingJobInterval()); 
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
		doLog("Kill Manager", 5);
		try {
			askServer(CommandKeys.KILL_MANAGER);
		} catch (ConnectException e) {
			doLog(e.getMessage(), 3);
		}
	}

	/**
	 * Allow manager to re-register.
	 */
	protected void bringAlive() {
		doLog("Bring alive", 0);
		try {
			askServer(CommandKeys.BRING_ALIVE);
		} catch (ConnectException e) {
			doLog(e.getMessage(), 3);
		}
	}
	
	/**
	 * Process a single pending job.
	 * @param jobId Id of the job to be printed.
	 * @param printService name. Associated print service name.
	 */
	protected abstract void processPendingJob(String jobId, String printServiceName) throws ConnectException;
	
	/**
	 * Finds the job id print service name.
	 * @param jobId Job id to find its print service.
	 * @return Job id.
	 * @throws ConnectException
	 */
	protected String jobPrintServiceName(String jobId) throws ConnectException {
		StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(jobId);
		String printServiceName = askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter.toString());
		return printServiceName;
	}
	
	/**
	 * Ask to the server for a list of all pending jobs for this client instance.
	 * @return A list of pending jobs, or a zero element String array. Never null.
	 */
	protected String[] getPendingJobs() throws ConnectException {
		doLog("Get Pending Jobs", 5);
		
		String[]returnValue = new String[]{};
		String pendingJobResponse = askServer(CommandKeys.PENDING_JOBS);
		String pendingJobList = "";
		if (!Is.emptyString(pendingJobResponse)
				&& pendingJobResponse.startsWith(ParameterKeys.PENDING_JOB_SIGNATURE)) {
			pendingJobList = pendingJobResponse.substring(ParameterKeys.PENDING_JOB_SIGNATURE.length());
			returnValue = pendingJobResponse.substring(ParameterKeys.PENDING_JOB_SIGNATURE.length())
				.split(ParameterKeys.CATEGORIES_SEPARATOR);
		}
		if (!Is.emptyString(pendingJobList)) {
			doLog("pending jobs: " + pendingJobList, 1);
		} else {
			doLog("pending jobs: " + pendingJobList, 5);
		}
		return returnValue;
	}
	
	/**
	 * Registers the probable computer name, and initializes remote print services for this client instance.
	 */
	protected void registerComputerName() throws ConnectException {
		doLog("Register Computer Name", 5);
		// Gather computer Name.
		StringBuffer computerName = new StringBuffer(ParameterKeys.COMPUTER_NAME)
			.append(ParameterKeys.PARAMETER_SEPARATOR); 
		try {
			computerName.append(InetAddress.getLocalHost().getHostName());
			doLog("Register computer name:" + computerName, 0);
			doLog("Force Serialized Communication:" + System.getProperty(PropertyKeys.WUBIQ_CLIENT_FORCE_SERIALIZED_CONNECTION), 0);
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
			computerName.append("UNKNOWN");
		}
		askServer(CommandKeys.REGISTER_COMPUTER_NAME, computerName.toString(),
				ParameterKeys.CLIENT_VERSION 
				+ ParameterKeys.PARAMETER_SEPARATOR
				+ Labels.VERSION);
	}


	/**
	 * Registers all valid local print services to the remote server.
	 */
	protected abstract void registerPrintServices() throws ConnectException;
	
	/**
	 * Asks the server if this instance of the client should be closed. This will allow for remote cancellation of client.
	 * @return True if the server responds that this client is already killed.
	 */
	protected boolean isKilled() {
		boolean returnValue = isCancelManager();
		if (!returnValue) {
			try {
				if (askServer(CommandKeys.IS_KILLED).equals("1")) {
					returnValue = true;
				}
			} catch (ConnectException e) {
				doLog(e.getMessage(), 3);
			}
		}
		doLog("Is Killed?" + returnValue, 5);
		return returnValue;
	}
	
	/**
	 * Asks the server if this instance of the client should be closed. This will allow for remote cancellation of client.
	 * @return True if the server responds that this client is active.
	 */
	protected boolean isActive() {
		boolean returnValue = false;
		try {
			if (askServer(CommandKeys.IS_ACTIVE).equals("1")) {
				returnValue = true;
			}
		} catch (ConnectException e) {
			doLog(e.getMessage(), 3);
		}
		doLog("Is Active?" + returnValue, 5);
		return returnValue;
	}

	/**
	 * Asks the server if this instance of the client needs to be refreshed with the list of print services.
	 * @return True if the printer information needs to be synchronized with the server.
	 */
	protected boolean needsRefresh() {
		boolean returnValue = true;
		try {
			if (askServer(CommandKeys.IS_REFRESHED).equals("1") && !isRefreshServices()) {
				returnValue = false;
			}
		} catch (ConnectException e) {
			doLog(e.getMessage(), 3);
		}
		doLog("Needs Refresh?" + returnValue, 5);
		return returnValue;
	}

	/**
	 * Communicates with the server using the sub set of direct connection commands.
	 * @param jobIdString id of the current job.
	 * @param command Direct connect sub command.
	 * @param parameters Parameters.
	 * @return Response from the server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	public Object directServer(String jobIdString, DirectConnectCommand command, String... parameters) 
			throws ConnectException {
		StringBuffer returnValue = new StringBuffer(DirectConnectKeys.DIRECT_CONNECT_PARAMETER)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(command.ordinal());
		String[] newParameters = new String[parameters.length + 2];
		int index = 0;
		newParameters[index++] = returnValue.toString();
		newParameters[index++] = ParameterKeys.PRINT_JOB_ID
				+ ParameterKeys.PARAMETER_SEPARATOR
				+ jobIdString;
		for (String parameter: parameters) {
			newParameters[index++] = parameter;
		}
		return pollServer(CommandKeys.DIRECT_CONNECT, newParameters);
	}
		
	/**
	 * Communicates with the server using the sub set of direct connection commands. This connection
	 * serves the objects serialized ONLY, no encoding is performed.
	 * @param jobIdString id of the current job.
	 * @param command Direct connect sub command.
	 * @return Response from the server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	public Object directServerNotSerialized(String jobIdString, DirectConnectCommand command) 
			throws ConnectException {
		return directServerNotSerialized(jobIdString, command, new HashMap<String, Object>());
	}

	/**
	 * Communicates with the server using the sub set of direct connection commands. This connection
	 * serves the objects serialized ONLY, no encoding is performed.
	 * @param jobIdString id of the current job.
	 * @param command Direct connect sub command.
	 * @param parameters Parameters. All parameters MUST be serializable.
	 * @return Response from the server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	public Object directServerNotSerialized(String jobIdString, DirectConnectCommand command, Map<String, Object> parameters) 
			throws ConnectException {
		parameters.put(ParameterKeys.CLIENT_SUPPORTS_COMPRESSION, "true");
		parameters.put(ParameterKeys.COMMAND, CommandKeys.DIRECT_CONNECT);
		parameters.put(ParameterKeys.UUID, getUuid());
		if (!Is.emptyString(getGroups())) {
			parameters.put(ParameterKeys.GROUPS, getGroups());
		}		
		parameters.put(DirectConnectKeys.DIRECT_CONNECT_PARAMETER, command.ordinal());
		parameters.put(ParameterKeys.PRINT_JOB_ID, jobIdString);
		parameters.put(DirectConnectKeys.DIRECT_CONNECT_COMPRESSED_MODE, "true");
		Object[] serializedData = DirectConnectUtils.INSTANCE.serializeObject(parameters);
		return pollServer(CommandKeys.DIRECT_CONNECT, (InputStream)serializedData[0], (Integer)serializedData[1]);
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
		try {
			String encodedParameters = null;
			encodedParameters = getEncodedParameters(command, parameters);
			ByteArrayInputStream input = new ByteArrayInputStream(encodedParameters.getBytes("utf-8"));
			return pollServer(command, input, encodedParameters.getBytes().length);
		} catch (ConnectException e) {
			throw e;
		} catch (IOException e) {
			LOG.debug(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Send command to server and returns it response as an object.
	 * @param command Command to send to the server.
	 * @param parameters List of parameters to be sent.
	 * @return Response from server.
	 * @throws ConnectException If it can't connect to the server.
	 */
	protected Object pollServer(String command, InputStream input, int length) throws ConnectException {
		Object returnValue = "";
		URL webUrl = null;
		HttpURLConnection connection = null;
		Object content = null;
		BufferedReader reader = null;
		List<URL> actualURLs = new ArrayList<URL>();
		resetLastSessionId();
		if (preferredURL != null) {
			actualURLs.add(preferredURL);
		} else {
			actualURLs.addAll(getUrls());
		}
		try {
			for (URL address : actualURLs) {
				try {
					webUrl = address;
					connection = (HttpURLConnection) webUrl.openConnection();
					connection.setDoOutput(true);
					connection.setDoInput(true);
					connection.setRequestMethod("POST");
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
					connection.setRequestProperty("charset", "utf-8");
					connection.setRequestProperty("Accept-Charset", "utf-8");
					// Using content-type will force the use of input stream and that is not managed by older servers.
					//connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
					connection.setRequestProperty("Content-Length", "" + length);
					if (!Is.emptyString(getSessionId())) {
						connection.setRequestProperty("Cookie", "JSESSIONID=" + getSessionId());
					}
					connection.setUseCaches (false);
					IOUtils.INSTANCE.copy(input, connection.getOutputStream());
					content = connection.getContent();
					for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
						if (entry.getKey() != null && entry.getValue() != null) {
							if (entry.getKey().equalsIgnoreCase("Set-Cookie")) {
								for (String line : entry.getValue()) {
									if (line.toLowerCase().contains("jsessionid")) {
										String[] values = line.split(";");
										String newSession = null;
										String path = null;
										for (String value : values) {
											if (value.toLowerCase().contains("jsessionid")) {
												newSession = value.trim().split("=")[1];
											} else if (value.toLowerCase().contains("path")) {
												path = value.trim().split("=")[1];
											}
										}
										if (path != null && newSession != null
												&& path.equals("/" + getApplicationName())) {
											setLastSessionId(newSession);
										}
									}
								}
										
							}
						}
					}
					if (!connected) {
						doLog("\nConnected! to:" + webUrl + "\n", 0);
						connected = true;
					} else {
						doLog("Connected! to:" + webUrl, 5);
					}
					preferredURL = webUrl;
					break;
				} catch (UnknownServiceException e) {
					throw e;
				} catch (IOException e) {
					LOG.debug(e.getMessage() + ":" + address);
					connection = null;
				}
			}
			if (connection == null) {
				connected = false;
				throw new IOException("Couldn't connect to any of the addresses:" + getUrls());
			}
	
			if (connection.getContentType() != null) {
				if (connection.getContentType().equals("application/pdf") ||
						"application/octet-stream".equals(connection.getContentType())) {
					InputStream inputStream = (InputStream) content;
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					IOUtils.INSTANCE.copy(inputStream, output);
					returnValue = new ByteArrayInputStream(output.toByteArray());
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
			LOG.error(e.getMessage() + "->" + webUrl);
		} catch (UnknownServiceException e) {
			preferredURL = null;
			doLog(e.getMessage(), 5);
		} catch (FileNotFoundException e) {
			doLog(e.getMessage(), 5);
		} catch (IOException e) {
			preferredURL = null;
			LOG.error(e.getMessage());
			throw new ConnectException(e.getMessage());
		} catch (Exception e) {
			preferredURL = null;
			LOG.error(e.getMessage() + "->" + webUrl);
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
	 * The last successfully connected URL.
	 * @return Last successfully connected URL or null.
	 */
	protected URL getPreferredURL() {
		return preferredURL;
	}
	
	/**
	 * Properly forms the url and encode the parameters so that servers can receive them correctly.
	 * @param command Command to be encoded as part of the url.
	 * @param parameters Arrays of parameters in the form parameterName=parameterValue that will be appended to the url.
	 * @return Url string with parameterValues encoded.
	 */
	protected String getEncodedParameters(String command, String... parameters) {
		StringBuffer parametersQuery = new StringBuffer("")
				.append(ParameterKeys.UUID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(getUuid());
			if (!Is.emptyString(getGroups())) {
				parametersQuery.append('&')
						.append(ParameterKeys.GROUPS)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(getGroups());
			}
				
			if (!Is.emptyString(command)) {
				parametersQuery.append('&')
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(command);
			}
		
			for (String parameter: parameters) {
				if (Is.emptyString(parameter)) {
					continue;
				}
				String parameterString = parameter;
				if (parameter.contains("=")) {
					String parameterName = parameter.substring(0, parameter.indexOf("="));
					String parameterValue = parameter.substring(parameter.indexOf("=") + 1);
					parameterValue = WebUtils.INSTANCE.encode(parameterValue);
					parameterString = parameterName + "=" + parameterValue;
				}
				parametersQuery.append('&')
						.append(parameterString);
			}
		String returnValue = parametersQuery.toString();

		return returnValue;
	}
	
	/**
	 * Creates a valid url with the given connection.
	 * @param connection Connection to encapsulate.
	 * @return Valid URL or null.
	 */
	protected URL hostServletUrl(String connection) {
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
		
	public String getGroups() {
		return groups;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}

	/**
	 * @return the connections
	 */
	public Set<String> getConnections() {
		return connections;
	}
	
	/**
	 * Sets the connections.
	 * @param connections
	 */
	protected void setConnections(Set<String> connections) {
		this.connections = connections;
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

	public int getDebugLevel() {
		return debugLevel;
	}

	public void setDebugLevel(int debugLevel) {
		if (debugLevel < 0) {
			debugLevel = 0;
		} else if (debugLevel > 5) {
			debugLevel = 5;
		}
		this.debugLevel = debugLevel;
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
			doLog(e.getMessage(), 5);
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
			doLog(e.getMessage(), 5);
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
	
	/**
	 * Takes care of the logging. It defaults to maximal
	 * @param message Message to be logged.
	 */
	protected void doLog(Object message) {
		doLog(message, 5);
	}
	
	/**
	 * Takes care of the logging.
	 * @param message Message to be logged.
	 * @param logLevel Level associated with the message.
	 */
	protected void doLog(Object message, int logLevel) {
		if (isDebugMode() && logLevel <= getDebugLevel()) {
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
		manager.setApplicationName(ClientProperties.INSTANCE.getApplicationName());
		manager.setServletName(ClientProperties.INSTANCE.getServletName());
		manager.setUuid(ClientProperties.INSTANCE.getUuid());
		String connectionsString = ClientProperties.INSTANCE.getConnections();
		addConnectionsString(manager, connectionsString);
		manager.setDebugMode(ClientProperties.INSTANCE.isDebugMode());
		manager.setCheckPendingJobInterval(ClientProperties.INSTANCE.getPollInterval());
		manager.setPrintingJobInterval(ClientProperties.INSTANCE.getPrintJobWait());
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
	
	/**
	 * Session id to be used in the next call.
	 * @return Session id to use.
	 */
	protected String getSessionId() {
		return sessionId;
	}
	
	/**
	 * Sets the new session id for the next connection attempt.
	 * @param sessionId new session id.
	 */
	protected void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	/**
	 * Reads the last session id.
	 * @return Last session id. Might be null.
	 */
	protected String lastSessionId() {
		return lastSessionId;
	}
	
	/**
	 * Stores the last session id.
	 * @param lastSessionId Last session id to store.
	 */
	protected void setLastSessionId(String lastSessionId) {
		this.lastSessionId = lastSessionId;
		setSessionId(lastSessionId);
	}
	
	/**
	 * Resets the last session id to null.
	 * @return Previous value in last session id.
	 */
	protected String resetLastSessionId() {
		String returnValue = this.lastSessionId;
		this.lastSessionId = null;
		return returnValue;
	}
}
