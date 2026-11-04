/**
 * 
 */
package net.sf.wubiq.clients;

import net.sf.wubiq.utils.Labels;

/**
 * Prints the client version: <code>java -cp wubiq-client.jar net.sf.wubiq.clients.VersionInformation</code>.
 * @author Federico Alcantara
 *
 */
public class VersionInformation {

	/**
	 * @return The version, followed by the build in parentheses when the client runs from a jar built
	 * by the ant script (the latest tag, the commits since it, the commit and -dirty for uncommitted changes).
	 */
	public static String describe() {
		Package clientPackage = VersionInformation.class.getPackage();
		String build = clientPackage == null ? null : clientPackage.getImplementationVersion();
		if (build == null || build.trim().length() == 0) {
			return Labels.VERSION;
		}
		return Labels.VERSION + " (" + build + ")";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(describe());
	}

}
