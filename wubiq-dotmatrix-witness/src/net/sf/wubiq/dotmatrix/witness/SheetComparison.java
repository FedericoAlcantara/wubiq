package net.sf.wubiq.dotmatrix.witness;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Compares what two jobs put on paper, sheet by sheet: the position and size of the
 * printed area, how many graphics dots coincide, and the text lines. Used to check a
 * job against a known good one (for example the same page printed by another JDK).
 * @author Federico Alcantara
 *
 */
public class SheetComparison {
	private static final double DOT_X = 1d / 120d;
	private static final double DOT_Y = 1d / 72d;

	private double horizontalTolerance = 1d / 60d;
	private double verticalTolerance = 1d / 72d;
	private double minimumDotMatch = 0.9;

	/**
	 * @param horizontalTolerance Largest accepted horizontal shift or width change, in inches (default 1/60).
	 */
	public SheetComparison setHorizontalTolerance(double horizontalTolerance) {
		this.horizontalTolerance = horizontalTolerance;
		return this;
	}

	/**
	 * @param verticalTolerance Largest accepted vertical shift or height change, in inches (default 1/72).
	 */
	public SheetComparison setVerticalTolerance(double verticalTolerance) {
		this.verticalTolerance = verticalTolerance;
		return this;
	}

	/**
	 * @param minimumDotMatch Smallest accepted share of base dots found in the other job once its shift is undone (default 0.9).
	 */
	public SheetComparison setMinimumDotMatch(double minimumDotMatch) {
		this.minimumDotMatch = minimumDotMatch;
		return this;
	}

	/**
	 * @param base The known good job.
	 * @param other The job to check.
	 * @return One item per sheet aspect; failed items name the difference in inches.
	 */
	public CheckResult compare(EscpResult base, EscpResult other) {
		CheckResult check = new CheckResult();
		if (base.getPageCount() == other.getPageCount()) {
			check.pass("sheets", base.getPageCount() + " sheet(s)");
		} else {
			check.fail("sheets", other.getPageCount() + " sheet(s), expected " + base.getPageCount());
		}
		int sheets = Math.min(base.getPageCount(), other.getPageCount());
		for (int page = 0; page < sheets; page++) {
			compareSheet(check, "sheet " + (page + 1), base, other, page);
		}
		return check;
	}

	private void compareSheet(CheckResult check, String sheet, EscpResult base, EscpResult other, int page) {
		Ink baseInk = new Ink(base, page);
		Ink otherInk = new Ink(other, page);
		if (baseInk.isEmpty() && otherInk.isEmpty()) {
			check.pass(sheet, "blank");
			return;
		}
		if (baseInk.isEmpty() || otherInk.isEmpty()) {
			check.fail(sheet, otherInk.isEmpty() ? "nothing printed" : "printed on a sheet that should be blank");
			return;
		}
		double dx = otherInk.minX - baseInk.minX;
		double dy = otherInk.minY - baseInk.minY;
		double dw = otherInk.width() - baseInk.width();
		double dh = otherInk.height() - baseInk.height();
		String area = String.format(Locale.US, "printed area at (%.3f, %.3f) in, %.3f x %.3f in",
				otherInk.minX, otherInk.minY, otherInk.width(), otherInk.height());
		if (Math.abs(dx) <= horizontalTolerance && Math.abs(dy) <= verticalTolerance) {
			check.pass(sheet + " position", area);
		} else {
			check.fail(sheet + " position", String.format(Locale.US, "shifted %+.3f in horizontally, %+.3f in vertically; %s, expected (%.3f, %.3f) in",
					dx, dy, area, baseInk.minX, baseInk.minY));
		}
		if (Math.abs(dw) <= horizontalTolerance && Math.abs(dh) <= verticalTolerance) {
			check.pass(sheet + " size", String.format(Locale.US, "%.3f x %.3f in", otherInk.width(), otherInk.height()));
		} else {
			check.fail(sheet + " size", String.format(Locale.US, "%+.3f in wider, %+.3f in taller (%.3f x %.3f in, expected %.3f x %.3f in)",
					dw, dh, otherInk.width(), otherInk.height(), baseInk.width(), baseInk.height()));
		}
		if (!baseInk.dots.isEmpty()) {
			double match = baseInk.matchIn(otherInk, dx, dy);
			String message = String.format(Locale.US, "%.1f%% of %d dots in place once the shift is undone (%d dots printed)",
					match * 100, baseInk.dots.size(), otherInk.dots.size());
			if (match >= minimumDotMatch) {
				check.pass(sheet + " dots", message);
			} else {
				check.fail(sheet + " dots", message);
			}
		}
		compareText(check, sheet, base, other, page);
	}

