<%@page import="net.sf.wubiq.utils.ServerLabels"%>
<%@page import="net.sf.wubiq.utils.Labels"%>
<%@page import="com.sun.org.apache.xml.internal.utils.LocaleUtility"%>
<%@page import="java.util.Locale"%>
<%@page import="net.sf.wubiq.utils.Is"%>
<%@page import="net.sf.wubiq.common.WebKeys"%>
<%@page import="net.sf.wubiq.utils.ServerProperties"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Wubiq - Toggle Locale</title>
</head>
<body>
	<% 
	Locale locale = ServerLabels.getLocale();
	if (locale == null || locale.equals(Locale.US)) {
		locale = new Locale("es");
	} else {
		locale = Locale.US;
	}
	session.setAttribute(WebKeys.SERVER_LOCALE, locale);
	ServerLabels.setLocale(locale);
	response.sendRedirect("index.jsp");
	%>
</body>
</html>