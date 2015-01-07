<%@page import="net.sf.wubiq.utils.Is"%>
<%@page import="net.sf.wubiq.common.WebKeys"%>
<%@page import="net.sf.wubiq.utils.ServerProperties"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Wubiq - Filter</title>
</head>
<body>
	<% 
	String filter=request.getParameter(WebKeys.SERVER_FILTER);
	if (Is.emptyString(filter)) {
		session.removeAttribute(WebKeys.SERVER_FILTER);
	} else {
		session.setAttribute(WebKeys.SERVER_FILTER, filter);
	}
	response.sendRedirect("index.jsp");
	%>
</body>
</html>