	private void compareText(CheckResult check, String sheet, EscpResult base, EscpResult other, int page) {
		List<TextLine> baseLines = linesOf(base, page);
		List<TextLine> otherLines = linesOf(other, page);
		if (baseLines.isEmpty() && otherLines.isEmpty()) {
			return;
		}
		int differences = 0;
		for (int index = 0; index < Math.max(baseLines.size(), otherLines.size()); index++) {
			TextLine expected = index < baseLines.size() ? baseLines.get(index) : null;
			TextLine actual = index < otherLines.size() ? otherLines.get(index) : null;
			if (expected == null || actual == null) {
				differences++;
				check.fail(sheet + " text", actual == null ? "missing line \"" + expected.getText().trim() + "\""
						: "extra line \"" + actual.getText().trim() + "\"");
			} else if (!expected.getText().equals(actual.getText())
					|| Math.abs(expected.getY() - actual.getY()) > verticalTolerance
					|| Math.abs(left(expected) - left(actual)) > horizontalTolerance) {
				differences++;
				check.fail(sheet + " text", String.format(Locale.US, "\"%s\" at (%.3f, %.4f) in, expected \"%s\" at (%.3f, %.4f) in",
						actual.getText().trim(), left(actual), actual.getY(), expected.getText().trim(), left(expected), expected.getY()));
			}
		}
		if (differences == 0) {
			check.pass(sheet + " text", baseLines.size() + " line(s) identical");
		}
	}

	private static List<TextLine> linesOf(EscpResult result, int page) {
		List<TextLine> lines = TextLine.of(result);
		for (int index = lines.size() - 1; index >= 0; index--) {
			if (lines.get(index).getPage() != page || lines.get(index).getText().trim().length() == 0) {
				lines.remove(index);
			}
		}
		return lines;
	}

	private static double left(TextLine line) {
		for (Glyph glyph : line.getGlyphs()) {
			if (glyph.getCharacter() != ' ') {
				return glyph.getX();
			}
		}
		return 0;
	}

	/**
	 * What one sheet shows: graphics dots on a 1/120 x 1/72 in grid and their bounding box.
	 * A sheet without graphics uses the cells of its printed characters for the box.
	 */
	private static class Ink {
		private final Set<Long> dots = new HashSet<Long>();
		private double minX = Double.MAX_VALUE;
		private double minY = Double.MAX_VALUE;
		private double maxX = -Double.MAX_VALUE;
		private double maxY = -Double.MAX_VALUE;

		Ink(EscpResult result, int page) {
			for (BitImage bitImage : result.getBitImages(page)) {
				long[] columns = bitImage.getColumns();
				for (int column = 0; column < columns.length; column++) {
					if (columns[column] == 0) {
						continue;
					}
					double x = bitImage.getX() + column * bitImage.getColumnWidth();
					for (int index = 0; index < bitImage.getDots(); index++) {
						if (bitImage.isSet(column, index)) {
							double y = bitImage.getY() + index * bitImage.getDotPitch();
							dots.add(key(x, y));
							extend(x, y, x + bitImage.getColumnWidth(), y + bitImage.getDotPitch());
						}
					}
				}
			}
			if (dots.isEmpty()) {
				for (Glyph glyph : result.getGlyphs(page)) {
					if (glyph.getCharacter() != ' ') {
						extend(glyph.getX(), glyph.getY(), glyph.getX() + glyph.getWidth(), glyph.getY() + 9 * DOT_Y);
					}
				}
			}
		}

		private void extend(double left, double top, double right, double bottom) {
			minX = Math.min(minX, left);
			minY = Math.min(minY, top);
			maxX = Math.max(maxX, right);
			maxY = Math.max(maxY, bottom);
		}

		boolean isEmpty() {
			return minX == Double.MAX_VALUE;
		}

		double width() {
			return maxX - minX;
		}

		double height() {
			return maxY - minY;
		}

		/**
		 * @return Share of this sheet's dots that the other sheet has too, within one dot,
		 * after moving the other sheet back by (dx, dy).
		 */
		double matchIn(Ink other, double dx, double dy) {
			int found = 0;
			for (Long dot : dots) {
				long column = dot >> 32;
				long row = (int) (long) dot;
				double x = column * DOT_X + dx;
				double y = row * DOT_Y + dy;
				if (other.hasNear(x, y)) {
					found++;
				}
			}
			return dots.isEmpty() ? 1 : found / (double) dots.size();
		}

		private boolean hasNear(double x, double y) {
			long column = Math.round(x / DOT_X);
			long row = Math.round(y / DOT_Y);
			for (long c = column - 1; c <= column + 1; c++) {
				for (long r = row - 1; r <= row + 1; r++) {
					if (dots.contains((c << 32) | (r & 0xFFFFFFFFL))) {
						return true;
					}
				}
			}
			return false;
		}

		private static long key(double x, double y) {
			return (Math.round(x / DOT_X) << 32) | (Math.round(y / DOT_Y) & 0xFFFFFFFFL);
		}
	}
}
