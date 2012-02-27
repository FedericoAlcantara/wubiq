/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.Paper;
import java.io.Serializable;

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
		imageableX = paper.getImageableX();
		imageableY = paper.getImageableY();
		imageableWidth = paper.getImageableWidth();
		imageableHeight = paper.getImageableHeight();
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
}
