/**
 * 
 */
package net.sf.wubiq.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerLabels;

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
				notifyRemote(uuid, request);
				if (!Is.emptyString(uuid)) {
					LOG.debug("accesing:" + uuid);
					
					manager = RemotePrintJobManagerFactory.getRemotePrintJobManager(uuid);
					
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
						} else if (command.equalsIgnoreCase(CommandKeys.SERVER_TIMESTAMP)) {
							serverTimestampCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_COMPUTER_NAME)) {
							registerComputerNameCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_PRINT_SERVICE)) {
							registerPrintServiceCommand(uuid, request, response);
						} else if (command.equalsIgnoreCase(CommandKeys.REGISTER_MOBILE_PRINT_SERVICE)) {
							registerMobilePrintServiceCommand(uuid, request, response);
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
		response.setContentType("text/html");
		
		response.getWriter().print(backResponse(request, ServerLabels.get("server.client_killed", uuid)));
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
	 * Produces a text response with a 1 if the client manager has all print services registered.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void serverTimestampCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.getWriter().print(timeStamp);
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
			String serviceName = PrintServiceUtils.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_NAME));
			String categoriesString = PrintServiceUtils.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES));
			String docFlavors = PrintServiceUtils.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_DOC_FLAVORS));
			String directConnectEnabled = PrintServiceUtils.decodeHtml(request.getParameter(DirectConnectKeys.DIRECT_CONNECT_ENABLED_PARAMETER));
			RemotePrintService remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteComputerName(client.getComputerName());
			remotePrintService.setSupportedDocFlavors(PrintServiceUtils.deserializeDocumentFlavors(docFlavors));
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setMobile(false);
			remotePrintService.setDirectCommunicationEnabled("true".equalsIgnoreCase(directConnectEnabled));
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
			String serviceName = PrintServiceUtils.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_NAME));
			String categoriesString = PrintServiceUtils.decodeHtml(request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES));
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
	protected void showPrintServicesCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
		if (manager != null) {
			for (Long printJobId : manager.getPrintJobs(uuid, RemotePrintJobStatus.NOT_PRINTED)) {
				if (buffer.length() > 0) {
					buffer.append(ParameterKeys.CATEGORIES_SEPARATOR);
				}
				buffer.append(printJobId);
			}
		}
		if (buffer.length() > 0) {
			buffer.insert(0, ParameterKeys.PENDING_JOB_SIGNATURE);
		}
		writer.print(buffer);
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
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
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
		if (manager != null) {
			IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
			if (printJob != null) {
				writer.print(printJob.getPrintServiceName());
			}
		}
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
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		printJob.setStatus(RemotePrintJobStatus.PRINTING); // Perform only on the first request for processing remotely the print job.
		writer.print(PrintServiceUtils.serializeAttributes(printJob.getPrintRequestAttributeSet()));
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
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		writer.print(PrintServiceUtils.serializeAttributes(printJob.getAttributes()));
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
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		writer.print(PrintServiceUtils.serializeAttributes(printJob.getDocAttributeSet()));
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
		response.setContentType("text/html");
		String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
		PrintWriter writer = response.getWriter();
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), false);
		writer.print(PrintServiceUtils.serializeDocFlavor(printJob.getDocFlavor()));
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
		StringBuffer responseOutput = new StringBuffer("");
		IRemotePrintJob printJob = manager.getRemotePrintJob(Long.parseLong(jobId), true);
		InputStream input = null;
		// If it is remote we must convert pdf to image and then scale it to print size
		if (RemotePrintServiceLookup.isMobile(uuid)) {
			input = ConversionServerUtils.INSTANCE.convertToMobile(printJob.getPrintServiceName(), printJob.getPrintData());
		} else {
			input = printJob.getPrintData();
		}
		if (input != null) {
			OutputStream output = response.getOutputStream();
			while (input.available() > 0) {
				output.write(input.read());
			}
			input.close();
		} else {
			response.setContentType("text/html");
			PrintWriter writer = response.getWriter();
			writer.write(responseOutput.toString());
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
	protected void printTestPageCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String testPageName = ServerLabels.get("server.test_page_name");
		if (Is.emptyString(testPageName)) {
			testPageName = "TestPage.pdf";
		}
		if (RemotePrintServiceLookup.isMobile(uuid)) {
			testPageName = "MobileTestPage.pdf";
		}
		String testPage = "net/sf/wubiq/reports/" + testPageName;  
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(testPage);
		PrintService printService = PrintServiceUtils.findPrinter(printServiceName, uuid);
		response.setContentType("text/html");				
		if (printService != null) {
			PrintRequestAttributeSet requestAttributes = new HashPrintRequestAttributeSet();
			requestAttributes.add(new JobName("Test page", Locale.getDefault()));
			requestAttributes.add(MediaSizeName.NA_LETTER);
			requestAttributes.add(new Copies(1));
			//if ((printService instanceof RemotePrintService) &&
			//		((RemotePrintService)printService).isMobile()) {
				Doc doc = new SimpleDoc(input, DocFlavor.INPUT_STREAM.PDF, null);
				DocPrintJob printJob = printService.createPrintJob();
				try {
					printJob.print(doc, requestAttributes);
				} catch (PrintException e) {
					throw new ServletException(e);
				}
			/*	
			} else {
				PrinterJobManager.initializePrinterJobManager();
				PrinterJob printerJob = PrinterJob.getPrinterJob();
				Pageable pageable;
				try {
					pageable = PdfUtils.INSTANCE.pdfToPageable(input, printerJob);
					synchronized(pageable) {
						printerJob.setPageable(pageable);
						try {
							printerJob.setPrintService(printService);
							printerJob.print(requestAttributes);
						} catch (PrinterException e) {
							LOG.error(e.getMessage(), e);
							throw new ServletException(e);
						} finally {
							if (pageable != null) {
								if (pageable instanceof PDDocument) {
									((PDDocument)pageable).close();
								} else if (pageable instanceof PdfPageable) {
									((PdfPageable)pageable).close();
								}
							}
						}
					}
				} catch (PrintException e) {
					throw new ServletException(e);
				}
			}
			*/
			input.close();
			PrintWriter writer = response.getWriter();
			writer.print(backResponse(request, ServerLabels.get("server.test_page_sent", printServiceName)));
		} else {
			response.setContentType("application/pdf");				
			OutputStream output = response.getOutputStream();
			while (input.available() > 0) {
				output.write(input.read());
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
	 * Creates a back response after performing actions that produce a user information.
	 * @param request Originating request.
	 * @param body Contents to be shown in the html body. 
	 * @return A Html string to be used.
	 * @throws IOException
	 */
	private String backResponse(HttpServletRequest request, String body) throws IOException {
		StringBuffer returnValue = new StringBuffer("");
		String context = request.getContextPath().substring(1);
		
		String url =  "/" + context;
		returnValue.append("<html>")
			.append("<header>")
			.append("<meta http-equiv=\"refresh\" content=\"3," + url + "\"/>")
			.append("</header>")
			.append("<body>")
			.append(body)
			.append("</body>")
			.append("</html>");

		return returnValue.toString();
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
		String dataUUID = null;
		String exception = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
		if (ordinal >= 0 && ordinal < DirectConnectCommand.values().length) {
			DirectConnectCommand command = DirectConnectCommand.values()[ordinal];
			String data = null;
			Object object = null;
			switch (command) {
				case START:
					String jobId = request.getParameter(ParameterKeys.PRINT_JOB_ID);
					directConnector.startPrintJob(Long.parseLong(jobId));
					response.setContentType("text/html");
					response.setContentLength(0);
					break;
					
				case POLL:
					response.setContentType("text/html");
					if (directConnector.isCommandToSendReady()) {
						RemoteCommand remoteCommand = directConnector.getCommandToSend();
						String serialized = DirectConnectUtils.INSTANCE.serialize(remoteCommand);
						ByteArrayInputStream input = new ByteArrayInputStream(serialized.getBytes());
						response.setContentType("application/octet-stream");
						response.setContentLength(serialized.getBytes().length);
						
						OutputStream output = response.getOutputStream();
						IOUtils.INSTANCE.copy(input, output);
						input.close();
						directConnector.resetCommandToSend();
					}
					break;
					
				case DATA:
					data = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
					object = null;
					if (data != null) {
						object = DirectConnectUtils.INSTANCE.deserialize(data);
					}
					directConnector.queueReturnedData(new ReturnedData(object));
					response.setContentType("text/html");
					response.setContentLength(0);
					break;
					
				case READ_REMOTE:
					data = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA);
					dataUUID = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA_UUID);
					object = null;
					if (data != null) {
						object = DirectConnectUtils.INSTANCE.deserialize(data);
					}
					if (object instanceof RemoteCommand) {
						directConnector.callCommand((RemoteCommand) object, dataUUID);
					}
					response.setContentType("text/html");
					response.setContentLength(0);
					break;

				case POLL_REMOTE_DATA:
					dataUUID = request.getParameter(DirectConnectKeys.DIRECT_CONNECT_DATA_UUID);
					response.setContentType("text/html");
					data = directConnector.getRemoteData(dataUUID);
					response.getWriter().print(data);
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
					response.setContentType("text/html");
					response.setContentLength(0);
					break;
			}
		}
	}
	
}
