/**
 * 
 */
package net.sf.wubiq.utils;

import java.io.File;
import java.io.IOException;

/**
 * @author Federico Alcantara
 *
 */
public class ATest {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(InstallerUtils.INSTANCE.wubiqClientVersion(new File("/OpenSource/JavaProjects/Wubiq/wubiq-client/dist/wubiq-client.jar")));
	}

}
