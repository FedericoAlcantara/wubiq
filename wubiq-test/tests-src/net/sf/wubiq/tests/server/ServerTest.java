/**
 * 
 */
package net.sf.wubiq.tests.server;

import java.awt.print.Pageable;
import java.io.InputStream;
import java.net.URL;

import javax.print.DocFlavor;
import javax.print.PrintService;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.tests.WubiqBaseTest;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerLabels;
import net.sf.wubiq.wrappers.LocalManagerTestWrapper;

import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

/**
 * @author Federico Alcantara
 *
 */
public class ServerTest extends WubiqBaseTest {
	private LocalManagerTestWrapper manager;
	
	public ServerTest(String nameTest) {
		super(nameTest);
	}

	public void setUp() throws Exception {
		super.setUp();
		manager = new LocalManagerTestWrapper();
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
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int originalRowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int newRowCount = ((originalRowCount - 1) * 2) + 1;
		assertEquals("Should be the double of print services registered", newRowCount, table.getRowCount());
		// Checks if it unregisters.
		manager.killManager();
		page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		assertEquals("Should be no remote print services registered", originalRowCount, table.getRowCount());
	}
	
	public void testPrintTestPage() throws Exception {
		UnexpectedPage page = (UnexpectedPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE);
		assertEquals("Content type should be application.pdf", "application/pdf", page.getWebResponse().getContentType());
		InputStream input = page.getInputStream();
		checkTestPageSize(input);
	}

	/**
	 * Tests the functionality of the local print manager, by calling its parts. No thread is started.
	 *
	 * @throws Exception
	 */
	public void testRemotePrintTestPage() throws Exception{
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		int rowCount = table.getRowCount();
		manager.registerPrintServices();
		page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
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
		String response = ((HtmlPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE, buffer.toString())).asText();
		assertTrue("Response ", response != null && response.contains(ServerLabels.get("server.test_page_sent", cellValue)));
		// Validate job count
		int newJobCount = manager.getPendingJobs().length;
		assertTrue("At least one more pending job should have be created", newJobCount > jobCount);
		String[] printJobs = manager.getPendingJobs();
		for (String jobId : printJobs) {
			StringBuffer parameter = new StringBuffer(ParameterKeys.PRINT_JOB_ID)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(jobId);
			Object content = null;
			String printServiceName = manager.askServer(CommandKeys.READ_PRINT_SERVICE_NAME, parameter.toString());
			String attributesData = manager.askServer(CommandKeys.READ_PRINT_REQUEST_ATTRIBUTES, parameter.toString());
			content = manager.pollServer(CommandKeys.READ_PRINT_JOB, parameter.toString());
			assertFalse("Print service name should not be empty", Is.emptyString(printServiceName));
			assertNotNull("Attributes data should not be null", attributesData);
			assertNotNull("Content should contain the print test page", content);
			checkTestPageSize(content);
		}
	}
	
	/**
	 * Test the local and direct print managers, by letting them start as a thread as it is 
	 * normally used. Validates that the direct print implementation works as expected.
	 * @throws Exception
	 */
	public void testRemotePrintTestPageDirect() throws Exception {
		Thread thread = new Thread(manager, "TestLocalPrintManager");
		thread.start();
		int timeout = 10;
		while (!manager.getTestData().isRegisteredServices() &&
				timeout-- > 0) {
			Thread.yield();
			Thread.sleep(1000);
		}
		assertTrue("Couldn't register services", timeout > 0);
		assertTrue("Printers must be registered", manager.getTestData().isRegisteredServices());
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		PrintService printService = null;
		String printServiceName = null;
		int rowCount = table.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			String serviceName = table.getCellAt(row, 0).asText();
			PrintService printer = manager.getPrintServicesName().get(serviceName);
			if (printer != null) {
				if (PrintServiceUtils.supportDocFlavor(printer, DocFlavor.INPUT_STREAM.PDF)) {
					printService = printer;
					printServiceName = serviceName;
					break;
				}
			}
		}
		assertNotNull("You MUST define at least one PDF capable printer. Try installing a PDF printer", printService);
		assertTrue("It should be at least one printer", manager.getPrintServicesName().size() > 0);
		assertTrue("It should be more printers than remote count", rowCount > manager.getPrintServicesName().size());

		StringBuffer buffer = new StringBuffer("")
				.append(ParameterKeys.PRINT_SERVICE_NAME)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(printServiceName);
		
