/**
 * 
 */
package net.sf.wubiq.tests.server;

import java.io.InputStream;

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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

/**
 * @author Federico Alcantara
 *
 */
public class ServerTest extends WubiqBaseTest {

	public ServerTest(String nameTest) {
		super(nameTest);
	}

	public void setUp() throws Exception {
		super.setUp();
		manager = new LocalManagerTestWrapper();
		manager.initializeDefaults();
		managerThread = new Thread(manager, "ServerTest thread");
		manager.killManager();
		Thread.sleep(2000);
	}
	
	/**
	 * 
	 */
	public void tearDown() throws Exception {
		super.tearDown();
		manager.askServer(CommandKeys.KILL_MANAGER);
		Thread.sleep(5000);
	}

	/**
	 * Tests the canConnect command.
	 * @throws Exception
	 */
	public void testKillAndBringAlive() throws Exception {
		startManager();
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
		int originalPrintServicesCount = countPrintServices(uuid);
		assertTrue("Should be zero", originalPrintServicesCount == 0);
		startManager();
		int actualPrintServicesCount = countPrintServices(uuid);
		assertTrue("Should not be zero", actualPrintServicesCount > 0);
		// Checks if it unregisters.
		manager.killManager();
		actualPrintServicesCount = countPrintServices(uuid);
		assertEquals("Should be no remote print services registered", originalPrintServicesCount, actualPrintServicesCount);
	}
	
	public void testPrintTestPage() throws Exception {
		UnexpectedPage page = (UnexpectedPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE);
		assertEquals("Content type should be application.pdf", "application/pdf", page.getWebResponse().getContentType());
		InputStream input = page.getInputStream();
		checkTestPageSize(input);
		String[] printJobs = manager.getPendingJobs();
		for (String jobId : printJobs) {
			manager.closePrintJob(jobId);
		}
	}

	/**
	 * Tests the functionality of the local print manager, by calling its parts. No thread is started.
	 *
	 * @throws Exception
	 */
	public void testRemotePrintTestPage() throws Exception{
		startManager();
		assertTrue("Should be at least another remote service", countPrintServices(uuid) > 0);
		String cellValue = findAPrintService(uuid);
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
			assertEquals("Content must be blank as we are using DirectConnect", "", (String)content);
			manager.closePrintJob(jobId);
		}
		
	}
	
	/**
	 * Test the local and direct print managers, by letting them start as a thread as it is 
	 * normally used. Validates that the direct print implementation works as expected.
	 * @throws Exception
	 */
	public void testRemotePrintTestPageDirect() throws Exception {
		startManager();
		int timeout = 10;
		while (!manager.getTestData().isRegisteredServices() &&
				timeout-- > 0) {
			Thread.yield();
			Thread.sleep(1000);
		}
		assertTrue("Couldn't register services", timeout > 0);
		assertTrue("Printers must be registered", manager.getTestData().isRegisteredServices());
		PrintService printService = null;
		String printServiceName = null;
		String serviceName = findAPrintService(uuid);
		PrintService printer = manager.getPrintServicesName().get(serviceName);
		if (printer != null) {
			if (PrintServiceUtils.supportDocFlavor(printer, DocFlavor.INPUT_STREAM.PDF)) {
				printService = printer;
				printServiceName = serviceName;
			}
		}
		assertNotNull("You MUST define at least one PDF capable printer. Try installing a PDF printer", printService);
		assertTrue("It should be at least one printer", manager.getPrintServicesName().size() > 0);

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
		String[] printJobs = manager.getPendingJobs();
		for (String jobId : printJobs) {
			manager.closePrintJob(jobId);
		}
	}
	/**
	 * Test the local and direct print managers, by letting them start as a thread as it is 
	 * normally used. Validates that the local printing without conversion is working as expected.
	 * @throws Exception 
	 */
	public void testRemotePrintTestPageNonPageableDirect() throws Exception {
		manager.getTestData().setForceSerializedBySystem(false);
		remotePrintTestPageNonPageable(false);
	}	

	public void testRemotePrintTestPageNonPageableOldRouting() throws Exception {
		manager.getTestData().setForceSerializedBySystem(true);
		Thread.sleep(5000);
		remotePrintTestPageNonPageable(true);
	}	

	/**
	 * Test the local and direct print managers, by letting them start as a thread as it is 
	 * normally used. Validates that the local printing without conversion is working as expected.
	 * @throws Exception 
	 */
	public void remotePrintTestPageNonPageable(boolean useOldRoutine) throws Exception {
		startManager();
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
			if (serviceName.contains("@")) {
				serviceName = serviceName.substring(0, serviceName.indexOf('@'));
			}
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
		if (useOldRoutine) {
			while (!manager.getTestData().isLocalManagerCalled() &&
					timeout-- > 0) {
				Thread.yield();
				Thread.sleep(1000);
			}
			assertTrue("Must be local manager", manager.getTestData().isLocalManagerCalled());
			assertTrue("Must be a pdf flavor: " + manager.getTestData().getLocalDocFlavor(), DocFlavor.INPUT_STREAM.PDF.equals(manager.getTestData().getLocalDocFlavor()));
		} else {
			while (!manager.getTestData().isDirectManagerCalled() &&
					timeout-- > 0) {
				Thread.yield();
				Thread.sleep(1000);
			}
			assertTrue("Must be direct manager", manager.getTestData().isDirectManagerCalled());
		}
		assertFalse("Shouldn't be no errors", manager.getTestData().isErrors());
		String[] printJobs = manager.getPendingJobs();
		for (String jobId : printJobs) {
			manager.closePrintJob(jobId);
		}
	}
	
	public void testPersistenceCapabilities() throws Exception {
		int rowCount = countPrintServices(uuid);
		startManager();
		assertTrue("Should be at least another remote service", rowCount < countPrintServices(uuid));
		String cellValue = findAPrintService(uuid);
		if (cellValue.contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
			cellValue = cellValue.substring(0, cellValue.indexOf(WebKeys.REMOTE_SERVICE_SEPARATOR));
		}
		manager.stopProcessing(true);
		int jobCount = manager.getPendingJobs().length;
		StringBuffer buffer = new StringBuffer("")
			.append(ParameterKeys.PRINT_SERVICE_NAME)
			.append(ParameterKeys.PARAMETER_SEPARATOR)
			.append(cellValue);
		String response = ((HtmlPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE, buffer.toString())).asText();
		assertTrue("Response ", response != null && response.contains(ServerLabels.get("server.test_page_sent", cellValue)));
		// Validate job count
		int newJobCount = manager.getPendingJobs().length;
		assertTrue("At least one more pending job should have be created", newJobCount > jobCount);
		
		// Delete the in memory from the server (forces the use of the persisted job data) 
		response = ((HtmlPage)getWubiqPage(CommandKeys.CLEAR_IN_MEMORY_JOBS, buffer.toString())).asText();
		assertTrue("Server is not in development mode or does not have persisted queue enabled or is not connected to a database", "ok".equals(response));
		int persistedJobCounts = manager.getPendingJobs().length;
		assertTrue("The pending jobs MUST be maintained", newJobCount == persistedJobCounts);
		
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
			assertEquals("Content must be blank as we are using DirectConnect", "", (String)content);
			manager.closePrintJob(jobId);
		}
		manager.stopProcessing(false);
		
	}
}
