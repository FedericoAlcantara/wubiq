/**
 * 
 */
package net.sf.wubiq.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.wubiq.utils.IOUtils;

/**
 * @author Federico Alcantara
 *
 */
public class WubiqSetupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String url = request.getRequestURL().toString();
		url = url.substring(0, url.lastIndexOf('/'));
		StringBuffer output = new StringBuffer("")
			.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
			.append("<jnlp\n")
			.append("\tspec=\"1.6+\"\n")
			.append("\tcodebase=\"")
			.append(url)
			.append("\">\n")
			.append("\t<information>\n")
		    .append("\t\t<title>Wubiq</title>\n")
		    .append("\t\t<vendor>Wubiq</vendor>\n")
		    .append("\t\t<homepage href=\"http://sourceforge.net\"/>\n")
		    .append("\t\t<description>Wubiq Installer</description>\n")
		    .append("\t\t<offline-allowed/>\n") 
		    .append("\t</information>\n")
		    .append("\t<security>\n")
		    .append("\t\t<all-permissions/>\n")
		    .append("\t</security>\n")
		    .append("\t<resources>\n")
		    .append("\t\t<j2se version=\"1.6+\"/>\n")
		    .append("\t\t<jar href=\"wubiq-setup.jar\"/>\n")
		    .append("\t</resources>\n")
		    .append("\t<application-desc main-class=\"com.izforge.izpack.installer.Installer\"/>\n")
		    .append("</jnlp>");
		response.setContentLength(output.toString().getBytes().length);
		response.setContentType("application/x-java-jnlp-file");
		response.setHeader("Content-Disposition", "attachment;filename=wubiq-setup.jnlp");
		IOUtils.INSTANCE.copy(new ByteArrayInputStream(output.toString().getBytes()), response.getOutputStream());
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