		String response = ((HtmlPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE, buffer.toString(), 
				ParameterKeys.PRINT_TEST_DIRECT_PAGEABLE + ParameterKeys.PARAMETER_SEPARATOR + "true")).asText();
		assertTrue("Response ", response != null && response.contains(ServerLabels.get("server.test_page_sent", printServiceName)));
		timeout = 20;
		while (!manager.getTestData().isDirectPrintPrintable() &&
				timeout-- > 0) {
			Thread.yield();
			Thread.sleep(1000);
		}
		assertTrue("Must be to direct manager", manager.getTestData().isDirectManagerCalled());
		assertTrue("Must print a pageable", manager.getTestData().isDirectPrintPageable());
		assertFalse("Can't be a printable", manager.getTestData().isDirectPrintPrintable());
		assertTrue("Page count must be at least one", manager.getTestData().getDirectPageableNumberOfPages() >= 1);
		assertTrue("Graphic command must not be empty", manager.getTestData().getDirectPrintableGraphicsCommandCount() > 50);
		assertFalse("Shouldn't be no errors", manager.getTestData().isErrors());
	}
	
	/**
	 * Test the local and direct print managers, by letting them start as a thread as it is 
	 * normally used. Validates that the local printing without conversion is working as expected.
	 * @throws Exception 
	 */
	public void testRemotePrintTestPageNonPageable() throws Exception {
		Thread thread = new Thread(manager, "TestLocalPrintManager");
		thread.start();
		int timeout = 10;
		while (!manager.getTestData().isRegisteredServices() &&
				timeout-- > 0) {
			Thread.yield();
			Thread.sleep(1000);
		}
		assertTrue("Couldn't register services", timeout > 0);
		assertTrue("Printers must be registered", manager.getTestData().isRegisteredServices());
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		PrintService printService = null;
		String printServiceName = null;
		int rowCount = table.getRowCount();
		for (int row = 0; row < rowCount; row++) {
			String serviceName = table.getCellAt(row, 0).asText();
			PrintService printer = manager.getPrintServicesName().get(serviceName);
			if (printer != null) {
				if (PrintServiceUtils.supportDocFlavor(printer, DocFlavor.INPUT_STREAM.PDF)) {
					printService = printer;
					printServiceName = serviceName;
					break;
				}
			}
		}
		assertNotNull("You MUST define at least one PDF capable printer. Try installing a PDF printer", printService);
		assertTrue("It should be at least one printer", manager.getPrintServicesName().size() > 0);
		assertTrue("It should be more printers than remote count", rowCount > manager.getPrintServicesName().size());

		StringBuffer buffer = new StringBuffer("")
				.append(ParameterKeys.PRINT_SERVICE_NAME)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(printServiceName);
		
		String response = ((HtmlPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE, buffer.toString())).asText();
		assertTrue("Response ", response != null && response.contains(ServerLabels.get("server.test_page_sent", printServiceName)));
		timeout = 20;
		while (!manager.getTestData().isLocalManagerCalled() &&
				timeout-- > 0) {
			Thread.yield();
			Thread.sleep(1000);
		}
		assertTrue("Must be local manager", manager.getTestData().isLocalManagerCalled());
		assertTrue("Must be a pdf flavor", DocFlavor.INPUT_STREAM.PDF.equals(manager.getTestData().getLocalDocFlavor()));
		assertFalse("Shouldn't be no errors", manager.getTestData().isErrors());
	}
	
	
	/**
	 * Returns a page which is read from the print test servlet instead of the main servlet.
	 * @param command Command to use.
	 * @return Page object.
	 * @throws Exception
	 */
	private Object getNewTestPage(String command, String... parameters) throws Exception {
		for (URL url : manager.getUrls()) {
			String returnValue = url.toString().replace("wubiq.do", "wubiq-print-test.do") 
					+ "?"
					+ manager.getEncodedParameters(command, parameters);
			
			return getPage(returnValue);
		}
		return null;
	}
	
	/**
	 * Checks the size 
	 * @param input
	 * @throws Exception
	 */
	private void checkTestPageSize(Object content) throws Exception {
		assertNotNull("Content must not be null", content);
		if (content instanceof InputStream) {
			InputStream input = (InputStream) content;
			
			int count = 0;
			int value = -1;
			do {
				value = input.read();
				count++;
			} while (value != -1);
		
			assertTrue("Size should be bigger than " + TestClientProperties.INSTANCE.get("minimum_file_size", "20000") + " (" + count + ")", 
					count > TestClientProperties.INSTANCE.getInt("minimum_file_size", 20000));
		} else if (content instanceof Pageable) {
			Pageable pageable = (Pageable)content;
			assertTrue("Should have at least one page ", pageable.getNumberOfPages() > 0);
		} else if (content instanceof String) {
			assertFalse("Should not return a blank string", "".equals(content));
		} else {
			fail("Unrecognized return value");
		}
	}
}
