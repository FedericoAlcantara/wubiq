package net.sf.wubiq.dotmatrix.witness;

import java.util.Locale;

/**
 * One character cell laid down by the virtual dot-matrix printer.
 * Positions are absolute from the top of form of the first sheet, in inches.
 * @author Federico Alcantara
 *
 */
public class Glyph {
	private final int page;
	private final double x;
	private final double y;
	private final double width;
	private final double cpi;
	private final char character;
	private final boolean bold;
	private final boolean italic;
	private final boolean underline;
	private final boolean doubleWidth;

	public Glyph(int page, double x, double y, double width, double cpi, char character,
			boolean bold, boolean italic, boolean underline, boolean doubleWidth) {
		this.page = page;
		this.x = x;
		this.y = y;
		this.width = width;
		this.cpi = cpi;
		this.character = character;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.doubleWidth = doubleWidth;
	}

	/**
	 * @return Zero based physical sheet index.
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @return Left edge of the cell, in inches from the left edge of the paper.
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return Print head (top of the cell) position, in inches from the top of the sheet.
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return Cell width in inches.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @return Effective characters per inch (condensed and double width applied).
	 */
	public double getCpi() {
		return cpi;
	}

	/**
	 * @return Pitch for display: "10", "12", "17.14".
	 */
	public String getPitchLabel() {
		double rounded = Math.round(cpi * 100) / 100d;
		if (Math.abs(rounded - Math.round(rounded)) < 0.005) {
			return String.valueOf(Math.round(rounded));
		}
		return String.format(Locale.US, "%.2f", rounded);
	}

	public char getCharacter() {
		return character;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public boolean isUnderline() {
		return underline;
	}

	public boolean isDoubleWidth() {
		return doubleWidth;
	}

	@Override
	public String toString() {
		return "Glyph [page=" + page + ", x=" + x + ", y=" + y + ", cpi=" + cpi
				+ ", char='" + character + "']";
	}
}
