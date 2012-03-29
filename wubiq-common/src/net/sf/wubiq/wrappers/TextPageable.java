/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.io.Serializable;
import java.util.Set;

/**
 * All values are saved in twips (inch * 1440).
 * @author Federico Alcantara
 *
 */
public class TextPageable implements Serializable, Pageable {
	private static final long serialVersionUID = 1L;
	private int pageWidth;
	private int pageHeight;
	private int top;
	private int left;

	private Set<TextField> textFields;

	public TextPageable(){
		
	}
	
	public TextPageable(Set<TextField> textFields) {
		this.textFields = textFields;
	}
	
	/**
	 * @return the pageWidth
	 */
	public int getPageWidth() {
		return pageWidth;
	}

	/**
	 * @param pageWidth the pageWidth to set
	 */
	public void setPageWidth(int pageWidth) {
		this.pageWidth = pageWidth;
	}

	/**
	 * @return the pageHeight
	 */
	public int getPageHeight() {
		return pageHeight;
	}

	/**
	 * @param pageHeight the pageHeight to set
	 */
	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}

	/**
	 * @return the top
	 */
	public int getTop() {
		return top;
	}

	/**
	 * @param top the top to set
	 */
	public void setTop(int top) {
		this.top = top;
	}

	/**
	 * @return the left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @param left the left to set
	 */
	public void setLeft(int left) {
		this.left = left;
	}

	/**
	 * @return the textFields
	 */
	public Set<TextField> getTextLines() {
		return textFields;
	}

	/**
	 * @param textFields the textFields to set
	 */
	public void setTextLines(Set<TextField> textFields) {
		this.textFields = textFields;
	}

	public int getNumberOfPages() {
		return Pageable.UNKNOWN_NUMBER_OF_PAGES;
	}

	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		PageFormat pageFormat = new PageFormat();
		Paper paper = new Paper();
		float width = pageWidth * 72 / 1440;
		float height = pageHeight * 72 / 1440;
		float leftMargin = left * 72 / 1440;
		float topMargin = top * 72 / 1440;
		paper.setSize(width, height);
		paper.setImageableArea(leftMargin, topMargin, width - leftMargin, height - topMargin);
		pageFormat.setOrientation(PageFormat.PORTRAIT);
		pageFormat.setPaper(paper);
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		Printable printable = new TextPrintable(this);
		return printable;
	}
	
}
