/**
 * 
 */
package net.sf.wubiq.print.attribute;

import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

/**
 * Represents custom media size. Java implementations are known for not 
 * allowing landscape media size definition. This class can be used for
 * creating any custom media size including landscape ones.
 * 
 * There are no accessible constructor, any instance must be created using the
 * static method createCustomMediaSize.
 * 
 * @author Federico Alcantara
 *
 */
public class CustomMediaSize extends MediaSize {
	private static final long serialVersionUID = 1L;
	
	private static int mediaSizeNameIndex = 100;

	private boolean reverted;

	/**
	 * Creates a custom media size. If necessary it also creates a net.sf.wubiq.print.attribute.CustomMediaSizeName
	 * and associates it with this instance.
	 * @param width Width. Can be larger than height.
	 * @param height Height. Can be smaller than width.
	 * @param units Units to use either javax.print.attribute.standard.MediaSize.INCH or .MM.
	 * @return A previously created custom media size or a newly created one.
	 */
	public static CustomMediaSize createCustomMediaSize(float width, float height, int units) {
		String mediaName = width + "x" + height + (MediaSize.INCH == units ? " inch" : " mm");
		CustomMediaSizeName media = new CustomMediaSizeName(mediaSizeNameIndex++, mediaName, width, height, units);
		if (width > height) {
			return new CustomMediaSize(height, width, units, media, true);
		} else {
			return new CustomMediaSize(width, height, units, media, false);
		}
	}
	
	/**
	 * Constructor for the media size.
	 * @param width Width. It MUST be smaller or equal than the height.
	 * @param height Height. It MUST be larger or equal than the width.
	 * @param units Units for the size values. Can be javax.print.attribute.standard.MediaSize.INCH or .MM
	 * @param media Associated media size name.
	 * @param reverted True if this media size is intended to represent a landscape media size.
	 */
	private CustomMediaSize(float width, float height, int units, MediaSizeName media, boolean reverted) {
		super(width, height, units, media);
		this.reverted = reverted;
	}

	/**
	 * It can return either the constructed width or height according to the state of the reverted property. 
	 * @see javax.print.attribute.Size2DSyntax#getX(int)
	 */
	@Override
	public float getX(int units) {
		if (reverted) {
			return super.getY(units);
		} else {
			return super.getX(units);
		}
	}
	
	/**
	 * It can return either the constructed width or height according to the state of the reverted property. 
	 * @see javax.print.attribute.Size2DSyntax#getY(int)
	 */
	@Override
	public float getY(int units) {
		if (reverted) {
			return super.getX(units);
		} else {
			return super.getY(units);
		}
	}
	
	/**
	 * It can return either the constructed width or height according to the state of the reverted property. 
	 * @see javax.print.attribute.Size2DSyntax#getXMicrometers()
	 */
	@Override
	protected int getXMicrometers() {
		if (reverted) {
			return super.getYMicrometers();
		} else {
			return super.getXMicrometers();
		}
	}
	
	/**
	 * It can return either the constructed width or height according to the state of the reverted property. 
	 * @see javax.print.attribute.Size2DSyntax#getYMicrometers()
	 */
	@Override
	protected int getYMicrometers() {
		if (reverted) {
			return super.getXMicrometers();
		} else {
			return super.getYMicrometers();
		}
	}
}
