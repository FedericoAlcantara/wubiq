/**
 * 
 */
package net.sf.wubiq.tests;

import java.awt.print.Pageable;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.tests.server.TestClientProperties;
import net.sf.wubiq.wrappers.LocalManagerTestWrapper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

/**
 * @author Federico Alcantara
 *
 */
public abstract class WubiqBaseTest extends TestCase {
	protected LocalManagerTestWrapper manager;
	protected Thread managerThread;
	protected String uuid;

	public WubiqBaseTest(String nameTest) {
		super(nameTest);
		uuid = TestClientProperties.INSTANCE.get(ConfigurationKeys.PROPERTY_UUID, "");
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Gets given page.
	 * @param url Url to load.
	 * @return Page object.
	 * @throws Exception
	 */
	protected Object getPage(String url) throws Exception {
		WebClient client = new WebClient();
		return client.getPage(url);
	}
	
	/**
	 * Counts the available print services.
	 * @return Count of print services.
	 * @throws Exception 
	 */
	protected int countPrintServices() throws Exception {
		return countPrintServices(null);
	}
	/**
	 * Counts the available print services for the client with uuid.
	 * @param uuid Id of the client.
	 * @return Count of print services.
	 * @throws Exception 
	 */
	protected int countPrintServices(String uuid) throws Exception {
		int returnValue = 0;
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		for (HtmlTableRow row : table.getRows()) {
			for (HtmlTableCell cell : row.getCells()) {
				String classAttribute = cell.getAttribute("class");
				if (classAttribute != null && classAttribute.contains(WebKeys.SHOW_SERVICES_ROW_CLASS)) {
					String cellContents = cell.asText();
					if (uuid == null ||
							cellContents.contains("@" + uuid)) {
						returnValue++;
					}
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Finds first print service belonging to client.
	 * @param uuid UUID of the print service.
	 * @return Service found or null.
	 * @throws Exception
	 */
	protected String findAPrintService(String uuid) throws Exception {
		String returnValue = null;
		HtmlPage page = (HtmlPage)getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		HtmlTable table = (HtmlTable) page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		outer:
		for (HtmlTableRow row : table.getRows()) {
			for (HtmlTableCell cell : row.getCells()) {
				String classAttribute = cell.getAttribute("class");
				if (classAttribute != null && classAttribute.contains(WebKeys.SHOW_SERVICES_ROW_CLASS)) {
					String cellContents = cell.asText();
					if (cellContents.contains("@" + uuid)) {
						returnValue = cellContents;
						break outer;
					}
				}
			}
		}
		return returnValue;
	}

	/**
	 * Returns a page which is read from the print test servlet instead of the main servlet.
	 * @param command Command to use.
	 * @return Page object.
	 * @throws Exception
	 */
	protected Object getNewTestPage(String command, String... parameters) throws Exception {
		for (URL url : manager.getUrls()) {
			String returnValue = url.toString().replace("wubiq.do", "wubiq-print-test.do") 
					+ "?"
					+ manager.getEncodedParameters(command, parameters);
			
			return getPage(returnValue);
		}
		return null;
	}
	
	/**
	 * Returns a page which is read from the print test servlet instead of the main servlet.
	 * @param command Command to use.
	 * @return Page object.
	 * @throws Exception
	 */
	protected Object getWubiqPage(String command, String... parameters) throws Exception {
		for (URL url : manager.getUrls()) {
			String returnValue = url.toString() 
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
	protected void checkTestPageSize(Object content) throws Exception {
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

	/**
	 * Properly starts the manager.
	 * @throws Exception
	 */
	protected void startManager() throws Exception {
		managerThread.start();
		Thread.sleep(3000);
	}

}
