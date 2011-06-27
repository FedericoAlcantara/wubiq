/**
 * 
 */
package org.wubiq.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wubiq.common.AttributeInputStream;
import org.wubiq.common.CommandKeys;
import org.wubiq.common.ParameterKeys;
import org.wubiq.common.WebKeys;
import org.wubiq.data.RemoteInfo;
import org.wubiq.print.jobs.IRemotePrintJob;
import org.wubiq.print.jobs.RemotePrintJobStatus;
import org.wubiq.print.jobs.impl.PrintJobInputStream;
import org.wubiq.print.managers.IRemotePrintJobManager;
import org.wubiq.print.managers.impl.RemotePrintJobManagerFactory;
import org.wubiq.print.services.RemotePrintService;
import org.wubiq.print.services.RemotePrintServiceLookup;
import org.wubiq.utils.Is;
import org.wubiq.utils.PrintServiceUtils;
import org.wubiq.utils.ServerLabels;

/**
 * Handles the communication between clients and server.
 * @author Federico Alcantara
 *
 */
public class RemotePrintServlet extends HttpServlet {
	private static final Log LOG = LogFactory.getLog(RemotePrintServlet.class);
	private static final long serialVersionUID = 1L;
	private static Map<String, RemoteInfo> remotes;
	private static RemotePrintServiceLookup remoteLookup;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uuid = request.getParameter(ParameterKeys.UUID);
		String command = request.getParameter(ParameterKeys.COMMAND);
		if (command.equals(CommandKeys.PRINT_TEST_PAGE)) {
			printTestPageCommand(uuid, request, response);
		} else if (command.equals(CommandKeys.SHOW_PRINT_SERVICES)) {
			showPrintServicesCommand("", request, response);
		}
		if (!Is.emptyString(uuid)) {
			LOG.debug("accesing:" + uuid);
			if (!Is.emptyString(command)) {
				LOG.debug("command:" + command);
				if (command.equals(CommandKeys.CAN_CONNECT)) {
					canConnectCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.KILL_MANAGER)) {
					killManagerCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.IS_KILLED)) {
					isKilledCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.REGISTER_COMPUTER_NAME)) {
					registerComputerNameCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.REGISTER_PRINT_SERVICE)) {
					registerPrintServiceCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.PENDING_JOBS)) {
					getPendingJobsCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.READ_PRINT_SERVICE_NAME)) {
					getPrintServiceNameCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.READ_PRINT_ATTRIBUTES)) {
					getPrintAttributesCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.READ_PRINT_JOB)) {
					getPrintJobCommand(uuid, request, response);
				} else if (command.equals(CommandKeys.CLOSE_PRINT_JOB)) {
					closePrintJobCommand(uuid, request, response);
				}
				
			}
			notifyRemote(uuid, request);
		}
	}

	/**
	 * Produces a text response with a 1 for can connect and 0 for no connection allowed.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void canConnectCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		RemoteInfo info = remotes().get(uuid);
		if (info == null ||
				!info.isRemoteActive() ||
				info.isKilled()) {
			if (info != null) {
				info.setKilled(false);
			}
			response.getWriter().print('1');
		} else {
			response.getWriter().print('0');
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
		RemoteInfo info = remotes().get(uuid);
		if (info != null) {
			info.setKilled(true);
			getRemoteLookup();
			RemotePrintServiceLookup.removePrintServices(uuid);
		}
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
		RemoteInfo info = remotes().get(uuid);
		response.setContentType("text/html");
		if (info != null && info.isKilled()) {
			response.getWriter().print("1");
		} else {
			response.getWriter().print("0");
		}
	}
	
	/**
	 * Registers computer name and clear its print services.
	 * @param uuid Unique computer identification.
	 * @param request Originating request.
	 * @param response Destination response.
	 * @throws ServletException
	 * @throws IOException
	 */
	private void registerComputerNameCommand(String uuid, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		notifyRemote(uuid, request);
		RemoteInfo info = remotes().get(uuid);
		info.setServices(null);
		info.setComputerName(request.getParameter(ParameterKeys.COMPUTER_NAME));
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
		RemoteInfo info = remotes().get(uuid);
		if (info != null) {
			String serviceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
			String categoriesString = request.getParameter(ParameterKeys.PRINT_SERVICE_CATEGORIES);
			RemotePrintService remotePrintService = new RemotePrintService();
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteName(serviceName);
			remotePrintService.setRemoteComputerName(info.getComputerName());
			if (!Is.emptyString(categoriesString)) {
				for (String categoryLine : categoriesString.split(ParameterKeys.CATEGORIES_SEPARATOR)) {
					String categoryName = categoryLine.substring(0, categoryLine.indexOf(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER));
					String attributes = categoryLine.substring(categoryLine.indexOf(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER) + 1);
					try {
						remotePrintService.getRemoteCategories().add(Class.forName(categoryName));
						if (!Is.emptyString(attributes)) {
							String[] attributeValues = attributes.split(ParameterKeys.ATTRIBUTES_SEPARATOR);
							if (attributeValues.length > 0) {
								List<Attribute> values = new ArrayList<Attribute>(); 
								for (String attributeValue : attributeValues) {
									try {
										ByteArrayInputStream stream = new ByteArrayInputStream(attributeValue.getBytes());
										AttributeInputStream input = new AttributeInputStream(stream);
										Attribute attribute = input.readAttribute();
										if (attribute != null) {
											values.add(attribute);
										}
									} catch (Exception e) {
										LOG.debug(e.getMessage());
									}
								}
								remotePrintService.getRemoteAttributes().put(categoryName, values);
							} else {
								try {
									remotePrintService.getRemoteAttributes().put(categoryName, 
											(Attribute)Class.forName(attributes).newInstance());
								} catch (InstantiationException e) {
									LOG.debug(e.getMessage());
								} catch (IllegalAccessException e) {
									LOG.debug(e.getMessage());
								}
							}
						}
					} catch (ClassNotFoundException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
			getRemoteLookup();
			RemotePrintServiceLookup.registerService(remotePrintService);
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
	 * Returns the serialization of the attributes.
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
		InputStream input = (InputStream)printJob.getPrintObject();
		while (input.available() > 0) {
			output.write(input.read());
		}
		input.close();
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
		if (Is.emptyString(testPageName)) {
			testPageName = "TestPage.pdf";
		}
		String testPage = "org/wubiq/reports/" + testPageName;  
		String printServiceName = request.getParameter(ParameterKeys.PRINT_SERVICE_NAME);
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(testPage);
		boolean toRemote = false;
		if (!Is.emptyString(uuid) 
				&& !Is.emptyString(printServiceName)) {
			toRemote = true;
		}
		if (input != null) {
			if (toRemote) {
				response.setContentType("text/html");				
				IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager();
				IRemotePrintJob remotePrintJob = new PrintJobInputStream(printServiceName, input, null);
				manager.addRemotePrintJob(uuid, remotePrintJob);
				response.getWriter().print(ServerLabels.get("server.test_page_sent", printServiceName));
			} else {
				response.setContentType("application/pdf");				
				OutputStream output = response.getOutputStream();
				while (input.available() > 0) {
					output.write(input.read());
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
	
	public static Map<String, RemoteInfo> remotes() {
		if (remotes == null) {
			remotes = new HashMap<String, RemoteInfo>();
		}
		return remotes;
	}
	
	/**
	 * Notify the remote object about access to it.
	 * @param uuid Unique identifier for the connecting computer.
	 * @param request Originating request.
	 */
	private void notifyRemote(String uuid, HttpServletRequest request) {
		RemoteInfo info = remotes().get(uuid);
		if (info == null) {
			info = new RemoteInfo();
			remotes().put(uuid, info);
		}
		info.setSession(request.getSession(true));
	}

	/**
	 * @return the remoteLookup
	 */
	public static RemotePrintServiceLookup getRemoteLookup() {
		if (remoteLookup == null) {
			remoteLookup = new RemotePrintServiceLookup();
			PrintServiceLookup.registerServiceProvider(remoteLookup);
		}
		return remoteLookup;
	}
}
