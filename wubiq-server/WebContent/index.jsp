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
<script src="js/jquery.js"></script>
<script src="js/deployJava.js"></script>
<script>
	function runServerCommand(url) {
		$.get(url, function(data) {
			$("#output").html($(data).children());
		});
		//location.reload(true);
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
					returnValue.add(printServiceData);
				}
			} else {
				if (uuid.isEmpty()) {
					printServiceData[0] = printService.getName();
					printServiceData[1] = ServerLabels.get("server.remote_no");
					printServiceData[2] = "";
					returnValue.add(printServiceData);
				}
			}
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
		String protocol = request.getProtocol().substring(0, request.getProtocol().indexOf("/")).toLowerCase();
		String address = request.getLocalName();
		String port = Integer.toString(request.getLocalPort()).trim();
		String context = request.getContextPath().substring(1);
		String host = protocol + "://" + address;
		
		String url = host + ":" + port + "/" + context;
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
			StringBuffer killClient = new StringBuffer("runServerCommand(")
				.append('"')
				.append(url)
				.append('/')
				.append("wubiq.do?")
				.append(ParameterKeys.COMMAND)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(CommandKeys.KILL_MANAGER)
				.append('&')
				.append(ParameterKeys.UUID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(uuid)
				.append('"')
				.append(')');
			%>
			<table class="wubiq_s_table" id="wubiq_service_table_<%=clientCount%>">
				<tr class="wubiq_s_table_tr" >
					<th class="wubiq_s_table_th_title" colspan='<%=remote ? "1" : "3"%>' class="wubiq-client-title"><%=remote ? remoteClient.getComputerName() : ServerLabels.get("server.server_manager")%> </th>
					<%if (remote) { %>
						<th class="wubiq_s_table_th_uuid"><%=uuid%> </th>
						<th class="wubiq_s_table_th_actions"><input type="button" value='<%=ServerLabels.get("server.kill_client")%>'  onclick='<%=killClient%>' /></th>
					<%}%>				 
				</tr>
				<tr class="wubiq_s_table_tr">
					<td class="wubiq_s_table_td" colspan="3">
						<table class="wubiq_sd_table" id="wubiq_service_details_table_<%=clientCount++%>">
							<tr class="wubiq_sd_table_tr">
								<th class="wubiq_sd_table_th_name"><%=ServerLabels.get("server.service_name")%></th>
								<th class="wubiq_sd_table_th_remote"><%=ServerLabels.get("server.remote")%></th>
								<th class="wubiq_sd_table_th_actions"><%=ServerLabels.get("server.actions")%></th>
							</tr>
			
			<%
			int serviceCount = 0;
			for (String[] serviceData : getPrintServices(uuid)) {
				StringBuffer buffer = new StringBuffer("runServerCommand(")
						.append('"')
						.append(url)
						.append('/')
						.append("wubiq-print-test.do?")
						.append(ParameterKeys.COMMAND)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(CommandKeys.PRINT_TEST_PAGE)
						.append('&')
						.append(ParameterKeys.PRINT_SERVICE_NAME)
						.append(ParameterKeys.PARAMETER_SEPARATOR)
						.append(serviceData[0]);
						if (!serviceData[2].isEmpty()) {
							buffer.append('&')
							.append(ParameterKeys.UUID)
							.append(ParameterKeys.PARAMETER_SEPARATOR)
							.append(serviceData[2]);
						}
						buffer.append('"')
						.append(')');
						
				%>
							<tr class="wubiq_sd_table_tr">
								<td class="wubiq_sd_table_td_name"><%=serviceData[0]%></td>
								<td class="wubiq_sd_table_td_remote"><%=serviceData[1]%></td>
								<td class="wubiq_sd_table_td_actions"><input type="button" value='<%=ServerLabels.get("server.print_test_page")%>' 
									onclick='<%=buffer.toString()%>' 
									id='wubiq_testpage_button_<%=serviceCount++%>' /></td> 
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