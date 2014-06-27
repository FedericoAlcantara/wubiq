/**
 * 
 */
package net.sf.wubiq.tests.server;

import java.io.InputStream;
import java.net.URL;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.tests.WubiqBaseTest;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.ServerLabels;
import net.sf.wubiq.wrappers.ClientManagerTestWrapper;

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
		manager.initializeDefaults();
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
	public void testKillAndBringAlive() throws Exception {
		manager.bringAlive();
		String value = manager.askServer(CommandKeys.IS_KILLED);
		assertEquals("Should be 0", "0", value);
		//Let's test the actual method in the client.
		assertFalse("canConnect() should return false, because a connection was established", manager.isKilled());
		// Kill it
		manager.askServer(CommandKeys.KILL_MANAGER);
		// now it should return true;
		value = manager.askServer(CommandKeys.IS_KILLED);
		assertEquals("Should be 1", "1", value);
		manager.bringAlive();
		value = manager.askServer(CommandKeys.IS_KILLED);
		assertEquals("Should be 0", "0", value);
	}
	
	/**
	 * Tests if local printServices are registered correctly.
	 * @throws Exception
	 */
	public void testRegisterPrintServices() throws Exception {
		HtmlPage page = (HtmlPage)getNewPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int originalRowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewPage(CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int newRowCount = ((originalRowCount - 1) * 2) + 1;
		assertEquals("Should be the double of print services registered", newRowCount, table.getRowCount());
		// Checks if it unregisters.
		manager.killManager();
		page = (HtmlPage)getNewPage(CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		assertEquals("Should be no remote print services registered", originalRowCount, table.getRowCount());
	}
	
	public void testPrintTestPage() throws Exception {
		UnexpectedPage page = (UnexpectedPage)getNewPage(CommandKeys.PRINT_TEST_PAGE);
		assertEquals("Content type should be application.pdf", "application/pdf", page.getWebResponse().getContentType());
		InputStream input = page.getInputStream();
		checkTestPageSize(input);
	}

	public void testRemotePrintTestPage() throws Exception{
		HtmlPage page = (HtmlPage)getNewPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int rowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewPage(CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		assertTrue("Should be at least another remote service", rowCount < table.getRowCount());
		HtmlTableCell cell = table.getCellAt(rowCount, 0);
		String cellValue = cell.asText();
		if (cellValue.contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
			cellValue = cellValue.substring(0, cellValue.indexOf(WebKeys.REMOTE_SERVICE_SEPARATOR));
		}
		StringBuffer buffer = new StringBuffer("")
				.append(ParameterKeys.PRINT_SERVICE_NAME)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(cellValue);
		int jobCount = manager.getPendingJobs().length;
		String response = manager.askServer(CommandKeys.PRINT_TEST_PAGE, buffer.toString());
		assertTrue("Response ", response != null && response.contains(ServerLabels.get("server.test_page_sent", cellValue)));
		// Validate job count
		int newJobCount = manager.getPendingJobs().length;
		assertTrue("At least one more pending job should have be created", newJobCount > jobCount);
		String[] printJobs = manager.getPendingJobs();
		for (String jobId : printJobs) {
			StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(jobId);
			InputStream input = null;
			String printServiceName = manager.askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter.toString());
			String attributesData = manager.askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter.toString());
			input = (InputStream)manager.pollServer(CommandKeys.READ_PRINT_JOB, parameter.toString());
			assertFalse("Print service name should not be empty", Is.emptyString(printServiceName));
			assertNotNull("Attributes data should not be null", attributesData);
			assertNotNull("Input stream should contain the print test page", input);
			checkTestPageSize(input);
		}
	}
	
	/** 
	 * Gets a new page.
	 * @param manager
	 * @param command
	 * @return
	 * @throws Exception
	 */
	private Object getNewPage(String command) throws Exception {
		for (URL url : manager.getUrls()) {
			String returnValue = url.toString() 
					+ "?"
					+ manager.getEncodedParameters(command);
			
			return getPage(returnValue);
		}
		return null;
	}
	
	/**
	 * Checks the size 
	 * @param input
	 * @throws Exception
	 */
	private void checkTestPageSize(InputStream input) throws Exception {
		int count = 0;
		int value = -1;
		do {
			value = input.read();
			count++;
		} while (value != -1);
	
		assertTrue("Size should be bigger than " + TestClientProperties.get("minimum_file_size", "20000") + " (" + count + ")", 
				count > TestClientProperties.getInt("minimum_file_size", 20000));
	}
}
