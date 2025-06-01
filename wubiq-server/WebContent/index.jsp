<%@page import="java.util.TreeMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="net.sf.wubiq.servlets.ServletsStatus"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.text.DateFormat"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.TreeSet"%>

<%@ page import="javax.print.PrintService" %>    

<%@ page import="net.sf.wubiq.common.CommandKeys" %>
<%@ page import="net.sf.wubiq.common.ParameterKeys" %>
<%@ page import="net.sf.wubiq.common.WebKeys"%>
<%@ page import="net.sf.wubiq.data.RemoteClient" %>
<%@ page import="net.sf.wubiq.persistence.PersistenceManager"%>
<%@ page import="net.sf.wubiq.print.jobs.RemotePrintJobStatus"%>
<%@ page import="net.sf.wubiq.print.managers.impl.RemotePrintJobManagerFactory"%>
<%@ page import="net.sf.wubiq.print.managers.IRemotePrintJobManager"%>
<%@ page import="net.sf.wubiq.print.services.RemotePrintService" %> 
<%@ page import="net.sf.wubiq.remote.RemoteClientManager" %>
<%@ page import="net.sf.wubiq.utils.Is"%>
<%@ page import="net.sf.wubiq.utils.Labels" %>
<%@ page import="net.sf.wubiq.utils.PrintServiceUtils" %>    
<%@ page import="net.sf.wubiq.utils.ServerLabels" %>
<%@ page import="net.sf.wubiq.utils.ServerProperties"%>
<%@ page import="net.sf.wubiq.utils.WebUtils"%>

<%! 
	private Collection<String[]> getPrintServices(String uuid, PrintService[] printServices) {
		Collection<String[]> returnValue = new ArrayList<String[]>();
		for (PrintService printService : printServices) {
			String[] printServiceData = new String[6];
			RemotePrintService remotePrintService = null;
			boolean remote = PrintServiceUtils.isRemotePrintService(printService);
			if (remote) {
				remotePrintService = (RemotePrintService)printService;
				if (remotePrintService.getUuid().equals(uuid)) {
					IRemotePrintJobManager manager = RemotePrintJobManagerFactory.getRemotePrintJobManager(uuid);
					String count = "0";
					if (manager != null) {
						count = Integer.toString(manager.getPrintServicePendingJobs(uuid, printService));
					}
					printServiceData[0] = remotePrintService.getRemoteName();
					printServiceData[1] = ServerLabels.get("server.remote_yes");
					printServiceData[2] = remotePrintService.getUuid();
					printServiceData[3] = count;
					printServiceData[4] = Boolean.toString(remotePrintService.isPrinting());
					StringBuilder groups = new StringBuilder();
					for (String group : remotePrintService.getGroups()) {
						if (groups.length() > 0) {
							groups.append(",");
						}
						groups.append(group);
					}
					printServiceData[5] = groups.toString();
				}
			} else {
				if (uuid.isEmpty()) {
					printServiceData[0] = printService.getName();
					printServiceData[1] = ServerLabels.get("server.remote_no");
					printServiceData[2] = "";
					printServiceData[3] = "0";
					printServiceData[4] = "false";
					printServiceData[5] = "";
				}
			}
			
			returnValue.add(printServiceData);
		}		
		return returnValue;
	}
	
