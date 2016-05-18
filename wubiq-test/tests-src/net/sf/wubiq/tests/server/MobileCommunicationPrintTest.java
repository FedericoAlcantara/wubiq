/**
 * 
 */
package net.sf.wubiq.tests.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.tests.WubiqBaseTest;
import net.sf.wubiq.utils.IOUtils;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.ServerLabels;
import net.sf.wubiq.wrappers.LocalManagerTestWrapper;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Federico Alcantara
 *
 */
public class MobileCommunicationPrintTest extends WubiqBaseTest {

	public MobileCommunicationPrintTest(String nameTest) {
		super(nameTest);
	}
	
	public void setUp() throws Exception {
		super.setUp();
		manager = new LocalManagerTestWrapper(CommandKeys.REGISTER_MOBILE_PRINT_SERVICE);
		manager.initializeDefaults();
		managerThread = new Thread(manager, "RemotePrintTest thread");
		manager.killManager();
		Thread.sleep(2000);
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		manager.killManager();
		Thread.sleep(5000);
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
	
	public void testRemotePrintTestPage() throws Exception {
		// Test as non pageable
		runRemotePrintTestPage("remoteMobileTestPageAsHex.txt", null);
	}

	public void testRemotePrintTestPagePageable() throws Exception {
		// Test as pageable
		runRemotePrintTestPage("remoteMobileTestPageAsHex-pageable.txt", ParameterKeys.PRINT_TEST_DIRECT_PAGEABLE
				+ ParameterKeys.PARAMETER_SEPARATOR
				+ "true");
	}
	
	/**
	 * Tests the functionality of the local print manager, by calling its parts. No thread is started.
	 *
	 * @throws Exception
	 */
	private void runRemotePrintTestPage(String fileName, String addParameter) throws Exception{
		int rowCount = countPrintServices(uuid);
		startManager();
		assertTrue("Should be at least another remote service", rowCount < countPrintServices(uuid));
		String cellValue = findAPrintService(uuid);
		if (cellValue.contains(WebKeys.REMOTE_SERVICE_SEPARATOR)) {
			cellValue = cellValue.substring(0, cellValue.indexOf(WebKeys.REMOTE_SERVICE_SEPARATOR));
		}
		manager.stopProcessing(true);
		StringBuffer buffer = new StringBuffer("")
				.append(ParameterKeys.PRINT_SERVICE_NAME)
				.append(ParameterKeys.PARAMETER_SEPARATOR)
				.append(cellValue);
		int jobCount = manager.getPendingJobs().length;
		String response = ((HtmlPage)getNewTestPage(CommandKeys.PRINT_TEST_PAGE, buffer.toString(), addParameter)).asText();
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
			assertTrue("Content must be of type input stream", content instanceof InputStream);
			String contentHex = convertToHex((InputStream)content);
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + fileName)));
			String textHex = reader.readLine();
			reader.close();
			assertEquals("Content must be valid", textHex, contentHex);
			manager.closePrintJob(jobId);
		}
		manager.stopProcessing(false);
	}
	
	/**
	 * Converts to hex.
	 * @param input Input stream.
	 * @return Hex value.
	 * @throws Exception
	 */
	private String convertToHex(InputStream input) throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IOUtils.INSTANCE.copy(input, stream);
		stream.flush();
		StringBuffer value = new StringBuffer("");
		for (byte byteVal : stream.toByteArray()) {
			value.append(String.format("%02X ", byteVal));
		}
		 
		/*
		// To store the stream properly
		PrintWriter writer = new PrintWriter(new FileWriter());
		writer.println(value.toString());
		writer.close();
		*/
		return value.toString();
	}
}
