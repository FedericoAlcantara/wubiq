/**
 * 
 */
package net.sf.wubiq.servlets;

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
				try {
					Thread.sleep(30000); // Sleep for 30 seconds
					ready = true;
				} catch (InterruptedException e) {
				} 
			}
		});
		thread.start();
	}
}
