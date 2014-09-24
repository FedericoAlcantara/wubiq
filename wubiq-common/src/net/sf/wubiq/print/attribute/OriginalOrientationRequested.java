/**
 * 
 */
package net.sf.wubiq.print.attribute;

import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.standard.OrientationRequested;

/**
 * Provides an attribute for saving the original orientation of a print job.
 * Sometimes it is necessary to manipulate the output with a different orientation, but
 * prior to print it must be returned back to the original state.
 * @author Federico Alcantara
 *
 */
public final class OriginalOrientationRequested extends EnumSyntax implements PrintRequestAttribute,
		DocAttribute, PrintJobAttribute {
	private static final long serialVersionUID = 1L;
	
	public static final OriginalOrientationRequested PORTRAIT = 
			new OriginalOrientationRequested(3);

	public static final OriginalOrientationRequested LANDSCAPE = 
			new OriginalOrientationRequested(4);
	
	public static final OriginalOrientationRequested REVERSE_LANDSCAPE = 
			new OriginalOrientationRequested(5);
	
	public static final OriginalOrientationRequested REVERSE_PORTRAIT = 
			new OriginalOrientationRequested(6);

	private static final String[] myStringTable = {
			"portrait",
			"landscape",
			"reverse-landscape",
			"reverse-portrait"
	};
	
	private static final OriginalOrientationRequested[] myEnumValueTable = {
		PORTRAIT,
		LANDSCAPE,
		REVERSE_LANDSCAPE,
		REVERSE_PORTRAIT
	};

	protected OriginalOrientationRequested(int value) {
		super(value);
	}
	
	/**
	 * Determines the equivalent orientation according to the requested value.
	 * @param requested Orientation requested attribute.
	 * @return A OriginalOrientationRequested attribute. Might be null.
	 */
	public static OriginalOrientationRequested set(OrientationRequested requested) {
		OriginalOrientationRequested returnValue = null;
		if (requested != null) {
			if (requested.equals(OrientationRequested.REVERSE_PORTRAIT)) {
				returnValue = REVERSE_PORTRAIT;
			} else if (requested.equals(OrientationRequested.REVERSE_LANDSCAPE)) {
				returnValue = REVERSE_LANDSCAPE;
			} else if (requested.equals(OrientationRequested.LANDSCAPE)) {
				returnValue = LANDSCAPE;
			} else {
				returnValue = PORTRAIT;
			}
		}
		return returnValue;
	}
	
	/**
	 * Gets the equivalent orientation requested from the original orientation requested.
	 * @param requested Original orientation requested.
	 * @return Instance attribute of orientation requested. Might be null.
	 */
	public static OrientationRequested get(OriginalOrientationRequested requested) {
		OrientationRequested returnValue = null;
		if (requested != null) {
			if (requested.equals(OriginalOrientationRequested.REVERSE_PORTRAIT)) {
				returnValue = OrientationRequested.REVERSE_PORTRAIT;
			} else if (requested.equals(OriginalOrientationRequested.REVERSE_LANDSCAPE)) {
				returnValue = OrientationRequested.REVERSE_LANDSCAPE;
			} else if (requested.equals(OriginalOrientationRequested.LANDSCAPE)) {
				returnValue = OrientationRequested.LANDSCAPE;
			} else {
				returnValue = OrientationRequested.PORTRAIT;
			}
		}
		return returnValue;
	}

	/**
	 * @see javax.print.attribute.EnumSyntax#getStringTable()
	 */
	@Override
	protected String[] getStringTable() {
		return myStringTable;
	}
	
	@Override
	protected OriginalOrientationRequested[] getEnumValueTable() {
		return myEnumValueTable;
	}
	
	@Override
	protected int getOffset() {
		return 3;
	}
	
	@Override
	public Class<? extends Attribute> getCategory() {
		return OriginalOrientationRequested.class;
	}

	@Override
	public String getName() {
		return "original-orientation-requested";
	}
}
