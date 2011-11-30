package net.sf.wubiq.android.test;

import org.xmlpull.v1.XmlPullParser;

import net.sf.wubiq.android.PrintManagerService;
import net.sf.wubiq.android.WubiqActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.test.ServiceTestCase;

/**
 * Tests the connection of the print service.
 * You must create a file named wubiq_test_values.xml in the res/xml folder 
 * of the wubiq-android project.
 * In that xml create host and port properties to the server running wubiq. 
 * Remember that this test will be run on an Android device or emulator.
 * If you don't provide a wubiq-client.properties with host and port it will attempt
 * to connect to http://localhost:8080 that most probably fail.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintServiceTest extends ServiceTestCase<PrintManagerService> {
	PrintManagerService service;
	Context context;
	
	public PrintServiceTest() {
		super(PrintManagerService.class);
	}
	@Override
	protected void setUp() throws Exception {
		context = getContext();
		super.setUp();
	}

	public void testConnection() throws Exception {
		initializeWithProperties();
		Intent intent = new Intent(getContext(), PrintManagerService.class);
		startService(intent);
		service = getService();
		assertTrue("Must be true", service.checkPrintManagerStatus());
		assertEquals("Should be 0", "0", service.checkKilledStatus());
	}

    /**
     * For testing purposes.
     * @throws Exception
     */
    public void initializeWithProperties() throws Exception {
    	XmlResourceParser parser  = getContext().getResources().getXml(net.sf.wubiq.android.R.xml.wubiq_test_values);
    	parser.next();
    	String host = null;
    	String port = null;
    	while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
    		if (parser.getEventType() == XmlPullParser.START_TAG) {
    			String nameValue = parser.getAttributeValue(null, "name");
    			if (nameValue != null) {
	    			if (nameValue.equals("host")) {
	    				parser.next();
		        		host = parser.getText();
	    			} else if (nameValue.equals("port")) {
	    				parser.next();
		        		port = parser.getText();
	    			}
    			}
    		}
    		parser.next();
    	}
		Editor editor = getContext().getSharedPreferences(WubiqActivity.PREFERENCES, Activity.MODE_PRIVATE).edit();
		editor.putString(WubiqActivity.HOST_KEY, host);
		editor.putString(WubiqActivity.PORT_KEY, port);
		editor.commit();
    }

}
