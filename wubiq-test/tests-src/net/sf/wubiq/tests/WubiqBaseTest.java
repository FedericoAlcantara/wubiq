/**
 * 
 */
package net.sf.wubiq.tests;

import junit.framework.TestCase;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * @author Federico Alcantara
 *
 */
public abstract class WubiqBaseTest extends TestCase {
	public WubiqBaseTest(String nameTest) {
		super(nameTest);
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
	
}
