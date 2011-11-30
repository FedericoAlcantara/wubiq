package net.sf.wubiq.android.test;

import net.sf.wubiq.android.PrintManagerService;
import android.content.Intent;
import android.test.ServiceTestCase;

/**
 * Tests the connection of the print service.
 * You must create a file named wubiq-client.properties in the src folder (no package)
 * and set host and port properties to the server running wubiq. Remember that this
 * test will be run on an Android device or emulator.
 * If you don't provide a wubiq-client.properties with host and port it will attempt
 * to connect to http://localhost:8080 that most probably fail.
 * 
 * @author Federico Alcantara
 *
 */
public class PrintServiceTest extends ServiceTestCase<PrintManagerService> {
	PrintManagerService service;

	public PrintServiceTest() {
		super(PrintManagerService.class);
	}

	public void testConnection() throws Exception {
		Intent intent = new Intent(getContext(), PrintManagerService.class);
		startService(intent);
		service = getService();
		assertTrue("Must be true", service.checkPrintManagerStatus());
		assertEquals("Should be 0", "0", service.checkKilledStatus());
	}

}
