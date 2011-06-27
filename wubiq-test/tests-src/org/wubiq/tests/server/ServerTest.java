/**
 * 
 */
package org.wubiq.tests.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.wubiq.common.CommandKeys;
import org.wubiq.common.ParameterKeys;
import org.wubiq.common.WebKeys;
import org.wubiq.fortests.ClientManagerTestWrapper;
import org.wubiq.tests.WubiqBaseTest;
import org.wubiq.utils.ClientProperties;
import org.wubiq.utils.ServerLabels;

import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

/**
 * @author Federico Alcantara
 *
 */
public class ServerTest extends WubiqBaseTest {
	private ClientManagerTestWrapper manager;
	
	public ServerTest(String nameTest) {
		super(nameTest);
	}

	public void setUp() throws Exception {
		super.setUp();
		manager = new ClientManagerTestWrapper();
		manager.setHost(ClientProperties.getHost());
		manager.setPort(ClientProperties.getPort());
		manager.setApplicationName(ClientProperties.getApplicationName());
		manager.setServletName(ClientProperties.getServletName());
		manager.setUuid(ClientProperties.getUuid());
	}
	
	/**
	 * 
	 */
	public void tearDown() throws Exception {
		super.tearDown();
		manager.askServer(CommandKeys.KILL_MANAGER);
	}

	/**
	 * Tests the canConnect command.
	 * @throws Exception
	 */
	public void testCanConnect() throws Exception {
		String value = manager.askServer(CommandKeys.CAN_CONNECT);
		assertEquals("Should be 1", "1", value);
		//Let's test the actual method in the client.
		assertFalse("canConnect() should return false, because a connection was established", manager.canConnect());
		// Kill it
		manager.askServer(CommandKeys.KILL_MANAGER);
		// now it should return true;
		assertTrue("canConnect() should return true", manager.canConnect());
		assertFalse("canConnect() should return false, because a connection was established", manager.canConnect());
	}
	
	/**
	 * Tests if local printServices are registered correctly.
	 * @throws Exception
	 */
	public void testRegisterPrintServices() throws Exception {
		String value = manager.askServer(CommandKeys.CAN_CONNECT);
		assertEquals("Should be 1", "1", value);
		manager.askServer(CommandKeys.SHOW_PRINT_SERVICES);
		Object pageObject = manager.getPage();
		assertTrue("Must be instance of HtmlPage", (pageObject instanceof HtmlPage));
		HtmlPage page = (HtmlPage)pageObject;
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int originalRowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewPage(manager, CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int newRowCount = ((originalRowCount - 1) * 2) + 1;
		assertEquals("Should be the double of print services registered", newRowCount, table.getRowCount());
		// Checks if it unregisters.
		manager.killManager();
		page = (HtmlPage)getNewPage(manager, CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		assertEquals("Should be no remote print services registered", originalRowCount, table.getRowCount());
	}
	
	public void testPrintTestPage() throws Exception {
		UnexpectedPage page = (UnexpectedPage)getNewPage(manager, CommandKeys.PRINT_TEST_PAGE);
		assertEquals("Content type should be application.pdf", "application/pdf", page.getWebResponse().getContentType());
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		InputStream input = page.getInputStream();
		while (input.available() > 0) {
			stream.write(input.read());
		}
		stream.close();
		assertTrue("Size should be bigger than 20k and less than 25k", stream.size() > 20000 && stream.size() < 30000);
	}

	public void testRemotePrintTestPage() throws Exception{
		HtmlPage page = (HtmlPage)getNewPage(manager, CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int rowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewPage(manager, CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		assertTrue("Should be at least another remote service", rowCount < table.getRowCount());
		HtmlTableCell cell = table.getCellAt(rowCount, 0);
		String cellValue = cell.asText();
		StringBuffer buffer = new StringBuffer("")
				.append(ParameterKeys.PRINT_SERVICE_NAME)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(cellValue);
		String response = manager.askServer(CommandKeys.PRINT_TEST_PAGE, buffer.toString());
		assertEquals("Response ", ServerLabels.get("server.test_page_sent", cellValue), response);
	}
	
	/** 
	 * Gets a new page.
	 * @param manager
	 * @param command
	 * @return
	 * @throws Exception
	 */
	private Object getNewPage(ClientManagerTestWrapper manager, String command) throws Exception {
		StringBuffer buffer = new StringBuffer(manager.hostServletUrl())
			.append('?')
			.append(ParameterKeys.COMMAND)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(command);
		return manager.getClient().getPage(buffer.toString());
	}
}
