/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.Serializable;
import java.util.Comparator;

import net.sf.wubiq.enums.TextAlignmentType;


/**
 * Represents a Text line in a report to be printed using stream. 
 * Positions and width are in points (inch / 72) on the page: x is the left of the text and
 * y is its baseline, measured from the top left corner of the paper.
 * @author Federico Alcantara
 *
 */
public class TextField implements Comparable<TextField>, Comparator<TextField>, Serializable{
	private static final long serialVersionUID = 1L;
	private int page;
	private float x;
	private float y;
	private int width;
	private int fontSize;
	private boolean bold;
	private boolean italic;
	private boolean underline;
	private TextAlignmentType textAlignment;
	private String text;
	public static final float CPI_5 = 5;
	public static final float CPI_6 = 6;
	public static final float CPI_10 = 10;
	public static final float CPI_12 = 12;
	public static final float CPI_15 = 15;
	public static final float CPI_17 = 17.14f;
	public static final float CPI_20 = 20f;
	
	public TextField(int page, float x, float y, int fontSize, String text) {
		this.page = page;
		this.x = x;
		this.y = y;
		this.fontSize = fontSize;
		this.text = text;
		calculateWidth();
	}
	
	public TextField(int page, float x, float y, Font font, String text) {
		this(page, x, y, font.getSize(), text);
		this.setBold(font.isBold());
		this.setItalic(font.isItalic());
		if (font.getAttributes().get(TextAttribute.UNDERLINE) != null &&
				font.getAttributes().get(TextAttribute.UNDERLINE) ==
				TextAttribute.UNDERLINE_ON) {
			this.setUnderline(true);
		}
	}
	
	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
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
	 * @return the fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}
	/**
	 * @param fontSize the fontSize to set
	 */
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		calculateWidth();
	}
	/**
	 * @return the bold
	 */
	public boolean isBold() {
		return bold;
	}
	/**
	 * @param bold the bold to set
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}
	/**
	 * @return the italic
	 */
	public boolean isItalic() {
		return italic;
	}
	/**
	 * @param italic the italic to set
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}
	/**
	 * @return the underline
	 */
	public boolean isUnderline() {
		return underline;
	}
	/**
	 * @param underline the underline to set
	 */
	public void setUnderline(boolean underline) {
		this.underline = underline;
	}

	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public TextAlignmentType getTextAlignment() {
		return textAlignment;
	}
	
	public void setTextAlignment(TextAlignmentType textAlignment) {
		this.textAlignment = textAlignment;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
		calculateWidth();
	}
	
	/**
	 * 
	 * @return Character per inches for the font
	 */
	public float getTextFontCPI() {
		float returnValue = CPI_12;
		if (fontSize <= 6) {
			returnValue = CPI_20;
		} else if (fontSize == 7) {
			returnValue = CPI_17;
		} else if (fontSize <= 9) {
			returnValue = CPI_15;
		} else if (fontSize <= 12) {
			returnValue = CPI_12;
		} else if (fontSize <= 17) {
			returnValue = CPI_10;
		} else if (fontSize < 22) {
			returnValue = CPI_6;
		} else {
			returnValue = CPI_5;
		}
		return returnValue;
	}
		
	private void calculateWidth() {
		float cpi = getTextFontCPI();
		width = Math.round(72f / cpi * getText().length()); // Points the characters take on the paper.
	}
	/**
	 * Orders it in line / position order.
	 */
	public int compareTo(TextField tl2) {
		return compare(this, tl2);
	}
	
	public int compare(TextField tl1, TextField tl2) {
		int returnValue = 0;
		if (tl1.equals(tl2)) {
			returnValue = 0;
		} else {
			if (tl1.page > tl2.page) {
				returnValue = 1;
			} else if (tl1.page < tl2.page) {
				returnValue = -1;
			} else {
				returnValue = Float.compare(tl1.y, tl2.y);
				if (returnValue == 0) {
					returnValue = Float.compare(tl1.x, tl2.x);
				}
			}
		}
		return returnValue;
	}
	@Override
	public String toString() {
		return "TextField [page=" + page + ", x=" + x + ", y=" + y + ", width="
				+ width + ", fontSize=" + fontSize + ", bold=" + bold
				+ ", italic=" + italic + ", underline=" + underline
				+ ", textAlignment=" + textAlignment + ", text=" + text + "]";
	}
	
}
