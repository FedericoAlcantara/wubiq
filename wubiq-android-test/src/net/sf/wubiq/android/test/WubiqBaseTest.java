/**
 * 
 */
package net.sf.wubiq.android.test;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.android.test.wrappers.LocalManagerTestWrapper;
import net.sf.wubiq.common.CommandKeys;
import net.sf.wubiq.common.ConfigurationKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.utils.Is;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.test.TouchUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * @author Federico Alcantara
 *
 */
public abstract class WubiqBaseTest extends InstrumentationTestCase {
	private final static String TAG = WubiqBaseTest.class.getSimpleName();
	
	protected Instrumentation instrumentation;
	protected LocalManagerTestWrapper manager;
	protected Thread managerThread;
	protected String uuid;
	
	public WubiqBaseTest() {
		uuid = AndroidTestProperties.get(ConfigurationKeys.PROPERTY_UUID, "");
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		instrumentation = getInstrumentation();
		instrumentation.setInTouchMode(true);		
	}

	/**
	 * Gets given page.
	 * @param url Url to load.
	 * @return Page object as a DOM object.
	 * @throws Exception
	 */
	private Document getPage(String url) {
		Document returnValue = null;
		try {
			returnValue = Jsoup.connect(url).get();
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage());
		}
		return returnValue; 
	}
	
	/**
	 * Returns a page which is read from the print test servlet instead of the main servlet.
	 * @param command Command to use.
	 * @return Page object.
	 * @throws Exception
	 */
	protected Document getNewTestPage(String command, String... parameters) {
		Document returnValue = null;
		for (URL url : manager.getUrls()) {
			String address = url.toString().replace("wubiq.do", "wubiq-print-test.do") 
					+ "?"
					+ manager.getEncodedParameters(command, parameters);
			
			returnValue = getPage(address);
			if (returnValue != null) {
				break;
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
	protected Document getWubiqPage(String command, String... parameters) {
		Document returnValue = null;
		for (URL url : manager.getUrls()) {
			String address = url.toString() 
					+ "?"
					+ manager.getEncodedParameters(command, parameters);
			
			returnValue = getPage(address);
			if (returnValue != null) {
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Counts the available print services.
	 * @return Count of print services.
	 * @throws Exception 
	 */
	protected int countPrintServices() {
		return countPrintServices(null);
	}
	/**
	 * Counts the available print services for the client with uuid.
	 * @param uuid Id of the client.
	 * @return Count of print services.
	 * @throws Exception 
	 */
	protected int countPrintServices(String uuid) {
		int returnValue = 0;
		Document page = getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		Element table = page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		for (Element row : table.getElementsByClass(WebKeys.SHOW_SERVICES_ROW_CLASS)) {
			String contents = row.html();
			if (uuid == null ||
					contents.contains(WebKeys.REMOTE_SERVICE_SEPARATOR + uuid)) {
				returnValue++;
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
	protected String findAPrintService(String uuid) {
		String returnValue = null;
		Document page = getNewTestPage(CommandKeys.SHOW_PRINT_SERVICES);
		Element table = page.getElementById(WebKeys.SHOW_SERVICES_TABLE_ID);
		for (Element row : table.getElementsByClass(WebKeys.SHOW_SERVICES_ROW_CLASS)) {
			String contents = row.html();
			if (contents.contains(WebKeys.REMOTE_SERVICE_SEPARATOR + uuid)) {
				returnValue = contents;
				break;
			}
		}
		return returnValue;
	}

	/**
	 * Gets the connections.
	 * @return
	 */
	protected Set<String> getConnections() {
		Set<String> returnValue = new TreeSet<String>();
		String readConnections = AndroidTestProperties.get(ConfigurationKeys.PROPERTY_CONNECTIONS, "");
		if (Is.emptyString(readConnections)) {
			if (manager != null) {
				for (String connection : manager.getConnections()) {
					if (!Is.emptyString(connection)) {
						returnValue.add(connection);
					}
				}
			}
		} else {
			for (String connection : readConnections.split(",;")) {
				if (!Is.emptyString(connection)) {
					returnValue.add(connection);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Set text to the given value.
	 * @param field Field Field to set the text.
	 * @param text Text to use.
	 */
	protected void setText(EditText field, String text) {
		clearField(field);
		typeText(text);
	}

	/**
	 * Clears field value.
	 * @param field Field to clear.
	 */
	protected void clearField(EditText field) {
		field.setSelectAllOnFocus(true);
		TouchUtils.clickView(this, field);
		sendKeys(KeyEvent.KEYCODE_DEL);
	}
	
	/**
	 * Sets the box to a given state.
	 * @param box Box field.
	 * @param status Status to set.
	 */
	protected void setChecked(CheckBox box, boolean status) {
		if ((status && !box.isChecked())
				|| (!status && box.isChecked())) {
			TouchUtils.clickView(this, box);
		}
	}

	
	/**
	 * Types text on current field.
	 * @param text Text to type.
	 */
	protected void typeText(String text) {
		instrumentation.sendStringSync(text);
	}
	
	/**
	 * Pauses for a given number of seconds.
	 * @param seconds Seconds.
	 */
	protected void pause(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			Log.v(TAG, e.getMessage());
		}
	}
}
