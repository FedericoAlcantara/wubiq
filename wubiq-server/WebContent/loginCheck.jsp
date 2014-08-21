<%@page import="net.sf.wubiq.utils.ServerLabels"%>
<%@page import="net.sf.wubiq.utils.ServerWebUtils"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="net.sf.wubiq.common.WebKeys"%>
<%@page import="net.sf.wubiq.utils.ServerProperties"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<%
		String userId=request.getParameter(WebKeys.SERVER_USER_ID);
		String password=request.getParameter(WebKeys.SERVER_USER_PASSWORD);
		if (ServerProperties.INSTANCE.isValidUser(userId, password)) {
			session.setAttribute(WebKeys.SERVER_USER_ID, userId);
			response.sendRedirect("index.jsp");
		} else {
			PrintWriter writer = response.getWriter();
			writer.println(ServerWebUtils.INSTANCE.backResponse(request, ServerLabels.get("server.invalid_login")));		
			
		}
	%>
</body>
</html>