%>
<%
PrintService[] printServices = PrintServiceUtils.getPrintServices();
String userId = (String)session.getAttribute(WebKeys.SERVER_USER_ID);
boolean logged =  !Is.emptyString(userId);
String filter = (String)session.getAttribute(WebKeys.SERVER_FILTER);
if (filter == null) {
	filter = "";
} else {
	filter = filter.toLowerCase();
}
String localeLabel = Locale.US.equals(ServerLabels.getLocale()) || ServerLabels.getLocale() == null ? "Espa&#241;ol" : "English";
String ready = ServletsStatus.isReady() ? "" : "paused" ;
%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="css/wubiq.css" />
		<title>Wubiq</title>
		<script src="js/jquery.js"></script>
	</head>
	<body>
	<div align="center" >
		<table class="wubiq_header <%=ready%>">
			<tr>
				<td align="center" colspan="4" ><%=ServerLabels.get("server.version", Labels.VERSION)%>
				</td>
				<td>
					<form action="localeToggle.jsp">
						<input type="Submit" value='<%=localeLabel %>' />
					</form>
				</td>
			</tr>
			<tr>
				<td>
					<form action="https://play.google.com/store/apps/details" method="GET">
						<input type="hidden" name="id" value="net.sf.wubiq.android" />
						<input type="Submit" value ='<%=ServerLabels.get("server.install_wubiq_android")%>'/>
					</form>
				</td>
				<td>
					<form action="wubiq-android.apk">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_wubiq_android")%>'/>
					</form>
				</td>
				<td>
					<form action="wubiq-setup.jar">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_setup")%>'/>
					</form>
				</td>
				<td>
					<form action="wubiq-client.jar">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_wubiq_client")%>'/>
					</form>
				</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
				<td>
				</td>
			</tr>
			<%if (!logged) {
				if (!ServerProperties.INSTANCE.getUsers().isEmpty()) {%>
			<tr>
				<td colspan="4">
						<form action="loginCheck.jsp" method="post" style="font-size: smaller;">
							<%=ServerLabels.get("server.user_id")%>:<input type="text" name="<%=WebKeys.SERVER_USER_ID%>" />
							&nbsp;&nbsp;
							<%=ServerLabels.get("server.user_password") %>:<input type="password" name="<%=WebKeys.SERVER_USER_PASSWORD%>" />
							&nbsp;&nbsp;
							<input type="submit" value="<%=ServerLabels.get("server.log_in") %>" />
						</form>
				</td>
			</tr>
			<%	}
			} else { %>
			<tr>
				<td colspan="4">
					<form action="logout.jsp" method="post">
						<%=ServerLabels.get("server.logged_in_as", userId) %>&nbsp;<input type="submit" value="<%=ServerLabels.get("server.log_out") %>" />
					</form>
				</td>
			</tr>
			<%} %>
			<tr>
				<td colspan="4">
					<form action="filter.jsp" method="post" style="font-size: smaller;">
						<%=ServerLabels.get("server.filter")%>:<input type="text" name="<%=WebKeys.SERVER_FILTER%>" value="<%=filter%>"/>
						<input type="submit" value="<%=ServerLabels.get("server.filter.apply") %>" />
					</form>
				</td>
			</tr>
			<%if (logged && PersistenceManager.isPersistenceEnabled()) {%>
			<tr>
				<td colspan="4">
					<%=ServerLabels.get("server.info_running_on_persistence") %>
				</td>
			</tr>
			<%} %>			
		</table>
		<%
		RemoteClientManager manager = RemoteClientManager.getRemoteClientManager(request);
		String url = request.getContextPath();
		Collection<String>uuids = new TreeSet<String>();
		uuids.add("");
		uuids.addAll(manager.getUuids());
		int clientCount = 0;
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (String uuid: uuids) {
			RemoteClient remoteClient = manager.getRemoteClient(uuid, true);
			if (remoteClient != null && remoteClient.isKilled()) {
				continue;
			}
			boolean remote = remoteClient != null;
			String clientVersion = "";
			String disconnection = "";
			String failures = "";
			String printTestButtonStyle = "";
			String[] firstPrintService = null;
			for (String[] printService : getPrintServices(uuid, printServices)) {
				firstPrintService = printService;
			}
			String groups = "";
			if (firstPrintService != null
					&& !Is.emptyString(firstPrintService[5])) {
				groups = " - " + firstPrintService[5];
			}
			if (remoteClient != null) {
				clientVersion = " (" + remoteClient.getClientVersion() + ") " 
						+ groups 
						+ " - " 
						+ df.format(manager.remoteLastAccessed(uuid));
				disconnection = (!manager.isRemoteActive(uuid)
								? ServerLabels.get("server.client_disconnected")
								: "");
				failures = "";
				
				printTestButtonStyle = !manager.isRemoteActive(uuid)
						? "pointer-events:none; cursor:default; text-decoration:none; color:grey;" : "text-decoration:none";
				
			}
			if (!Is.emptyString(filter)) {
				if (!uuid.toLowerCase().startsWith(filter) || remoteClient == null) {
					continue;
				}
			}
			StringBuffer killClient = new StringBuffer("")
				.append(url)
				.append('/')
				.append("wubiq.do?")
				.append(ParameterKeys.COMMAND)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(CommandKeys.KILL_MANAGER)
				.append('&')
				.append(ParameterKeys.UUID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(uuid);
			StringBuffer pauseResumeClient = new StringBuffer("");
			String pauseResumeLabel = "";
			String pausedClass = "";
			if (remoteClient != null) {
				if (remoteClient.isPaused()) {
					pausedClass = "paused";
					pauseResumeLabel = ServerLabels.get("server.resume_client");
					pauseResumeClient
						.append(url)
						.append('/')
						.append("wubiq.do?")
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(CommandKeys.RESUME_MANAGER)
						.append('&')
						.append(ParameterKeys.UUID)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(uuid);
				} else {
					pauseResumeLabel = ServerLabels.get("server.pause_client");
					pauseResumeClient
						.append(url)
						.append('/')
						.append("wubiq.do?")
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(CommandKeys.PAUSE_MANAGER)
						.append('&')
						.append(ParameterKeys.UUID)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(uuid);
				}
			}
			%>
			<table class="wubiq_s_table <%=pausedClass %>" id="wubiq_service_table_<%=clientCount%>">
				<tr class="wubiq_s_table_tr" >
					<th class="wubiq_s_table_th_title" colspan='<%=remote ? "1" : "3"%>' class="wubiq-client-title"><%=remote ? remoteClient.getComputerName() : ServerLabels.get("server.server_manager")%> </th>
					<%if (remote) { %>
						<th class="wubiq_s_table_th_uuid"><%=uuid%><%=clientVersion%></th>
						<th class="wubiq_s_table_th_actions">
							<%if (logged) {%>
								<a href="<%=killClient.toString()%>">
									<input type="button" value='<%=ServerLabels.get("server.kill_client")%>'  onclick='<%=killClient%>' />
								</a>
								<a href="<%=pauseResumeClient.toString()%>">
									<input type="button" value='<%=pauseResumeLabel%>'  onclick='<%=pauseResumeClient%>' />
								</a>
							<%} %>	
						</th>
					<%if (!Is.emptyString(disconnection)) {%>
					</tr>
						<th>&nbsp;</th>
						<th colspan="2" style="text-align:center; color:red;"><%=disconnection%></th>
					<%}
					if (!Is.emptyString(failures)) {%>
					</tr>
						<th>&nbsp;</th>
						<th colspan="2" style="text-align:center; color:green;"><%=failures%></th>
					<%}
					}%>			 
				</tr>
				<tr class="wubiq_s_table_tr">
					<td class="wubiq_s_table_td" colspan="3">
						<table class="wubiq_sd_table" id="wubiq_service_details_table_<%=clientCount++%>">
							<tr class="wubiq_sd_table_tr">
								<th class="wubiq_sd_table_th_name"><%=ServerLabels.get("server.service_name")%></th>
								<th class="wubiq_sd_table_th_remote"><%=ServerLabels.get("server.remote")%></th>
								<th class="wubiq_sd_table_th_actions"><%=ServerLabels.get("server.actions")%></th>
								<%if (remote) { %>
									<th class="wubiq_sd_table_th_jobs">&nbsp;</th>
									<th class="wubiq_sd_table_th_jobs"><%=ServerLabels.get("server.jobs")%></th>
									<%if (logged) {%>
										<th class="wubiq_sd_table_th_actions"><%=ServerLabels.get("server.jobs.remove_all") %></th>
									<%} %>
								<%} %>
							</tr>
			
			<%
			int serviceCount = 0;
			for (String[] serviceData : getPrintServices(uuid, printServices)) {
				if (serviceData[0] == null) {
					continue;
				}
				String serviceName = WebUtils.INSTANCE.encode(serviceData[0]);
				String serviceUUID = serviceData[2];
				StringBuffer printTestPage = new StringBuffer("")
						.append(url)
						.append('/')
						.append("wubiq-print-test.do?")
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(CommandKeys.PRINT_TEST_PAGE)
						.append('&')
						.append(ParameterKeys.PRINT_SERVICE_NAME)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(serviceName);
						if (!serviceData[2].isEmpty()) {
							printTestPage.append('&')
								.append(ParameterKeys.UUID)
								.append(ParameterKeys.PARAMETER_SEPARATOR)
								.append(serviceData[2]);
						}
				StringBuffer removeAll = new StringBuffer("")
						.append(url)
						.append('/')
						.append("wubiq.do?")
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(CommandKeys.REMOVE_ALL_PRINT_JOBS)
						.append('&')
						.append(ParameterKeys.PRINT_SERVICE_NAME)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(serviceName)
						.append('&')
						.append(ParameterKeys.UUID)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(uuid);
				%>
							<tr class="wubiq_sd_table_tr">
								<td class="wubiq_sd_table_td_name"><%=serviceData[0]%></td>
								<td class="wubiq_sd_table_td_remote"><%=serviceData[1]%></td>
								<td class="wubiq_sd_table_td_actions">
									<a href="<%=printTestPage.toString()%>" style="<%=printTestButtonStyle%>">
										<input style="width:100%; <%=printTestButtonStyle%>" type="button" value='<%=ServerLabels.get("server.print_test_page")%>'
											id='wubiq_testpage_button_<%=serviceCount++%>' />
									</a>
								</td>
								<%if (remote) { %>
									<td class="wubiq_sd_table_td_printing" style="text-align:center">
										<%if ("true".equalsIgnoreCase(serviceData[4])) {%>
											<img src="images/icon_print.gif" height="24px" width="24px" /> 
										<%} else { %>
											&nbsp;
										<%} %>
									</td>
									<td class="wubiq_sd_table_td_jobs" style="text-align:center">
										<%=serviceData[3]%>
									</td>
									<%if (logged) {%>
										<td class="wubiq_sd_table_td_actions">
											<a href="<%=removeAll.toString()%>">
												<input style="width:100%" type="button" value='<%=ServerLabels.get("server.remove_button")%>'
													id='wubiq_remove_all_button_<%=serviceCount++%>' />
											</a>
										</td>
									<%} %>
								<%} %>			 
							</tr>
				<%
			}
			%>
						</table>
					</td>	
				</tr>
			</table>
			<%
		}%>
		</div>
		<div id="output" >
		</div>
	</body>
</html>