package net.sf.wubiq.dotmatrix.witness;

/**
 * One bit-image band laid down by the virtual dot-matrix printer: a run of dot columns
 * printed in a single head pass (ESC K, L, Y, Z, ESC * or ESC ^).
 * @author Federico Alcantara
 *
 */
public class BitImage {
	private final int page;
	private final double x;
	private final double y;
	private final double columnWidth;
	private final double dotPitch;
	private final int dots;
	private final long[] columns;

	/**
	 * @param page Zero based physical sheet index.
	 * @param x Left edge of the first column, inches from the left edge of the paper.
	 * @param y Print head position (top dot), inches from the top of the sheet.
	 * @param columnWidth Horizontal distance between columns, in inches.
	 * @param dotPitch Vertical distance between dots, in inches.
	 * @param dots Dots per column (8, 9, 24 or 48).
	 * @param columns One mask per column; the most significant of the <code>dots</code> bits is the top dot.
	 */
	public BitImage(int page, double x, double y, double columnWidth, double dotPitch, int dots, long[] columns) {
		this.page = page;
		this.x = x;
		this.y = y;
		this.columnWidth = columnWidth;
		this.dotPitch = dotPitch;
		this.dots = dots;
		this.columns = columns;
	}

	public int getPage() {
		return page;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getColumnWidth() {
		return columnWidth;
	}

	public double getDotPitch() {
		return dotPitch;
	}

	public int getDots() {
		return dots;
	}

	public long[] getColumns() {
		return columns;
	}

	/**
	 * @param column Column index.
	 * @param dot Dot index, 0 being the top dot.
	 * @return True if the pin fires there.
	 */
	public boolean isSet(int column, int dot) {
		return (columns[column] & (1L << (dots - 1 - dot))) != 0;
	}

	/**
	 * @return Number of dots fired in this band.
	 */
	public int getDotCount() {
		int count = 0;
		for (long column : columns) {
			count += Long.bitCount(column);
		}
		return count;
	}
}
