/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.print.Paper;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author Federico Alcantara
 *
 */
public class PaperWrapper extends Paper implements Serializable {
	private static final long serialVersionUID = 1L;

	private double width;
	private double height;
	private double imageableX;
	private double imageableY;
	private double imageableWidth;
	private double imageableHeight;
	
	public PaperWrapper(){
	}

	public PaperWrapper(Paper paper) {
		width = paper.getWidth();
		height = paper.getHeight();
		Rectangle2D.Double bounds = getImageableBounds(paper);
		imageableX = bounds.x;
		imageableY = bounds.y;
		imageableWidth = bounds.width;
		imageableHeight = bounds.height;
	}
	
	/**
	 * @return the width
	 */
	@Override
	public double getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	@Override
	public double getHeight() {
		return height;
	}

	/**
	 * @return the imageableX
	 */
	@Override
	public double getImageableX() {
		return imageableX;
	}

	/**
	 * @return the imageableY
	 */
	@Override
	public double getImageableY() {
		return imageableY;
	}

	/**
	 * @return the imageableWidth
	 */
	@Override
	public double getImageableWidth() {
		return imageableWidth;
	}

	/**
	 * @return the imageableHeight
	 */
	@Override
	public double getImageableHeight() {
		return imageableHeight;
	}

	@Override
	public void setImageableArea(double x, double y, double width, double height) {
		this.imageableX = x;
		this.imageableY = y;
		this.imageableWidth = width;
		this.imageableHeight = height;
	}
	
	@Override
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}
	
	@SuppressWarnings("rawtypes")
	private Rectangle2D.Double getImageableBounds(Paper paper) {
		Rectangle2D.Double bounds = new Rectangle2D.Double(paper.getImageableX(), paper.getImageableY(), 
				paper.getImageableWidth(), paper.getImageableHeight());
		Field field = null;
		Class paperClass = paper.getClass();
		while (paperClass != null) {
			try {
				field = paper.getClass().getDeclaredField("mImageableArea");
				break;
			} catch (SecurityException e1) {
				// Not implemented here.
			} catch (NoSuchFieldException e1) {
				// Not implemented here.
		 	}
			paperClass = paperClass.getSuperclass();
		}
		if (field != null) {
			try {
				field.setAccessible(true);
				bounds = (Double) field.get(this);
			} catch (SecurityException e) {
				// Not implemented here.
			} catch (IllegalArgumentException e) {
				// Not implemented here
			} catch (IllegalAccessException e) {
				// Not implemented here
			}
		}
		return bounds;
	}
}
