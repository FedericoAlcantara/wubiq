/**
 * 
 */
package net.sf.wubiq.enums;

import net.sf.wubiq.utils.InstallerBundle;


/**
 * Indicates the type of service return.
 * @author Federico Alcantara
 *
 */
public enum ServiceReturnStatus {
	OKEY, NOT_ACCEPTING_COMMANDS, NO_SERVICE, UNKNOWN;
	
	/**
	 * Creates the corresponding label.
	 * @return Label of the status.
	 */
	public String getLabel() {
		return InstallerBundle.getLabel("ServiceReturnStatus." + this.name());
	}
}
