/**
 * 
 */
package net.sf.wubiq.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;

/**
 * Handles the communication between clients and server.
 * @author Federico Alcantara
 *
 */
public class PrintTestServlet extends RemotePrintServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uuid = request.getParameter(ParameterKeys.UUID);
		String command = request.getParameter(ParameterKeys.COMMAND);
		if (command.equalsIgnoreCase(CommandKeys.PRINT_TEST_PAGE)) {
			printTestPageCommand(uuid, request, response);
		} else if (command.equalsIgnoreCase(CommandKeys.SHOW_PRINT_SERVICES)) {
			showPrintServicesCommand("", request, response);
		}
	}
			
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
}
