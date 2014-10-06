/**
 * 
 */
package net.sf.wubiq.print.attribute;

import javax.print.attribute.standard.MediaSizeName;

/**
 * Represents a custom media size name. They are normally created through the implementation of
 * {@link net.sf.wubiq.print.attribute.CustomMediaSize#createCustomMediaSize(float, float, int)}.
 * A normal media size name is just a marker, no dimensions are assign to the. This implementation,
 * however, does require the size, so that the instance can travel between server and client with the
 * necessary information to reproduce the same media size on both sides.
 * @author Federico Alcantara
 *
 */
public class CustomMediaSizeName extends MediaSizeName {
	private static final long serialVersionUID = 1L;

	private String mediaName;
	private float x;
	private float y;
	private int units;
	
	/**
	 * Construct a media size name.
	 * @param value Index to the media size.
	 * @param mediaName Name to use for identification purposes.
	 * @param x Width.
	 * @param y Height.
	 * @param units Units.
	 */
	public CustomMediaSizeName(int value, String mediaName, float x, float y, int units) {
		super(value);
		this.setMediaName(mediaName);
		this.x = x;
		this.y = y;
		this.setUnits(units);
	}

	/**
	 * Construct a media size name.
	 * @param value Index to the media size.
	 * @param mediaName Name to use for identification purposes.
	 * @param x Width.
	 * @param y Height.
	 * @param units Units.
	 */
	public CustomMediaSizeName(int value, String mediaName, int x, int y, int units) {
		this(value, mediaName, new Float(x), new Float(y), units);
	}

	/**
	 * @return the mediaName
	 */
	public String getMediaName() {
		return mediaName;
	}

	/**
	 * @param mediaName the mediaName to set
	 */
	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return the units
	 */
	public int getUnits() {
		return units;
	}

	/**
	 * @param units the units to set
	 */
	public void setUnits(int units) {
		this.units = units;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mediaName + "(" + super.toString() + ")";
	}
	
}
