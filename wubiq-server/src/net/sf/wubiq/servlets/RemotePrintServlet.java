/**
 * 
 */
package net.sf.wubiq.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.wubiq.android.ConversionServerUtils;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.jobs.impl.PrintJobInputStream;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;
import net.sf.wubiq.remote.RemoteClientManager;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerLabels;
import net.sf.wubiq.utils.ServerPrintDirectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the communication between clients and server.
 * @author Federico Alcantara
 *
 */
public class RemotePrintServlet extends HttpServlet {
	private static final Log LOG = LogFactory.getLog(RemotePrintServlet.class);
	private static final long serialVersionUID = 1L;
	private static ServerPrintDirectUtils printDirectUtils;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uuid = request.getParameter(ParameterKeys.UUID);
		String command = request.getParameter(ParameterKeys.COMMAND);
		if (command.equalsIgnoreCase(CommandKeys.IS_ACTIVE)) {
			isActiveCommand(uuid, request, response);
		} else  {
			notifyRemote(uuid, request);
			if (command.equalsIgnoreCase(CommandKeys.PRINT_TEST_PAGE)) {
				printTestPageCommand(uuid, request, response);
			} else if (command.equalsIgnoreCase(CommandKeys.SHOW_PRINT_SERVICES)) {
				showPrintServicesCommand("", request, response);
			}
			if (!Is.emptyString(uuid)) {
				LOG.debug("accesing:" + uuid);
				if (!Is.emptyString(command)) {
					LOG.debug("command:" + command);
					if (command.equalsIgnoreCase(CommandKeys.KILL_MANAGER)) {
						killManagerCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.IS_KILLED)) {
						isKilledCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.IS_REFRESHED)) {
						isRefreshedCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.BRING_ALIVE)) {
						bringAliveCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_COMPUTER_NAME)) {
						registerComputerNameCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_PRINT_SERVICE)) {
						registerPrintServiceCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_MOBILE_PRINT_SERVICE)) {
						registerMobilePrintServiceCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.PENDING_JOBS)) {
						getPendingJobsCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_SERVICE_NAME)) {
						getPrintServiceNameCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.READ_DOC_FLAVOR)) {
						getDocFlavorCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_ATTRIBUTES)) {
						getPrintAttributesCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_JOB)) {
						getPrintJobCommand(uuid, request, response);
					} else if (command.equalsIgnoreCase(CommandKeys.CLOSE_PRINT_JOB)) {
						closePrintJobCommand(uuid, request, response);
					}
					
				}
			}
		}
	}
	
	/**
	 * Produces a text response with a 1 indicating that the kill command was executed.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void killManagerCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			client.setKilled(true);
			RemotePrintServiceLookup.removePrintServices(uuid);
		}
		getRemoteClientManager(request).updateRemotes();
		response.setContentType("text/html");
		response.getWriter().print("killed");
	}
	
	/**
	 * Produces a text response with a 1 if the manager is killed.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void isKilledCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		response.setContentType("text/html");
		if (client != null && client.isKilled()) {
			response.getWriter().print("1");
		} else {
			response.getWriter().print("0");
		}
	}
	
	/**
	 * Produces a text response with a 1 if the client manager is active.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void isActiveCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		response.setContentType("text/html");
		if (client != null && client.isRemoteActive()) {
			response.getWriter().print("1");
		} else {
			response.getWriter().print("0");
		}
	}

	/**
	 * Produces a text response with a 1 if the client manager has all print services registered.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void isRefreshedCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		response.setContentType("text/html");
		if (client != null && client.isRefreshed()) {
			response.getWriter().print("1");
		} else {
			response.getWriter().print("0");
		}
	}

	/**
	 * Sets remote client to true.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void bringAliveCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			client.setKilled(false);
		} else {
			client = new RemoteClient();
			getRemoteClientManager(request).addRemote(uuid, client);
		}
		response.setContentType("text/html");
		response.getWriter().print("alive");
	}

	/**
	 * Registers computer and clear its print services.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void registerComputerNameCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		notifyRemote(uuid, request);
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		client.setServices(null);
		client.setComputerName(request.getRemoteAddr());
		client.setRefreshed(true);
		response.setContentType("text/html");
		response.getWriter().print("ok");
	}
	
	/**
	 * Registers a printService along with its categories and attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void registerPrintServiceCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			String serviceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
			String categoriesString = request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES);
			String docFlavors = request.getParameter(ParameterKeys.PRINT_SERVICE_DOC_FLAVORS);
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setSupportedDocFlavors(PrintServiceUtils.deserializeDocumentFlavors(docFlavors));
			remotePrintService.setMobile(false);
			getRemoteClientManager(request).validateRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
			response.setContentType("text/html");
			response.getWriter().print("ok");
		}
	}
	
	/**
	 * Registers a printService along with its categories and attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void registerMobilePrintServiceCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			String serviceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
			String categoriesString = request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES);
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setMobile(true);
			remotePrintService.setSupportedDocFlavors(new DocFlavor[]{PrintServiceUtils.DEFAULT_DOC_FLAVOR});
			getRemoteClientManager(request).validateRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
			response.setContentType("text/html");
			response.getWriter().print("ok");
		}
	}
	
	/**
	 * Show current printer services.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void showPrintServicesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		String tr = "<tr style='border:1px solid black'>";
		String th = "<th style='border:1px solid black'>";
		String td = "<td style='border:1px solid black'>";
		String tdc = "<td style='border:1px solid black; text-align:center' align='center'>";
		PrintWriter writer = response.getWriter();
		StringBuffer buffer = new StringBuffer("")
			.append("<table style='border:2px solid black; background-color:#FFFAFA' id='")
			.append(WebKeys.SHOW_SERVICES_TABLE_ID)
			.append('\'')
			.append('>')
			.append(tr)
			.append(th)
			.append(ServerLabels.get("server.service_name"))
			.append("</th>")
			.append(th)
			.append(ServerLabels.get("server.remote"))
			.append("</th>")
			.append(th)
			.append(ServerLabels.get("server.uuid"))
			.append("</th>")
			.append("</tr>");
		String remoteNo=ServerLabels.get("server.remote_no");
		String remoteYes=ServerLabels.get("server.remote_yes");
		for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			boolean remote = false;
			String remoteUuid = "";
			if (printService instanceof RemotePrintService) {
				remote = true;
				remoteUuid = ((RemotePrintService)printService).getUuid();
			}
			buffer.append(tr)
				.append(td)
				.append(printService.getName())
				.append("</td>")
				.append(td)
				.append(remote ? remoteYes : remoteNo)
				.append("</td>")
				.append(tdc)
				.append(remoteUuid)
				.append("</td>")
				.append("</tr>");
		}
		buffer.append("</table");
		writer.print(buffer.toString());
	}
	/**
	 * Returns a list of pending jobs.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPendingJobsCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		StringBuffer buffer = new StringBuffer("");
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		for (Long printJobId : manager.getPrintJobs(uuid, RemotePrintJobStatus.NOT_PRINTED)) {
			if (buffer.length() > 0) {
				buffer.append(ParameterKeys.CATEGORIES_SEPARATOR);
			}
			buffer.append(printJobId);
		}
		if (buffer.length() > 0) {
			buffer.insert(0, ParameterKeys.PENDING_JOB_SIGNATURE);
		}
		writer.print(buffer);
	}
	
	/**
	 * Returns the name of the print service name.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintServiceNameCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId));
		writer.print(printJob.getPrintServiceName());
	}
	
	/**
	 * Returns the document flavor used to encode the document.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getDocFlavorCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId));
		writer.print(PrintServiceUtils.serializeDocFlavor(printJob.getDocFlavor()));
	}

	/**
	 * Returns the serialization of the attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintAttributesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId));
		printJob.setStatus(RemotePrintJobStatus.PRINTING);
		writer.print(PrintServiceUtils.serializeAttributes(printJob.getAttributes()));
	}

	/**
	 * Returns the print data.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintJobCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/pdf");				
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId));
		OutputStream output = response.getOutputStream();
		InputStream input = null;
		// If it is remote we must convert pdf to image and then scale it to print size
		if (RemotePrintServiceLookup.isMobile(uuid)) {
			input = ConversionServerUtils.INSTANCE.convertToMobile(printJob.getPrintServiceName(), printJob.getPrintDocument());
		} else {
			if (!printJob.isConverted()) {
				List<InputStream> inputs = PrintServiceUtils.convertToProperStream(printJob.getPrintDocument(), printJob.getDocFlavor());
				if (inputs.size() == 1) {
					input = inputs.get(0);
				} else {
					input = null;
					if (inputs.size() > 1) {
						for (InputStream readInput : inputs) {
							IRemotePrintJob remotePrintJob = new PrintJobInputStream(printJob.getPrintServiceName(), 
									readInput, null, printJob.getDocFlavor());
							remotePrintJob.setConverted(true);
							manager.addRemotePrintJob(uuid, remotePrintJob);
						}
					}
				}
			} else {
				input = printJob.getPrintDocument();
			}
		}
		if (input != null) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			input.close();
		} else {
			response.setContentType("text/html");
		}
	}
	
	/**
	 * Closes the printJob.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void closePrintJobCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");				
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
		manager.removeRemotePrintJob(Long.parseLong(jobId));
		response.setContentType("text/html");
		response.getWriter().print("ok");
	}
	
	/**
	 * Creates a print test page to response.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void printTestPageCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String testPageName = ServerLabels.get("server.test_page_name");
		testPageName = "FivePagesTest.pdf";
		if (Is.emptyString(testPageName)) {
			testPageName = "TestPage.pdf";
		}
		if (RemotePrintServiceLookup.isMobile(uuid)) {
			testPageName = "MobileTestPage.pdf";
		}
		String testPage = "net/sf/wubiq/reports/" + testPageName;  
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(testPage);
		boolean toRemote = false;
		if (!Is.emptyString(uuid) 
				&& !Is.emptyString(printServiceName)) {
			toRemote = true;
		}
		if (input != null) {
			if (toRemote) {
				if (printServiceName.contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
					printServiceName = printServiceName.substring(0, printServiceName.indexOf(WebKeys.REMOTE_SERVICE_SEPARATOR));
				}
				PrintService printService = RemotePrintServiceLookup.find(uuid, printServiceName);
				DocFlavor docFlavor = PrintServiceUtils.DEFAULT_DOC_FLAVOR;
				if (printService != null) {
					docFlavor = PrintServiceUtils.determineDocFlavor(printService);
				}
				response.setContentType("text/html");				
				IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
				IRemotePrintJob remotePrintJob = new PrintJobInputStream(printServiceName, input, null, docFlavor);
				manager.addRemotePrintJob(uuid, remotePrintJob);
				response.getWriter().print(ServerLabels.get("server.test_page_sent", printServiceName));
			} else {
				if (Is.emptyString(printServiceName)) {
					response.setContentType("application/pdf");				
					OutputStream output = response.getOutputStream();
					while (input.available() > 0) {
						output.write(input.read());
					}
				} else {
					getPrintDirectUtils().print("testpage-0", printServiceName, null, input);
					response.getWriter().print(ServerLabels.get("server.test_page_sent", printServiceName));
				}
			}
			input.close();
		} else {
			if (toRemote) {
				response.getWriter().print(ServerLabels.get("server.error_test_page_sent", printServiceName));
			}
		}
	}
		
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	/**
	 * Notify the remote object about access to it.
	 * @param uuid Unique identifier for the connecting computer.
	 * @param request Originating request.
	 */
	private void notifyRemote(String uuid, HttpServletRequest request) {
		if (!Is.emptyString(uuid)) {
			RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
			if (client == null) {
				client = new RemoteClient();
				client.setComputerName(request.getRemoteAddr());
				getRemoteClientManager(request).addRemote(uuid, client);
			}
			client.setLastAccessedTime(new Date().getTime());
		}
	}

	/**
	 * Gets the session associated remoteClientManager.
	 * @param request Originating request.
	 * @return Remote Client manager.
	 */
	private RemoteClientManager getRemoteClientManager(HttpServletRequest request) {
		return RemoteClientManager.getRemoteClientManager(request);
	}
	
	/**
	 * @return the printDirectUtils
	 */
	private static ServerPrintDirectUtils getPrintDirectUtils() {
		if (printDirectUtils == null) {
			printDirectUtils = new ServerPrintDirectUtils();
		}
		return printDirectUtils;
	}
	
}
