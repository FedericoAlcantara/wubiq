/**
 * 
 */
package net.sf.wubiq.servlets;

import java.awt.print.Pageable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.wubiq.adapters.ReturnedData;
import net.sf.wubiq.android.ConversionServerUtils;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.DirectConnectKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.enums.DirectConnectCommand;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.PrinterJobManager;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IDirectConnectPrintJobManager;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;
import net.sf.wubiq.remote.RemoteClientManager;
import net.sf.wubiq.utils.DirectConnectUtils;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.Labels;
import net.sf.wubiq.utils.PdfUtils;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerLabels;
import net.sf.wubiq.utils.ServerWebUtils;
import net.sf.wubiq.utils.WebUtils;

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
	private long timeStamp = -1l;
	private IRemotePrintJobManager manager;

	public RemotePrintServlet() {
		timeStamp = new Date().getTime();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (ServletsStatus.isReady()) {
			String uuid = request.getParameter(ParameterKeys.UUID);
			String command = request.getParameter(ParameterKeys.COMMAND);
			if (command.equalsIgnoreCase(CommandKeys.IS_ACTIVE)) {
				isActiveCommand(uuid, request, response);
			} else  {
				if (CommandKeys.READ_VERSION.equalsIgnoreCase(command)) {
					respond(Labels.VERSION, response);
				} else if (CommandKeys.CONNECTION_TEST.equalsIgnoreCase(command)) {
					respond(ParameterKeys.CONNECTION_TEST_STRING, response);
				} else if (!Is.emptyString(uuid)) {
					LOG.debug("accesing:" + uuid);
					notifyRemote(uuid, request);
					manager = RemotePrintJobManagerFactory.getRemotePrintJobManager(uuid);
					
					if (!Is.emptyString(command)) {
						LOG.debug("command:" + command);
						if (command.equalsIgnoreCase(CommandKeys.KILL_MANAGER)) {
							killManagerCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.PAUSE_MANAGER)) {
							pauseManagerCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.RESUME_MANAGER)) {
							resumeManagerCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.IS_KILLED)) {
							isKilledCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.IS_REFRESHED)) {
							isRefreshedCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.BRING_ALIVE)) {
							bringAliveCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.SERVER_TIMESTAMP)) {
							serverTimestampCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_COMPUTER_NAME)) {
							registerComputerNameCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_PRINT_SERVICE)) {
							registerPrintServiceCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_PRINT_SERVICE_V2)) {
							registerPrintServiceCommandV2(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_MOBILE_PRINT_SERVICE)) {
							registerMobilePrintServiceCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REMOVE_ALL_PRINT_JOBS)) {
							getRemoveAllCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.PENDING_JOBS)) {
							getPendingJobsCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.PRINT_SERVICE_PENDING_JOBS)) {
							getPrintServicePendingJobsCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_SERVICE_NAME)) {
							getPrintServiceNameCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES)) {
							getPrintRequestAttributesCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_JOB_ATTRIBUTES)) {
							getPrintJobAttributesCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_DOC_ATTRIBUTES)) {
							getDocAttributesCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_DOC_FLAVOR)) {
							getDocFlavorCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_IS_DIRECT_CONNECT)) {
							isDirectConnectionCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.READ_PRINT_JOB)) {
							getPrintJobCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.CLOSE_PRINT_JOB)) {
							closePrintJobCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.DIRECT_CONNECT)) {
							directConnect(uuid, request, response);
						}
						
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
		respond(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.client_killed", uuid)), response);
	}
	
	/**
	 * Produces a text response with a 1 indicating that the pause command was executed.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void pauseManagerCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			client.setPaused(true);;
		}
		getRemoteClientManager(request).updateRemotes();
		respond(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.client_paused", uuid)), response);
	}

	/**
	 * Produces a text response with a 1 indicating that the resume command was executed.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void resumeManagerCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			client.setPaused(false);;
		}
		getRemoteClientManager(request).updateRemotes();
		respond(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.client_resumed", uuid)), response);
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
		if (client != null && client.isKilled()) {
			respond("1", response);
		} else {
			respond("0", response);
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
		if (client != null && client.isRemoteActive()) {
			respond("1", response);
		} else {
			respond("0", response);
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
		if (client != null && client.isRefreshed()) {
			respond("1", response);
		} else {
			respond("0", response);
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
		respond("alive", response);
	}

	/**
	 * Produces a text response with a 1 if the client manager has all print services registered.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void serverTimestampCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		respond(Long.toString(timeStamp), response);
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
		respond("ok", response);
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
			String serviceName = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_NAME));
			String categoriesString = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES));
			String docFlavors = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_DOC_FLAVORS));
			String directConnectEnabled = WebUtils.INSTANCE.decodeHtml(request.getParameter(DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER));
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setSupportedDocFlavors(PrintServiceUtils.deserializeDocumentFlavors(docFlavors));
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setMobile(false);
			remotePrintService.setDirectCommunicationEnabled("true".equalsIgnoreCase(directConnectEnabled));
			getRemoteClientManager(request).validateRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
			respond("ok", response);
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
	private void registerPrintServiceCommandV2(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		if (client != null) {
			String serviceName = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_NAME));
			String categoriesString = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES));
			String docFlavors = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_DOC_FLAVORS));
			String directConnectEnabled = WebUtils.INSTANCE.decodeHtml(request.getParameter(DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER));
			String clientVersion = WebUtils.INSTANCE.decodeHtml(request.getParameter(DirectConnectKeys.DIRECT_CONNECT_CLIENT_VERSION));
			String groups = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.GROUPS));
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.registerGroups(groups);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setSupportedDocFlavors((DocFlavor[])DirectConnectUtils.INSTANCE.deserialize(docFlavors));
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setMobile(false);
			remotePrintService.setDirectCommunicationEnabled("true".equalsIgnoreCase(directConnectEnabled));
			remotePrintService.setClientVersion(clientVersion);
			getRemoteClientManager(request).validateRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
			respond("ok", response);
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
			String serviceName = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_NAME));
			String categoriesString = WebUtils.INSTANCE.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES));
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setMobile(true);
			remotePrintService.setSupportedDocFlavors(new DocFlavor[]{PrintServiceUtils.DEFAULT_DOC_FLAVOR, 
					DocFlavor.SERVICE_FORMATTED.PAGEABLE, 
					DocFlavor.SERVICE_FORMATTED.PRINTABLE});
			getRemoteClientManager(request).validateRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
			respond("ok", response);
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
	protected void showPrintServicesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String tr = "<tr style='border:1px solid black'>";
		String th = "<th style='border:1px solid black'>";
		String td = "<td style='border:1px solid black'>";
		String tdc = "<td style='border:1px solid black; text-align:center' align='center'>";
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
		respond(buffer.toString(), response);
	}
	
	/**
	 * Remove all jobs for the print service.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getRemoveAllCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		PrintService printService = PrintServiceUtils.findPrinter(printServiceName, uuid);
		Integer count = 0;
		if (manager != null && printService != null) {
			for (Long jobId : manager.getPrintJobs(uuid, RemotePrintJobStatus.NOT_PRINTED)) {
				IRemotePrintJob printJob = manager.getRemotePrintJob(jobId, false);
				if (printService.equals(printJob.getPrintService())) {
					manager.removeRemotePrintJob(jobId);
					count++;
				}
			}
		}
		respond(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.jobs_removed", 
				count.toString(), printServiceName)), response);
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
		RemoteClient client = getRemoteClientManager(request).getRemoteClient(uuid);
		StringBuffer buffer = new StringBuffer("");
		if (!client.isPaused()) {
			if (manager != null) {
				for (Long printJobId : manager.getPrintJobs(uuid, RemotePrintJobStatus.NOT_PRINTED)) {
					if (buffer.length() > 0) {
						buffer.append(ParameterKeys.CATEGORIES_SEPARATOR);
					}
					buffer.append(printJobId);
				}
			}
		}
		if (buffer.length() > 0) {
			buffer.insert(0, ParameterKeys.PENDING_JOB_SIGNATURE);
		}
		respond(buffer.toString(), response);
	}
	
	/**
	 * Returns a list of pending jobs for the print service.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintServicePendingJobsCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Integer count = 0;
		StringBuffer buffer = new StringBuffer("");
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		PrintService printService = PrintServiceUtils.findPrinter(printServiceName, uuid);
		if (printService != null) {
			for (Long printJobId : manager.getPrintJobs(uuid, RemotePrintJobStatus.NOT_PRINTED)) {
				IRemotePrintJob remotePrintJob = manager.getRemotePrintJob(printJobId, false);
				if (remotePrintJob != null) {
					if (printService.getName().equals(remotePrintJob.getPrintService().getName())) {
						count++;
					}
				}

			}
		}
		buffer.append(count.toString());
		respond(buffer.toString(), response);
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
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		String printServiceName = "";
		if (manager != null) {
			IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
			if (printJob != null) {
				printServiceName = printJob.getPrintServiceName();
			}
		}
		respond(printServiceName, response);
	}
	
	/**
	 * Returns the serialization of the attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintRequestAttributesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		printJob.setStatus(RemotePrintJobStatus.PRINTING); // Perform only on the first request for processing remotely the print job.
		respond(PrintServiceUtils.serializeAttributes(printJob.getPrintRequestAttributeSet()), response);
	}

	/**
	 * Returns the serialization of the print job attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getPrintJobAttributesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		respond(PrintServiceUtils.serializeAttributes(printJob.getAttributes()), response);
	}

	/**
	 * Returns the serialization of the doc attributes.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getDocAttributesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		respond(PrintServiceUtils.serializeAttributes(printJob.getDocAttributeSet()), response);
	}

	/**
	 * Returns the serialization of the doc flavor.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void getDocFlavorCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		respond(PrintServiceUtils.serializeDocFlavor(printJob.getDocFlavor()), response);
	}
	
	/**
	 * Returns if the type of manager is direct connection.
	 * @param uuid Unique Id of the print service.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException 
	 * @throws IOException
	 */
	private void isDirectConnectionCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Boolean returnValue = (manager instanceof IDirectConnectPrintJobManager);
		respond(returnValue.toString(), response);
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
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), true);
		InputStream input = null;
		// If it is remote we must convert pdf to image and then scale it to print size
		try {
			if (RemotePrintServiceLookup.isMobile(uuid)) {
				input = ConversionServerUtils.INSTANCE.convertToMobile(printJob.getPrintServiceName(), printJob.getPrintData());
			} else {
				input = printJob.getPrintData();
			}
		} catch (Throwable e) {
			LOG.fatal(e.getMessage(), e);
			input = null;
		}
		if (input != null) {
			respond("application/pdf", input, response);
			input.close();
		} else {
			respond("", response);
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
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		manager.removeRemotePrintJob(Long.parseLong(jobId));
		respond("ok", response);
	}
	
	/**
	 * Creates a print test page to response.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void printTestPageCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String testPageName = ServerLabels.get("server.test_page_name");
		if (Is.emptyString(testPageName)) {
			testPageName = "TestPage.pdf";
		}
		if (RemotePrintServiceLookup.isMobile(uuid)) {
			testPageName = "MobileTestPage.pdf";
		}
//testPageName = "TestPage-calibrate.pdf";

		String testPage = "net/sf/wubiq/reports/" + testPageName;  
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		String printTestDirectPageable = request.getParameter(ParameterKeys.PRINT_TEST_DIRECT_PAGEABLE);
		boolean printDirectPageable = "true".equalsIgnoreCase(printTestDirectPageable);
		printServiceName = WebUtils.INSTANCE.decodeHtml(printServiceName);
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(testPage);
		PrintService printService = PrintServiceUtils.findPrinter(printServiceName, uuid);
		PrinterJobManager.initializePrinterJobManager();

		if (printService != null) {
			PrintRequestAttributeSet requestAttributes = new HashPrintRequestAttributeSet();
			requestAttributes.add(new JobName("Test page", Locale.getDefault()));
			requestAttributes.add(MediaSizeName.NA_LETTER);
			requestAttributes.add(new Copies(1));
			if (!printDirectPageable) {
				Doc doc = new SimpleDoc(input, DocFlavor.INPUT_STREAM.PDF, null);
				DocPrintJob printJob = printService.createPrintJob();
				try {
					printJob.print(doc, requestAttributes);
				} catch (PrintException e) {
					throw new ServletException(e);
				}				
			} else {
				PrinterJobManager.initializePrinterJobManager();
				PrinterJob printerJob = PrinterJob.getPrinterJob();
				Pageable pageable;
				try {
					pageable = PdfUtils.INSTANCE.pdfToPageable(input, printService, requestAttributes);
					synchronized(pageable) {
						printerJob.setPageable(pageable);
						try {
							printerJob.setPrintService(printService);
							printerJob.print(requestAttributes);
						} catch (PrinterException e) {
							LOG.error(e.getMessage(), e);
							throw new ServletException(e);
						}
					}
				} catch (PrintException e) {
					throw new ServletException(e);
				}
			}
			respond(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.test_page_sent", printServiceName)), response);
		} else {
			respond("application/pdf", input, response);
		}
		input.close();
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
	 * Handles the direct connect sub commands.
	 * @param uuid Unique printer uuid.
	 * @param request Originating request.
	 * @param response Output response.
	 * @throws IOException Thrown if any errors found.
	 */
	private void directConnect(String uuid, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int ordinal = Integer.parseInt(request.getParameter(DirectConnectKeys.DIRECT_CONNECT_PARAMETER));
		IDirectConnectPrintJobManager manager = (IDirectConnectPrintJobManager)this.manager;
		IDirectConnectorQueue directConnector = manager.directConnector(uuid);
		ReturnedData returnedData = null;
		String exception = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
		if (ordinal >= 0 && ordinal < DirectConnectCommand.values().length) {
			DirectConnectCommand command = DirectConnectCommand.values()[ordinal];
			String data = null;
			Object object = null;
			String jobIdString = request.getParameter(ParameterKeys.PRINT_JOB_ID);
			Long jobId = Long.parseLong(jobIdString);
			switch (command) {
				case START:
					directConnector.startPrintJob(jobId);
					respond("", response);
					break;
					
				case POLL:
					if (directConnector.isCommandToSendReady()) {
						RemoteCommand remoteCommand = directConnector.getCommandToSend();
						String serialized = DirectConnectUtils.INSTANCE.serialize(remoteCommand);
						respond("application/octet-stream", serialized, response);
						directConnector.resetCommandToSend();
					} else {
						respond("", response);
					}
					break;
					
				case DATA:
					data = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
					object = null;
					if (data != null) {
						object = DirectConnectUtils.INSTANCE.deserialize(data);
					}
					directConnector.queueReturnedData(new ReturnedData(object));
					respond("", response);
					break;
					
				case READ_REMOTE:
					data = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
					object = null;
					if (data != null) {
						object = DirectConnectUtils.INSTANCE.deserialize(data);
					}
					if (object instanceof RemoteCommand) {
						data = directConnector.callCommand(jobId, (RemoteCommand) object);
					}
					respond(data, response);
					break;
					
				case EXCEPTION:
					returnedData = new ReturnedData(exception);
					returnedData.setException(true);
					break;
					
				case RUNTIME_EXCEPTION:
					if (returnedData == null) {
						returnedData = new ReturnedData(exception);
						returnedData.setRuntimeException(true);
					}
					directConnector.queueReturnedData(returnedData);
					respond("", response);
					break;
			}
		}
	}
	
	/**
	 * Creates the answer for the response.
	 * @param text Text to return to the caller.
	 * @param response Response channel.
	 */
	private void respond(String text, HttpServletResponse response) {
		respond("text/html", text, response);
	}
	
	/**
	 * Creates the answer for the response.
	 * @param contentType Type of content to send.
	 * @param input Input stream.
	 * @param response Response channel.
	 */
	private void respond(String contentType, InputStream input, HttpServletResponse response) {
		response.setContentType(contentType);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			IOUtils.INSTANCE.copy(input, output);
			output.flush();
			response.setContentLength(output.toByteArray().length);
			ByteArrayInputStream newInput = new ByteArrayInputStream(output.toByteArray());
			IOUtils.INSTANCE.copy(newInput, response.getOutputStream());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the answer for the response.
	 * @param contentType Type of content to send.
	 * @param inputString Input string to return.
	 * @param response Response channel.
	 */
	private void respond(String contentType, String inputString, HttpServletResponse response) {
		response.setContentType(contentType);
		response.setContentLength(inputString.getBytes().length);
		try {
			ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
			IOUtils.INSTANCE.copy(input, response.getOutputStream());
			input.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
