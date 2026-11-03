package net.sf.wubiq.dotmatrix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The shapes and images of a page, drawn at the density of a dot-matrix printer and cut into the
 * bands of dots an ESC/P bit image prints in one pass of the head.
 * <p>9-pin printers print 8 dots high bands at 120 dots per inch across and 72 down; 24-pin printers
 * print 24 dots high bands at 180 by 180. A pixel is a dot: pixels darker than their place in a
 * 4 by 4 ordered dither pattern are printed, so black prints solid and grays print as a pattern of dots.</p>
 * <p>The page is drawn in points, from the paper's corner: the raster's graphics have the imageable
 * area of the page format as origin and clip.</p>
 * @author Federico Alcantara
 *
 */
public class GraphicsRaster {
	/** Dither thresholds: a pixel is a dot when its brightness is below 16 * threshold + 8. */
	private static final int[][] DITHER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};
	/** Blank columns that end a band's run of dots: the head skips them instead of printing them. */
	private static final int MAXIMUM_BLANK_COLUMNS = 16;

	private final boolean pins24;
	private final BufferedImage image;
	private final PageFormat pageFormat;
	private final List<Graphics2D> graphics = new ArrayList<Graphics2D>();
	private boolean drawn;

	/**
	 * @param pageFormat Page format of the page to draw.
	 * @param pins24 True for the 24-pin density, false for the 9-pin one.
	 */
	public GraphicsRaster(PageFormat pageFormat, boolean pins24) {
		this.pageFormat = pageFormat;
		this.pins24 = pins24;
		this.image = new BufferedImage(
				Math.max((int) Math.ceil(pageFormat.getWidth() * dotsPerInchAcross() / 72), 1),
				Math.max((int) Math.ceil(pageFormat.getHeight() * dotsPerInchDown() / 72), 1),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D paper = image.createGraphics();
		try {
			paper.setColor(Color.WHITE);
			paper.fillRect(0, 0, image.getWidth(), image.getHeight());
		} finally {
			paper.dispose();
		}
	}

	/**
	 * @return Dots per inch across the page: 120 for 9-pin printers, 180 for 24-pin ones.
	 */
	public int dotsPerInchAcross() {
		return pins24 ? 180 : 120;
	}

	/**
	 * @return Dots per inch down the page: 72 for 9-pin printers, 180 for 24-pin ones.
	 */
	public int dotsPerInchDown() {
		return pins24 ? 180 : 72;
	}

	/**
	 * @return Dots one band holds from top to bottom: 8 or 24.
	 */
	public int dotsPerBand() {
		return pins24 ? 24 : 8;
	}

	/**
	 * @return A graphics that draws on the page: in points, with the origin and clip of the imageable
	 * area, black on white as the printer's graphics are. Dispose it with {@link #dispose()}.
	 */
	public Graphics2D createGraphics() {
		Graphics2D returnValue = image.createGraphics();
		returnValue.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		returnValue.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		returnValue.scale(dotsPerInchAcross() / 72d, dotsPerInchDown() / 72d);
		returnValue.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		returnValue.setClip(0, 0, (int) Math.ceil(pageFormat.getImageableWidth()), (int) Math.ceil(pageFormat.getImageableHeight()));
		returnValue.setColor(Color.BLACK);
		returnValue.setBackground(Color.WHITE);
		graphics.add(returnValue);
		return returnValue;
	}

	/**
	 * Records that something was drawn. Pages nothing was drawn on have no bands.
	 */
	public void markDrawn() {
		drawn = true;
	}

	public boolean isDrawn() {
		return drawn;
	}

	/**
	 * Releases the graphics created by {@link #createGraphics()}.
	 */
	public void dispose() {
		for (Graphics2D returnValue : graphics) {
			returnValue.dispose();
		}
		graphics.clear();
	}

	/**
	 * @return The page as drawn, one pixel per dot.
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Cuts the dots of the page into bands. Bands are as high as the head, start at the top of what the
	 * printer prints on, and are in page order; bands and runs of columns with no dot are left out.
	 * @param shiftX Points added to a position across the paper to get the position across the printer, which counts
	 * from the left of what it prints on (the text's origin less the imageable area's x); graphics that end up
	 * at the left of the printer's first column are lost.
	 * @param shiftY Points added to a position down the paper to get the position down the printer.
	 * @return The bands to print; none when nothing was drawn.
	 */
	public List<Band> getBands(float shiftX, float shiftY) {
		List<Band> returnValue = new ArrayList<Band>();
		if (!drawn) {
			return returnValue;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		int dots = dotsPerBand();
		int bytesPerColumn = dots / 8;
		int unit = pins24 ? 3 : 2; // Columns per 1/60 inch, the unit the head is moved across in.
		int offsetX = Math.round(shiftX * dotsPerInchAcross() / 72f); // Paper column + this = printer column
		int offsetY = Math.round(shiftY * dotsPerInchDown() / 72f);
		int[] line = new int[width];
		byte[] columns = new byte[width * bytesPerColumn];
		for (int headRow = 0; headRow - offsetY < height; headRow += dots) {
			int firstRow = headRow - offsetY;
			if (firstRow + dots <= 0) {
				continue;
			}
			java.util.Arrays.fill(columns, (byte) 0);
			boolean any = false;
			for (int dot = 0; dot < dots; dot++) {
				int row = firstRow + dot;
				if (row < 0 || row >= height) {
					continue;
				}
				image.getRGB(0, row, width, 1, line, 0, width);
				for (int column = 0; column < width; column++) {
					if (isDot(line[column], column, row)) {
						columns[column * bytesPerColumn + dot / 8] |= (byte) (0x80 >> (dot % 8));
						any = true;
					}
				}
			}
			if (any) {
				cut(returnValue, columns, width, bytesPerColumn, headRow, offsetX, unit);
			}
		}
		return returnValue;
	}

	/**
	 * Adds the runs of columns with dots of one band.
	 */
	private void cut(List<Band> bands, byte[] columns, int width, int bytesPerColumn, int headRow, int offsetX, int unit) {
		int column = 0;
		while (column < width) {
			while (column < width && blank(columns, column, bytesPerColumn)) {
				column++;
			}
			if (column >= width) {
				break;
			}
			int first = column;
			int last = column;
			int blanks = 0;
			while (column < width && blanks <= MAXIMUM_BLANK_COLUMNS) {
				if (blank(columns, column, bytesPerColumn)) {
					blanks++;
				} else {
					blanks = 0;
					last = column;
				}
				column++;
			}
			// Columns left of the printer's first column are lost; the run starts on a 1/60 inch position.
			int start = Math.max(first + offsetX, 0);
			int skipped = start - (first + offsetX);
			int aligned = start - start % unit;
			int count = last - first + 1 - skipped;
			if (count > 0) {
				int leading = start - aligned;
				byte[] data = new byte[(leading + count) * bytesPerColumn];
				System.arraycopy(columns, (first + skipped) * bytesPerColumn, data, leading * bytesPerColumn, count * bytesPerColumn);
				bands.add(new Band(headRow, aligned, leading + count, data));
			}
		}
	}

	private static boolean blank(byte[] columns, int column, int bytesPerColumn) {
		for (int index = 0; index < bytesPerColumn; index++) {
			if (columns[column * bytesPerColumn + index] != 0) {
				return false;
			}
		}
		return true;
	}

	private static boolean isDot(int rgb, int x, int y) {
		int brightness = (((rgb >> 16) & 0xFF) * 299 + ((rgb >> 8) & 0xFF) * 587 + (rgb & 0xFF) * 114) / 1000;
		return brightness < DITHER[y & 3][x & 3] * 16 + 8;
	}

	/**
	 * A run of dot columns the head prints in one pass.
	 */
	public static class Band {
		private final int row;
		private final int column;
		private final int columns;
		private final byte[] data;

		/**
		 * @param row Dot line of the band's top dot, from the printer's first line.
		 * @param column Dot column of the band's first column, from the printer's first column; a multiple of 1/60 inch.
		 * @param columns Number of columns.
		 * @param data The columns' bytes: for each column, the dots from top to bottom, the top dot being the most significant bit of the first byte.
		 */
		public Band(int row, int column, int columns, byte[] data) {
			this.row = row;
			this.column = column;
			this.columns = columns;
			this.data = data;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		public int getColumns() {
			return columns;
		}

		public byte[] getData() {
			return data;
		}
	}
}
