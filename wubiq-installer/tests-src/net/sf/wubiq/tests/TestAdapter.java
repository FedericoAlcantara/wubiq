/**
 * 
 */
package net.sf.wubiq.tests;

import net.sf.wubiq.clients.WubiqConfigurator;

import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

/**
 * @author Federico Alcantara
 *
 */
public class TestAdapter implements UISpecAdapter {

	/**
	 * @see org.uispec4j.UISpecAdapter#getMainWindow()
	 */
	@Override
	public Window getMainWindow() {
		return WindowInterceptor.run(new Trigger() {

			@Override
			public void run() throws Exception {
				WubiqConfigurator configurator = new WubiqConfigurator("");
				configurator.getFrame().setVisible(true);
			}
			
		});
	}
}
