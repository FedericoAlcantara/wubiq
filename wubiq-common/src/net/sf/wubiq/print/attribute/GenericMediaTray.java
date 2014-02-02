/**
 * 
 */
package net.sf.wubiq.print.attribute;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.MediaTray;

/**
 * @author Federico Alcantara
 *
 */
public class GenericMediaTray extends MediaTray {
	private static final long serialVersionUID = 1L;

	public GenericMediaTray(int value) {
		super(value);
	}

	@Override
	public String[] getStringTable() {
		return super.getStringTable();
	}
	
	@Override
	public EnumSyntax[] getEnumValueTable() {
		return super.getEnumValueTable();
	}
}
