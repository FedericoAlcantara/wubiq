package net.sf.wubiq.clients;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import net.sf.wubiq.enums.ServiceCommandType;
import net.sf.wubiq.enums.ServiceReturnStatus;
import net.sf.wubiq.utils.InstallerUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Takes care of handling the printers as services.
 * @author Federico Alcantara
 *
 */
public class WubiqLauncher implements Runnable {
	private static final Log LOG = LogFactory.getLog(WubiqLauncher.class);
	public boolean notAcceptingCommands = false;
	private boolean stop = false;
	
	/**
	 * Contains the fiscal printer remote wrapper and its associated thread.
	 * @author Federico Alcantara
	 *
	 */
	private class RunningComponent {
		private Thread thread;
		
		private RunningComponent(Thread thread) {
			this.thread = thread;
		}
	}
	
	private static Map<String, RunningComponent> clients;
	
	public static void start(String[] args) throws Exception {
		LOG.info("Starting the service and printers");
		ServiceReturnStatus currentServiceStatus = requestServiceCommand(ServiceCommandType.START);
		LOG.info(currentServiceStatus.getLabel());
		
	}
	
	public static void stop(String[] args) throws Exception {
		LOG.info("Stopping the services");
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
				Thread thread = new Thread(launcher, "FiscalPrinterLauncher");
				thread.start();
			} 
			Thread.sleep(1000);
			if ("start".equalsIgnoreCase(args[0])) {
				if (ServiceReturnStatus.OKEY.equals(currentServiceStatus)) {
					LOG.info("Service and printers already running!");
				} else {
					start(new String[]{});
				}
			} else if ("stop".equalsIgnoreCase(args[0])) {
				if (ServiceReturnStatus.OKEY.equals(currentServiceStatus)) {
					stop(new String[]{});
				} else {
					LOG.info("Service and printers already stopped");
				}
			} else if ("restart".equalsIgnoreCase(args[0])) {
				LOG.info("Restarting the service and printers");
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
		File wubiqClientJar = new File("./wubiq-client.jar");
		/*
		for (String fiscalPrinterId : FiscalPrinterUtils.INSTANCE.fiscalPrinterProperties()) {
			Properties properties = new Properties();
			File propertiesFile = FiscalPrinterUtils.INSTANCE.getFiscalPrinterProperty(fiscalPrinterId);
			if (propertiesFile != null && propertiesFile.exists()) {
				try {
					properties.load(new FileInputStream(propertiesFile));
					String fiscalPrinterDescription = properties.getProperty(Constants.PROPERTY_DESCRIPTION);
					String groupId = properties.getProperty(Constants.PROPERTY_GROUP_ID);
					String internetAddresses = properties.getProperty(Constants.PROPERTY_INTERNET_ADDRESSES);
					String[] connections = new String[]{};
					if (!Is.emptyString(internetAddresses)) {
						connections = internetAddresses.split("[,;]");
					}
					String serialPortId = properties.getProperty(Constants.PROPERTY_PORT);
					String type = properties.getProperty(Constants.PROPERTY_TYPE);
					FiscalPrinterType fiscalPrinterType = FiscalPrinterType.valueOf(type);
					IFiscalPrinter fiscalPrinter = 
							FiscalPrinterUtils.INSTANCE.createLocalFiscalPrinterInstance(fiscalPrinterType, fiscalPrinterId, fiscalPrinterDescription);
					if (fiscalPrinter instanceof AbstractSerialDriver) {
						((AbstractSerialDriver)fiscalPrinter).setSerialPortId(serialPortId);
					}
					
					RemoteFiscalPrinterWrapper remotePrinter = 
							new RemoteFiscalPrinterWrapper(groupId, fiscalPrinter, connections);
					Thread tr = new Thread(remotePrinter, remotePrinter.getFiscalPrinterId() + ":" + remotePrinter.getFiscalPrinterDescription());
					RunningComponent old = getDrivers().put(fiscalPrinterId, new RunningComponent(remotePrinter, tr));
					stopPrinterDriver(old);
					LOG.info("Starting printer:" 
					+ fiscalPrinterId
					+ " "
					+ fiscalPrinterDescription + "\n");
					tr.start();
				} catch (IOException e) {
					LOG.error(e.getMessage() + ":" + propertiesFile);
				}

			}
		}
		*/
	}
	
	/**
	 * Stops all clients.
	 */
	private void stopPrinterDrivers() {
		for (RunningComponent component : getClients().values()) {
			stopPrinterDriver(component);
		}
		getClients().clear();
	}
	
	/**
	 * Stops specific printer driver.
	 * @param component Printer driver to stop.
	 */
	private void stopPrinterDriver(RunningComponent component) {
		/*
		if (component != null) {
			LOG.info("Stopping printer:" 
					+ component.remote.getFiscalPrinterId() 
					+ " " 
					+ component.remote.getFiscalPrinterDescription() + "\n");
			component.remote.stop();
			try {
				component.thread.join();
			} catch (InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
		}
		*/
	}
	
	/**
	 * Creates or return an instance of the clients' map.
	 * @return Instance of the clients' map.
	 */
	private Map<String, RunningComponent> getClients() {
		if (clients == null) {
			clients = new HashMap<String, RunningComponent>();
		}
		return clients;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOG.info("Service and printers started");
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
						stopPrinterDrivers();
						stop = true;
						break;
					case RESTART:
						notAcceptingCommands = true;
						stopPrinterDrivers();
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
}
