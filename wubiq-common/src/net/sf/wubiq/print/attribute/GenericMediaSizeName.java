/**
 * 
 */
package net.sf.wubiq.print.attribute;

import java.util.Arrays;

import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.MediaSizeName;

/**
 * Allow the creation of new MediaSizeName.
 * @author Federico Alcantara
 *
 */
public class GenericMediaSizeName extends MediaSizeName {
	private static final long serialVersionUID = 1L;

	private static String[] stringTable;
	private static EnumSyntax[] enumValueTable;
	
	static {
		GenericMediaSizeName mediaSizeName = new GenericMediaSizeName();
		stringTable = mediaSizeName.getOriginalStringTable();
		enumValueTable = mediaSizeName.getOriginalEnumValueTable();
	}
	
	private GenericMediaSizeName() {
		super(0);
	}
	
	public GenericMediaSizeName(String name) {
		super(stringTable.length);
		stringTable = Arrays.copyOf(stringTable, stringTable.length + 1);
		stringTable[stringTable.length - 1] = name;
		enumValueTable = Arrays.copyOf(enumValueTable, enumValueTable.length + 1);
		enumValueTable[enumValueTable.length -1] = this;
	}

	@Override
	protected String[] getStringTable() {
		return stringTable.clone();
	}
	
	private String[] getOriginalStringTable() {
		return super.getStringTable();
	}
	
	@Override
	protected EnumSyntax[] getEnumValueTable() {
		return enumValueTable.clone();
	}
	
	private EnumSyntax[] getOriginalEnumValueTable() {
		return super.getEnumValueTable();
	}
}
