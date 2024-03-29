/**
 * 
 */
package net.sf.wubiq.servlets;

import net.sf.wubiq.common.PropertyKeys;

/**
 * Indicates the state of the servlet.
 * @author Federico Alcantara
 *
 */
public class ServletsStatus {
	private static boolean ready = false;
	
	public static boolean isReady(){
		return ready;
	}
	
	public static void setReady() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				long time = 30000;
				if ("true".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_DEVELOPMENT_MODE))) {
					time = 10;
				}
				try {
					Thread.sleep(time); // Sleep for 30 seconds
					ready = true;
				} catch (InterruptedException e) {
				} 
			}
		});
		thread.start();
	}
}
