<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="javax.print.PrintService" %>    
<%@ page import="net.sf.wubiq.common.CommandKeys" %>
<%@ page import="net.sf.wubiq.common.ParameterKeys" %>
<%@ page import="net.sf.wubiq.data.RemoteClient" %>
<%@ page import="net.sf.wubiq.print.services.RemotePrintService" %> 
<%@ page import="net.sf.wubiq.remote.RemoteClientManager" %>
<%@ page import="net.sf.wubiq.utils.PrintServiceUtils" %>    
<%@ page import="net.sf.wubiq.utils.ServerLabels" %>
<%@ page import="net.sf.wubiq.utils.Labels" %>
<script src="js/jquery.js"></script>
<script src="js/deployJava.js"></script>
<script>
	function runServerCommand(url) {
		$.get(url, function(data) {
			$("#output").html($(data).children());
		});
		alert(url);
		location.reload(true);
	};
</script>
<%! 
	private Collection<String[]> getPrintServices(String uuid) {
		Collection<String[]> returnValue = new ArrayList<String[]>();
		for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			String[] printServiceData = new String[3];
			RemotePrintService remotePrintService = null;
			boolean remote = PrintServiceUtils.isRemotePrintService(printService);
			if (remote) {
				remotePrintService = (RemotePrintService)printService;
				if (remotePrintService.getUuid().equals(uuid)) {
					printServiceData[0] = remotePrintService.getRemoteName();
					printServiceData[1] = ServerLabels.get("server.remote_yes");
					printServiceData[2] = remotePrintService.getUuid();
				}
			} else {
				if (uuid.isEmpty()) {
					printServiceData[0] = printService.getName();
					printServiceData[1] = ServerLabels.get("server.remote_no");
					printServiceData[2] = "";
				}
			}
			
			returnValue.add(printServiceData);
		}		
		return returnValue;
	}
	
	private String fixServiceName(String serviceName) {
		String returnValue = serviceName;
		try {
			returnValue = java.net.URLEncoder.encode(serviceName, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			returnValue = serviceName;
		}
		return returnValue;
	}
%>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="css/wubiq.css" />
		<title>Wubiq</title>
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
					<script>
						var dir = location.href.substring(0, location.href.lastIndexOf('/')+1);
					    var url = dir + "wubiq-client-webstart.jsp";
					    deployJava.launchButtonPNG = "<%=ServerLabels.get("server.jws_image") %>";
					    deployJava.createWebStartLaunchButton(url, '1.6.0');	
					</script>
				</td>
				<td>
					<form action="wubiq-client.jar">
						<input type="Submit" value ='<%=ServerLabels.get("server.download_wubiq_client")%>'/>
					</form>
				</td>
			</tr>
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
			%>
			<table class="wubiq_s_table" id="wubiq_service_table_<%=clientCount%>">
				<tr class="wubiq_s_table_tr" >
					<th class="wubiq_s_table_th_title" colspan='<%=remote ? "1" : "3"%>' class="wubiq-client-title"><%=remote ? remoteClient.getComputerName() : ServerLabels.get("server.server_manager")%> </th>
					<%if (remote) { %>
						<th class="wubiq_s_table_th_uuid"><%=uuid%> </th>
						<th class="wubiq_s_table_th_actions">
							<a href="<%=killClient.toString()%>">
								<input type="button" value='<%=ServerLabels.get("server.kill_client")%>'  onclick='<%=killClient%>' />
							</a>
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
								<th class="wubiq_sd_table_th_jobs"><%=ServerLabels.get("server.jobs")%></th>
							</tr>
			
			<%
			int serviceCount = 0;
			for (String[] serviceData : getPrintServices(uuid)) {
				if (serviceData[0] == null) {
					continue;
				}
				String serviceName = !uuid.equals("") ? fixServiceName(serviceData[0]) : serviceData[0];
				String serviceUUID = serviceData[2];
				StringBuffer buffer = new StringBuffer("")
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
							buffer.append('&')
							.append(ParameterKeys.UUID)
							.append(ParameterKeys.PARAMETER_SEPARATOR)
							.append(serviceData[2]);
						}
				StringBuffer pendingJobPage = new StringBuffer("")
						.append("wubiq.do");
						
				%>
							<tr class="wubiq_sd_table_tr">
								<td class="wubiq_sd_table_td_name"><%=serviceData[0]%></td>
								<td class="wubiq_sd_table_td_remote"><%=serviceData[1]%></td>
								<td class="wubiq_sd_table_td_actions">
									<a href="<%=buffer.toString()%>">
										<input style="width:100%" type="button" value='<%=ServerLabels.get("server.print_test_page")%>'
											id='wubiq_testpage_button_<%=serviceCount++%>' />
									</a>
								</td>
								<td class="wubiq_sd_table_td_jobs">
									<%if (serviceUUID != null && !"".equals(serviceUUID)) {%>
										<jsp:include page="<%=pendingJobPage.toString()%>" flush="true">
											<jsp:param name="command" value="<%=CommandKeys.PRINT_SERVICE_PENDING_JOBS%>"/>
											<jsp:param name="printServiceName" value="<%=serviceName%>"/>
											<jsp:param name="uuid" value="<%=serviceUUID %>" />
										</jsp:include>
									<%} else { %>
										&nbsp;
									<%}%>				
								</td>			 
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