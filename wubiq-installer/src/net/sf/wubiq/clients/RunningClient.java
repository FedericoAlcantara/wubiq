/**
 * 
 */
package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.InstallerProperties;
import net.sf.wubiq.utils.InstallerUtils;
import net.sf.wubiq.utils.Is;

/**
 * @author Federico Alcantara
 *
 */
public class RunningClient extends AbstractLocalPrintManager implements Runnable {
	private boolean stopClient;
	private JavaRun javaRun;
	private Process process;
	private WubiqLauncher launcher;
	private String serviceName;
	
	public RunningClient(WubiqLauncher launcher) {
		this.launcher = launcher;
		this.serviceName = launcher.getServiceName();
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				if (stopClient) {
					break;
				}
				try {
					createJavaRun();
					killClient();
					runClient();
				} catch (ConnectException e) {
					try {
						Thread.sleep(1000); // Every second it tries to reconnect.
					} catch (InterruptedException e1) {
						doLog(e1.getMessage(), 0);
					}
				}
			}
			killClient();
		} catch (Exception e) {
			doLog(e.getMessage(), 0);
		} finally {
			launcher.notifyThreadStopped();
		}
	}
	
	public void stop(){
		try {
			stopClient = true;
			killClient();
			if (process != null) {
				process.destroy();
			}
		} catch (Exception e) {
			doLog(e.getMessage(), 0);
		}
	}
	
	/**
	 * Creates a representation of a java runnable.
	 * @throws ConnectException
	 */
	private void createJavaRun() throws ConnectException {
		InstallerProperties.INSTANCE(serviceName).resetProperties();
		File wubiqClientJar = InstallerUtils.INSTANCE.wubiqClientFile();

		javaRun = new JavaRun();
		javaRun.setFixedParameters(InstallerProperties.INSTANCE(serviceName).getClientParameters());
		List<String> jvmParameters = new ArrayList<String>();
		for (String jvmParam : InstallerProperties.INSTANCE(serviceName).getJvmParameters().split("[; ]")) {
			if (!Is.emptyString(jvmParam.trim())) {
				jvmParameters.add(jvmParam.trim());
			}
		}
		if (!jvmParameters.contains("-D" + PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES + "=")) {
			jvmParameters
					.add("-D" + PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES + "=30");
		}
		
		String dmPrinters = InstallerProperties.INSTANCE(serviceName).getDmPrinters().trim();
		String dmHqPrinters = InstallerProperties.INSTANCE(serviceName).getDmHqPrinters().trim();
		String photoPrinters = InstallerProperties.INSTANCE(serviceName).getPhotoPrinters().trim();
		String defaultDmFont = InstallerProperties.INSTANCE("serviceName").getDefaultDmFont().trim();
		String forceLogicalFonts = InstallerProperties.INSTANCE("serviceName").getForceLogicalFontOnDm().trim();
		
		if (!Is.emptyString(dmPrinters)) {
			jvmParameters
				.add("-D" + PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX + "=" + dmPrinters);
		}
		
		if (!Is.emptyString(dmHqPrinters)) {
			jvmParameters
				.add("-D" + PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ+ "=" + dmHqPrinters);
		}
		
		if (!Is.emptyString(photoPrinters)) {
			jvmParameters
				.add("-D" + PropertyKeys.WUBIQ_PRINTERS_PHOTO + "=" + photoPrinters);
		}
		
		if (!Is.emptyString(defaultDmFont)) {
			jvmParameters
				.add("-D" + PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT + "=" + defaultDmFont);
		}
		
		if (!Is.emptyString(forceLogicalFonts)) {
			jvmParameters
				.add("-D" + PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL + "=" + forceLogicalFonts );
		}
		
		javaRun.setJvmParameters(jvmParameters);
		javaRun.setJarFile(wubiqClientJar.getPath());
		boolean loadNewJar = true;
		
		String serverVersion = serverVersion();
		if (wubiqClientJar.exists()) {
			System.out.println("Wubiq Client Exists!");
			String clientVersion = InstallerUtils.INSTANCE.wubiqClientVersion();
			System.out.println("Client Version:" + clientVersion + ". Server Version:" + serverVersion);
			if (clientVersion.equals(serverVersion)) {
				loadNewJar = false;
			}
		}
		if (loadNewJar) {
			try {
				System.out.println("Loading a new Jar");
				URL downloadURL = new URL(getPreferredURL(), "wubiq-client.jar");
				HttpURLConnection connection = (HttpURLConnection) downloadURL.openConnection();
				connection.connect();
				InputStream inputStream = (InputStream)connection.getContent();
				IOUtils.INSTANCE.copy(inputStream, new FileOutputStream(wubiqClientJar));
				inputStream.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void killClient() {
		if (javaRun != null &&
				getPreferredURL() != null) {
			// Stop current instance;
			URL host;
			try {
				host = new URL(getPreferredURL(), "/");
				javaRun.setParameters("-k", "-c", host.toString());
				wubiqClientRun(false, javaRun.command());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					doLog(e.getMessage(), 0);
				}
			} catch (MalformedURLException e) {
				doLog(e.getMessage(), 0);
				throw new RuntimeException(e);
			} catch (ConnectException e) {
				doLog(e.getMessage(), 0);
			}
		}
	}
	
	private void runClient() throws ConnectException {
		if (javaRun != null &&
				getPreferredURL() != null) {
			// run current client;
			URL host;
			try {
				host = new URL(getPreferredURL(), "/");
				javaRun.setParameters("-c", host.toString());
				wubiqClientRun(true, javaRun.command());
			} catch (MalformedURLException e) {
				doLog(e.getMessage(), 0);
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@Override
	protected void processPendingJob(String jobId, String printServiceName) throws ConnectException {
		// Here do nothing
	}

	@Override
	protected void registerPrintServices() throws ConnectException {
		// Here do nothing
	}
	
	@Override
	public String getApplicationName() {
		return InstallerProperties.INSTANCE(serviceName).getApplicationName();
	}
	
	@Override
	public String getServletName() {
		return InstallerProperties.INSTANCE(serviceName).getServletName();
	}
	
	@Override
	public String getUuid() {
		return InstallerProperties.INSTANCE(serviceName).getUuid();
	}
	
	@Override
	public Set<String> getConnections() {
		Set<String> returnValue = new HashSet<String>();
		for (String connection : InstallerProperties.INSTANCE(serviceName).getConnections().split("[,;]")) {
			if (!"".equals(connection)) {
				returnValue.add(connection);
			}
		}
		return returnValue;
	}
	@Override
	public boolean isDebugMode() {
		return true;
	}
	
	/**
	 * Wubiq client run.
	 * @param wubiqClientJar Wubiq client jar file.
	 * @param sentParameters Sent parameters.
	 * @return Integer value with the command output.
	 */
	private int wubiqClientRun(boolean setProcess, String... command) throws ConnectException {
		int returnValue = 0;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(command);
		Process currentProcess = null;
		try {
			currentProcess = processBuilder.start();
			StreamHandler stdOutHandler = new StreamHandler(currentProcess.getInputStream());
			StreamHandler stdErrorHandler = new StreamHandler(currentProcess.getErrorStream());
			Thread stdOut = new Thread(stdOutHandler, "StdOut");
			Thread stdErr = new Thread(stdErrorHandler, "StdErr");
			stdOut.start();
			stdErr.start();
			returnValue = currentProcess.waitFor();
			if (returnValue != 0) {
				throw new ConnectException(stdErrorHandler.getMessages());
			}
		} catch (ConnectException e) {
			throw e;
		} catch (Exception e) {
			doLog(e.getMessage(), 0);
			returnValue = 1;
		}
		if (setProcess) {
			process = currentProcess;
		}
		return returnValue;
	}
	
	private String serverVersion() throws ConnectException {
		return (String)pollServer(CommandKeys.READ_VERSION);
	}
	
	/**
	 * Handles the output and error stream of a running process.
	 * @author Federico Alcantara
	 *
	 */
	private class StreamHandler implements Runnable {
		private InputStream stream;
		private StringBuffer messages;
		
		private StreamHandler(InputStream stream) {
			this.stream = stream;
		}
		
		public void run() {
			BufferedReader reader = null;
			String line = null;
			messages = new StringBuffer("");
			try {
				reader = new BufferedReader(new InputStreamReader(stream));
				while ((line = reader.readLine()) != null) {
					doLog(line, 0);
					if (messages.length() > 0) {
						messages.append('\n');
					}
					messages.append(line);
					if (stopClient) {
						break;
					}
				}
			} catch (IOException e) {
				doLog(e.getMessage());
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
					}
				}
			}

		}

		/**
		 * @return the messages
		 */
		public String getMessages() {
			return messages.toString();
		}

	}
}
