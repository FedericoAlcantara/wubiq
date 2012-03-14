/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.Serializable;

/**
 * Wraps a PageFormat object.
 * @author Federico Alcantara
 *
 */
public class PageFormatWrapper extends PageFormat implements Serializable {
	private static final long serialVersionUID = 1L;
	private int orientation;
	private PaperWrapper paper;
	
	public PageFormatWrapper() {
	}

	public PageFormatWrapper(PageFormat pageFormat) {
		orientation = pageFormat.getOrientation();
		Paper originalPaper = pageFormat.getPaper();
		paper = new PaperWrapper(originalPaper);
	}

	/**
	 * @return the width
	 */
	public double getWidth() {
        double width;
        int orientation = getOrientation();

        if (orientation == PORTRAIT) {
            width = paper.getWidth();
        } else {
            width = paper.getHeight();
        }

        return width;	
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
        double height;
        int orientation = getOrientation();

        if (orientation == PORTRAIT) {
            height = paper.getHeight();
        } else {
            height = paper.getWidth();
        }

        return height;
	}

	/**
	 * @return the imageableX
	 */
	public double getImageableX() {
		double x;
		 
		switch (getOrientation()) {
	        case LANDSCAPE:
	            x = paper.getHeight()
	                - (paper.getImageableY() + paper.getImageableHeight());
	            break;

	        case PORTRAIT:
	            x = paper.getImageableX();
	            break;

	        case REVERSE_LANDSCAPE:
	            x = paper.getImageableY();
	            break;

	        default:
	            /* This should never happen since it signifies that the
	             * PageFormat is in an invalid orientation.
	             */
	            throw new InternalError("unrecognized orientation");
		}
	    return x;
	}

	/**
	 * @return the imageableY
	 */
	public double getImageableY() {
        double y;

        switch (getOrientation()) {

        case LANDSCAPE:
            y = paper.getImageableX();
            break;

        case PORTRAIT:
            y = paper.getImageableY();
            break;

        case REVERSE_LANDSCAPE:
            y = paper.getWidth()
                - (paper.getImageableX() + paper.getImageableWidth());
            break;

        default:
            /* This should never happen since it signifies that the
             * PageFormat is in an invalid orientation.
             */
            throw new InternalError("unrecognized orientation");

        }
        return y;

	}

	/**
	 * @return the imageableWidth
	 */
	public double getImageableWidth() {
        double width;

        if (getOrientation() == PORTRAIT) {
            width = paper.getImageableWidth();
        } else {
            width = paper.getImageableHeight();
        }

        return width;		
	}

	/**
	 * @return the imageableHeight
	 */
	public double getImageableHeight() {
        double height;

        if (getOrientation() == PORTRAIT) {
            height = paper.getImageableHeight();
        } else {
            height = paper.getImageableWidth();
        }

        return height;
	}

	/**
	 * @return the matrix
	 */
	public double[] getMatrix() {
		double[] matrix = new double[6];

        switch (orientation) {

        case LANDSCAPE:
            matrix[0] =  0;     matrix[1] = -1;
            matrix[2] =  1;     matrix[3] =  0;
            matrix[4] =  0;     matrix[5] =  paper.getHeight();
            break;

        case PORTRAIT:
            matrix[0] =  1;     matrix[1] =  0;
            matrix[2] =  0;     matrix[3] =  1;
            matrix[4] =  0;     matrix[5] =  0;
            break;

        case REVERSE_LANDSCAPE:
            matrix[0] =  0;                     matrix[1] =  1;
            matrix[2] = -1;                     matrix[3] =  0;
            matrix[4] =  paper.getWidth();     matrix[5] =  0;
            break;

        default:
            throw new IllegalArgumentException();
        }

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
		PaperWrapper returnValue = new PaperWrapper();
		returnValue.setSize(paper.getWidth(), paper.getHeight());
		returnValue.setImageableArea(paper.getImageableX(), paper.getImageableY(), 
				paper.getImageableWidth(), 
				paper.getImageableHeight());
		return returnValue;
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
		this.paper = new PaperWrapper(paper);
	}
	
	@Override
	public Object clone() {
		PageFormatWrapper returnValue = new PageFormatWrapper();
		returnValue.setOrientation(orientation);
		returnValue.setPaper(getPaper());
		return returnValue;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PageFormatWrapper [orientation=" + orientation + ", paper="
				+ paper + "]";
	}
}
