<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="javax.print.PrintService" %>    
<%@ page import="net.sf.wubiq.utils.PrintServiceUtils" %>    
<%@ page import="net.sf.wubiq.print.services.RemotePrintService" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<script src="http://www.java.com/js/deployJava.js"></script>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Wubiq</title>
	</head>
	<body>
		<table>
		<tr>
			<td align = "center" colspan = "2">
				<script>
					var dir = location.href.substring(0, location.href.lastIndexOf('/')+1);
				    var url = dir + "wubiq-client-webstart.jsp";
				    deployJava.createWebStartLaunchButton(url, '1.6.0');	
				</script>
			</td>
		</tr>
		<%for (PrintService printService : PrintServiceUtils.getPrintServices()) {
			%>
			<tr>
				<td><%=printService.getName()%> </td>
				<td><%=printService instanceof RemotePrintService%></td>
			</tr>
		<%			
		}
		%>
	</body>
</html>