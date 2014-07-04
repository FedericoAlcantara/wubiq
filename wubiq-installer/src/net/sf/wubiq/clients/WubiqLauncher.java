package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import net.sf.wubiq.enums.ServiceCommandType;
import net.sf.wubiq.enums.ServiceReturnStatus;
import net.sf.wubiq.utils.InstallerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Takes care of handling the wubiq client as a service.
 * @author Federico Alcantara
 *
 */
public class WubiqLauncher implements Runnable {
	private static final Log LOG = LogFactory.getLog(WubiqLauncher.class);
	public boolean notAcceptingCommands = false;
	private boolean stop = false;
	private RunningClient runningClient;
	private boolean keepRunning = false;
	
	
	public static void start(String[] args) throws Exception {
		LOG.info("Starting the Wubiq Client");
		ServiceReturnStatus currentServiceStatus = requestServiceCommand(ServiceCommandType.START);
		LOG.info(currentServiceStatus.getLabel());
		
	}
	
	public static void stop(String[] args) throws Exception {
		LOG.info("Stopping the Wubiq Client");
		ServiceReturnStatus currentServiceStatus = requestServiceCommand(ServiceCommandType.STOP);
		LOG.info(currentServiceStatus.getLabel());
	}
	
	/**
	 * Start up the fiscal printer device.
	 * 
	 * @param args Arguments.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			ServiceReturnStatus currentServiceStatus = requestServiceCommand(ServiceCommandType.STATUS);
			if (ServiceReturnStatus.NO_SERVICE.equals(currentServiceStatus) &&
					!"stop".equalsIgnoreCase(args[0])) {
				WubiqLauncher launcher = new WubiqLauncher();
				Thread thread = new Thread(launcher, "WubiqLauncher");
				thread.start();
			} 
			Thread.sleep(1000);
			if ("start".equalsIgnoreCase(args[0])) {
				if (ServiceReturnStatus.OKEY.equals(currentServiceStatus)) {
					LOG.info("Wubiq service already running!");
				} else {
					start(new String[]{});
				}
			} else if ("stop".equalsIgnoreCase(args[0])) {
				if (ServiceReturnStatus.OKEY.equals(currentServiceStatus)) {
					stop(new String[]{});
				} else {
					LOG.info("Wubiq service already stopped");
				}
			} else if ("restart".equalsIgnoreCase(args[0])) {
				LOG.info("Restarting the Wubiq service");
				currentServiceStatus = requestServiceCommand(ServiceCommandType.RESTART);
				LOG.info(currentServiceStatus.getLabel());
			}
		}
	}
	
	/**
	 * Request service command.
	 * @param command Command to request.
	 * @return Status returned of the command.
	 */
	private static ServiceReturnStatus requestServiceCommand(ServiceCommandType command) {
		ServiceReturnStatus returnValue = ServiceReturnStatus.NO_SERVICE;
		Socket socket = null;
		PrintWriter out = null;
		try {
			socket = new Socket(InetAddress.getLocalHost(), InstallerUtils.INSTANCE.getPortAddress());
			out = new PrintWriter(socket.getOutputStream(), true);
			out.write(command.ordinal() + "\n");
			out.flush();
			String inputLine = "";
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				loop:
				while (true) {
					while ((inputLine = in.readLine()) != null) {
						break loop;
					}
				}
			} catch (IOException e){
				LOG.error(e.getMessage());
			}

			try {
				int serviceReturn = Integer.parseInt(inputLine);
				if (serviceReturn >= 0 &&
						serviceReturn <= ServiceReturnStatus.values().length) {
					returnValue = ServiceReturnStatus.values()[serviceReturn];
				}
			} catch (NumberFormatException e) {
				LOG.error(e.getMessage(), e);
				returnValue = ServiceReturnStatus.UNKNOWN;
			}

		} catch (UnknownHostException e) {
			LOG.debug(e.getMessage());
			returnValue = ServiceReturnStatus.NO_SERVICE;
		} catch (IOException e) {
			LOG.debug(e.getMessage());
			returnValue = ServiceReturnStatus.NO_SERVICE;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return returnValue;
	}

	
	/**
	 * Starts all printer clients.
	 */
	private void startWubiqClient()  {
		stopWubiqClient();
		keepRunning = true;
		runningClient = new RunningClient(this);
		Thread thread = new Thread(runningClient, "RunningClient");
		thread.start();
	}
		
	/**
	 * Stops specific printer driver.
	 * @param component Printer driver to stop.
	 */
	private void stopWubiqClient() {
		keepRunning = false;
		if (runningClient != null) {
			runningClient.stop();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOG.fatal(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
		runningClient = null;
	}
	
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOG.info("Wubiq client started");
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(InstallerUtils.INSTANCE.getPortAddress());
			while (!stop) {
				Socket clientSocket = null;
				try {
					clientSocket = serverSocket.accept();
					LOG.info("Command requested");
					handleClient(clientSocket);
					if (notAcceptingCommands) {
						break; // ends listener.
					}
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
					// This won't cancel.
				} finally {
					if (clientSocket != null) {
						clientSocket.close();
					}
				}
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		System.exit(0);
	}

	/**
	 * Handles the client socket.
	 * @param clientSocket Client socket to handle.
	 * @throws IOException thrown if any communication error.
	 */
	private void handleClient(final Socket clientSocket) {
		String inputLine = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			loop:
			while (true) {
				while ((inputLine = in.readLine()) != null) {
					break loop;
				}
			}
		} catch (IOException e){
			LOG.error(e.getMessage());
		}
		PrintWriter out = null;

		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);

			int service = Integer.parseInt(inputLine);
			if (service >= 0 && service < ServiceCommandType.values().length) {
				ServiceCommandType command = ServiceCommandType.values()[service];
				switch (command) {
					case START:
						startWubiqClient();
						break;
					case STOP:
						notAcceptingCommands = true;
						stopWubiqClient();
						stop = true;
						break;
					case RESTART:
						notAcceptingCommands = true;
						stopWubiqClient();
						startWubiqClient();
						notAcceptingCommands = false;
						break;
					case STATUS:
						break;
				}
				if (!notAcceptingCommands) {
					out.write(ServiceReturnStatus.OKEY.ordinal() + "\n");
				} else {
					out.write(ServiceReturnStatus.NOT_ACCEPTING_COMMANDS.ordinal() + "\n");
				}
				out.flush();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			// Garbage or unsupported command
			LOG.error(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage());
		}				
	}
	
	public void notifyThreadStopped() {
		if (keepRunning) {
			startWubiqClient();
		}
	}
}
