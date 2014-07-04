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
import java.util.HashSet;
import java.util.Set;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.InstallerProperties;
import net.sf.wubiq.utils.InstallerUtils;

/**
 * @author Federico Alcantara
 *
 */
public class RunningClient extends AbstractLocalPrintManager implements Runnable {
	private boolean stopClient;
	private JavaRun javaRun;
	private Process process;
	private WubiqLauncher launcher;
	
	public RunningClient(WubiqLauncher launcher) {
		this.launcher = launcher;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				killClient();
				if (stopClient) {
					break;
				}
				try {
					createJavaRun();
					runClient();
				} catch (ConnectException e) {
					try {
						Thread.sleep(1000);
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
		File wubiqClientJar = InstallerUtils.INSTANCE.wubiqClientFile();

		javaRun = new JavaRun();
		javaRun.setFixedParameters("-u", InstallerProperties.getUuid(), 
				InstallerProperties.getClientParameters());
		javaRun.setJvmParameters("-D" + PropertyKeys.WUBIQ_CLIENT_CONNECTION_RETRIES + "=3");
		javaRun.setJarFile(wubiqClientJar.getPath());
		boolean loadNewJar = true;
		
		String serverVersion = serverVersion();
		if (wubiqClientJar.exists()) {
			String clientVersion = InstallerUtils.INSTANCE.wubiqClientVersion();
			if (clientVersion.equals(serverVersion)) {
				loadNewJar = false;
			}
		}
		if (loadNewJar) {
			try {
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
			} catch (MalformedURLException e) {
				doLog(e.getMessage(), 0);
				throw new RuntimeException(e);
			}
		}
	}
	
	private void runClient() {
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
	protected void processPendingJob(String jobId) throws ConnectException {
		// Here do nothing
	}

	@Override
	protected void registerPrintServices() throws ConnectException {
		// Here do nothing
	}
	
	@Override
	public String getApplicationName() {
		return InstallerProperties.getApplicationName();
	}
	
	@Override
	public String getServletName() {
		return InstallerProperties.getServletName();
	}
	
	@Override
	public String getUuid() {
		return InstallerProperties.getUuid();
	}
	
	@Override
	public Set<String> getConnections() {
		Set<String> returnValue = new HashSet<String>();
		for (String connection : InstallerProperties.getConnections().split("[,;]")) {
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
	private int wubiqClientRun(boolean setProcess, String... command) {
		int returnValue = 0;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(command);
		Process currentProcess = null;
		try {
			currentProcess = processBuilder.start();
			Thread stdOut = new Thread(new StreamHandler(currentProcess.getInputStream()), "StdOut");
			Thread stdErr = new Thread(new StreamHandler(currentProcess.getErrorStream()), "StdErr");
			stdOut.start();
			stdErr.start();
			returnValue = currentProcess.waitFor();
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
		
		private StreamHandler(InputStream stream) {
			this.stream = stream;
		}
		public void run() {
			BufferedReader reader = null;
			String line = null;
			try {
				reader = new BufferedReader(new InputStreamReader(stream));
				while ((line = reader.readLine()) != null) {
					doLog(line, 0);
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
	}
}
