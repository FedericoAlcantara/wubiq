/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class PageFormatWrapper extends PageFormat implements Serializable {
	private static final long serialVersionUID = 1L;
	private double width;
	private double height;
	private double imageableX;
	private double imageableY;
	private double imageableWidth;
	private double imageableHeight;
	private double[] matrix;
	private int orientation;
	private PaperWrapper paper;
	
	public PageFormatWrapper() {
	}

	public PageFormatWrapper(PageFormat pageFormat) {
		width = pageFormat.getWidth();
		height = pageFormat.getHeight();
		imageableX = pageFormat.getImageableX();
		imageableY = pageFormat.getImageableY();
		imageableWidth = pageFormat.getImageableWidth();
		imageableHeight = pageFormat.getImageableHeight();
		matrix = pageFormat.getMatrix();
		orientation = pageFormat.getOrientation();
		paper = new PaperWrapper(pageFormat.getPaper());
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @return the imageableX
	 */
	public double getImageableX() {
		return imageableX;
	}

	/**
	 * @return the imageableY
	 */
	public double getImageableY() {
		return imageableY;
	}

	/**
	 * @return the imageableWidth
	 */
	public double getImageableWidth() {
		return imageableWidth;
	}

	/**
	 * @return the imageableHeight
	 */
	public double getImageableHeight() {
		return imageableHeight;
	}

	/**
	 * @return the matrix
	 */
	public double[] getMatrix() {
		return matrix;
	}

	/**
	 * @return the orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/**
	 * @return the paper
	 */
	public PaperWrapper getPaper() {
		return paper;
	}


	/**
	 * @param orientation the orientation to set
	 */
	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	/**
	 * @param paper the paper to set
	 */
	public void setPaper(PaperWrapper paper) {
		this.paper = paper;
	}
	
}
