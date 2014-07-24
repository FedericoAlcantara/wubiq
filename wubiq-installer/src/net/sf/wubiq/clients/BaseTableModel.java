/**
 * 
 */
package net.sf.wubiq.clients;

import javax.swing.table.DefaultTableModel;

import net.sf.wubiq.utils.InstallerBundle;

/**
 * Base table model for all defined models.
 * @author Federico Alcantara
 *
 */
public abstract class BaseTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	private String labelPrefix;

	/**
	 * Unique initializer for later localization.
	 * @param labelPrefix
	 */
	public BaseTableModel(String labelPrefix) {
		this.labelPrefix = labelPrefix;
	}
	
	/**
	 * Returns the localized text for row input.
	 * @return Text.
	 */
	public String getTextLabel() {
		return InstallerBundle.getLabel(labelPrefix + ".text");
	}
	
	/**
	 * Returns the localized title for row input.
	 * @return Title.
	 */
	public String getTitleLabel() {
		return InstallerBundle.getLabel(labelPrefix + ".title");
	}

	/**
	 * Removes all rows.
	 */
	public abstract void removeAll();
	
	/**
	 * Cleans the string for ensuring that only valid text is added
	 * @param sentText Text to clean.
	 * @return Parsed text.
	 */
	public abstract String cleanText(String sentText);
	
}
