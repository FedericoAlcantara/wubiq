<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>

<%@ page import="javax.print.PrintService" %>    

<%@ page import="net.sf.wubiq.common.CommandKeys" %>
<%@ page import="net.sf.wubiq.common.ParameterKeys" %>
<%@ page import="net.sf.wubiq.common.WebKeys"%>
<%@ page import="net.sf.wubiq.data.RemoteClient" %>
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
	private Collection<String[]> getPrintServices(String uuid) {
		Collection<String[]> returnValue = new ArrayList<String[]>();
		for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			String[] printServiceData = new String[4];
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
				}
			} else {
				if (uuid.isEmpty()) {
					printServiceData[0] = printService.getName();
					printServiceData[1] = ServerLabels.get("server.remote_no");
					printServiceData[2] = "";
					printServiceData[3] = "0";
				}
			}
			
			returnValue.add(printServiceData);
		}		
		return returnValue;
	}
	
%>
<%
String userId = (String)session.getAttribute(WebKeys.SERVER_USER_ID);
boolean logged =  !Is.emptyString(userId);
%>  
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="css/wubiq.css" />
		<title>Wubiq</title>
		<script src="js/jquery.js"></script>
		<script src="js/deployJava.js"></script>
		<script>
			window.onload=function() {
				var div = document.getElementById("wubiq-setup-hidden");
				var a = div.getElementsByTagName("a")[0];
				var setup = document.getElementById("wubiq-setup-action");
				setup.action = a.href;
			}
		</script>
	</head>
	<body>
	<div align="center">
		<table class="wubiq_header">
			<tr>
				<td align="center" colspan="3" ><%=ServerLabels.get("server.version", Labels.VERSION)%></td>
			</tr>
			<tr>
				<td>
					<form action="wubiq-android.apk">
						<input type="Submit" value ='<%=ServerLabels.get("server.install_wubiq_android")%>'/>
					</form>
				</td>
				<td>
					<div id="wubiq-setup-hidden" style="display:none">
						<script>
						    var url = "/wubiq-server/wubiq-setup.do";
						    deployJava.createWebStartLaunchButton(url, '1.6.0');
						</script>
					</div>
					<form id="wubiq-setup-action" action="">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_setup")%>'/>
					</form>
				</td>
				<td>
					<form action="wubiq-client.jar">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_wubiq_client")%>'/>
					</form>
				</td>
			</tr>
			<%if (!logged) {
				if (!ServerProperties.INSTANCE.getUsers().isEmpty()) {%>
			<tr>
				<td colspan="3">
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
				<td colspan="3">
					<form action="logout.jsp" method="post">
						<%=ServerLabels.get("server.logged_in_as", userId) %>&nbsp;<input type="submit" value="<%=ServerLabels.get("server.log_out") %>" />
					</form>
				</td>
			</tr>
			<%} %>			
		</table>
		<%
		RemoteClientManager manager = RemoteClientManager.getRemoteClientManager(request);
		String url = request.getContextPath();
		manager.updateRemotes();
		Collection<String>uuids = new ArrayList<String>();
		uuids.add("");
		uuids.addAll(manager.getUuids());
		int clientCount = 0;
		for (String uuid: uuids) {
			RemoteClient remoteClient = manager.getRemoteClient(uuid);
			if (remoteClient != null && remoteClient.isKilled()) {
				continue;
			}
			boolean remote = remoteClient != null;
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
						<th class="wubiq_s_table_th_uuid"><%=uuid%> </th>
						<th class="wubiq_s_table_th_actions">
							<a href="<%=killClient.toString()%>">
								<input type="button" value='<%=ServerLabels.get("server.kill_client")%>'  onclick='<%=killClient%>' />
							</a>
							<%if (logged) {%>
								<a href="<%=pauseResumeClient.toString()%>">
									<input type="button" value='<%=pauseResumeLabel%>'  onclick='<%=pauseResumeClient%>' />
								</a>
							<%} %>	
						</th>
					<%}%>				 
				</tr>
				<tr class="wubiq_s_table_tr">
					<td class="wubiq_s_table_td" colspan="3">
						<table class="wubiq_sd_table" id="wubiq_service_details_table_<%=clientCount++%>">
							<tr class="wubiq_sd_table_tr">
								<th class="wubiq_sd_table_th_name"><%=ServerLabels.get("server.service_name")%></th>
								<th class="wubiq_sd_table_th_remote"><%=ServerLabels.get("server.remote")%></th>
								<th class="wubiq_sd_table_th_actions"><%=ServerLabels.get("server.actions")%></th>
								<%if (remote) { %>
									<%if (logged) {%>
										<th class="wubiq_sd_table_th_jobs"><%=ServerLabels.get("server.jobs")%></th>
										<th class="wubiq_sd_table_th_actions"><%=ServerLabels.get("server.jobs.remove_all") %></th>
									<%} %>
								<%} %>
							</tr>
			
			<%
			int serviceCount = 0;
			for (String[] serviceData : getPrintServices(uuid)) {
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
									<a href="<%=printTestPage.toString()%>">
										<input style="width:100%" type="button" value='<%=ServerLabels.get("server.print_test_page")%>'
											id='wubiq_testpage_button_<%=serviceCount++%>' />
									</a>
								</td>
								<%if (remote) { %>
									<%if (logged) {%>
										<td class="wubiq_sd_table_td_jobs" style="text-align:center">
											<%=serviceData[3]%>
										</td>
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