package net.sf.wubiq.dotmatrix.witness;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Checks the printed text widths of a job against the expected text extents: the left edge,
 * right edge and baseline of every text line, in inches. A job printed as graphics is
 * measured by its ink bands, one band per text line.
 * @author Federico Alcantara
 *
 */
public class TextExtents {
	private static final double ROWS_PER_INCH = 144;
	private static final int BAND_BREAK = 3;
	private static final double BAND_SLACK = 0.05;
	private static final int LINES_NEEDED = 3;

	/**
	 * One expected text line.
	 */
	public static class Line {
		private final int page;
		private final double left;
		private final double right;
		private final double baseline;
		private final String text;

		/**
		 * @param page Zero based sheet index.
		 * @param left Left edge of the text, inches from the left edge of the paper.
		 * @param right Right edge of the text, inches from the left edge of the paper.
		 * @param baseline Baseline of the text, inches from the top of the sheet.
		 * @param text The text of the line.
		 */
		public Line(int page, double left, double right, double baseline, String text) {
			this.page = page;
			this.left = left;
			this.right = right;
			this.baseline = baseline;
			this.text = text;
		}

		public int getPage() {
			return page;
		}

		public double getLeft() {
			return left;
		}

		public double getRight() {
			return right;
		}

		public double getBaseline() {
			return baseline;
		}

		public String getText() {
			return text;
		}
	}

	private static class Band {
		private double top;
		private double bottom;
		private double left;
		private double right;
	}

	private double tolerance = 0.1;

	/**
	 * @param tolerance Largest accepted width or left edge difference, in inches.
	 * @return This object.
	 */
	public TextExtents setTolerance(double tolerance) {
		this.tolerance = tolerance;
		return this;
	}

	/**
	 * Reads an expected text extents file: one line per row, the fields sheet (from 0), left,
	 * right, baseline and text separated by tabs. Empty rows and rows starting with # are skipped.
	 * @param file The expected file.
	 * @return The expected lines.
	 * @throws IOException When the file can't be read or a row is malformed.
	 */
	public static List<Line> read(File file) throws IOException {
		List<Line> lines = new ArrayList<Line>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		try {
			String row;
			int lineNumber = 0;
			while ((row = reader.readLine()) != null) {
				lineNumber++;
				if (row.length() == 0 || row.startsWith("#")) {
					continue;
				}
				String[] fields = row.split("\t");
				if (fields.length < 4) {
					throw malformed(file, lineNumber);
				}
				try {
					lines.add(new Line(Integer.parseInt(fields[0]), Double.parseDouble(fields[1]),
							Double.parseDouble(fields[2]), Double.parseDouble(fields[3]),
							fields.length > 4 ? fields[4] : ""));
				} catch (NumberFormatException e) {
					throw malformed(file, lineNumber);
				}
			}
		} finally {
			reader.close();
		}
		return lines;
	}

	private static IOException malformed(File file, int lineNumber) {
		return new IOException(file.getName() + ":" + lineNumber + ": expected sheet, left, right and baseline separated by tabs");
	}

	/**
	 * Checks the printed text widths and left edges of every sheet named by an expected line.
	 * @param result The interpreted job.
	 * @param expected The expected lines.
	 * @return The outcome, sheets in ascending order.
	 */
	public CheckResult check(EscpResult result, List<Line> expected) {
		CheckResult check = new CheckResult();
		TreeSet<Integer> pages = new TreeSet<Integer>();
		for (Line line : expected) {
			pages.add(line.getPage());
		}
		for (int page : pages) {
			checkSheet(check, result, page, expected);
		}
		return check;
	}

	private void checkSheet(CheckResult check, EscpResult result, int page, List<Line> expected) {
		String sheet = "sheet " + (page + 1);
		TreeMap<Integer, double[]> rows = new TreeMap<Integer, double[]>();
		for (BitImage image : result.getBitImages(page)) {
			for (int c = 0; c < image.getColumns().length; c++) {
				if (image.getColumns()[c] == 0) {
					continue;
				}
				for (int d = 0; d < image.getDots(); d++) {
					if (image.isSet(c, d)) {
						double x = image.getX() + c * image.getColumnWidth();
						double y = image.getY() + d * image.getDotPitch();
						int row = (int) Math.round(y * ROWS_PER_INCH);
						double[] extent = rows.get(row);
						if (extent == null) {
							rows.put(row, new double[] {x, x + image.getColumnWidth()});
						} else {
							extent[0] = Math.min(extent[0], x);
							extent[1] = Math.max(extent[1], x + image.getColumnWidth());
						}
					}
				}
			}
		}
		if (rows.isEmpty()) {
			check.fail(sheet, "no graphics dots to measure");
			return;
		}
		List<Band> bands = bands(rows);
		List<Line> merged = merged(page, expected);
		double dy = bands.get(0).bottom - merged.get(0).getBaseline();

		List<Line> measured = new ArrayList<Line>();
		List<Band> measuredBands = new ArrayList<Band>();
		for (Line line : merged) {
			Band holder = null;
			int holders = 0;
			for (Band band : bands) {
				if (holds(band, line, dy)) {
					holder = band;
					holders++;
				}
			}
			if (holders == 1 && heldLines(holder, merged, dy) == 1) {
				measured.add(line);
				measuredBands.add(holder);
			}
		}

		List<Double> shifts = new ArrayList<Double>();
		for (int i = 0; i < measured.size(); i++) {
			shifts.add(measuredBands.get(i).left - measured.get(i).getLeft());
		}
		List<Double> sorted = new ArrayList<Double>(shifts);
		Collections.sort(sorted);
		double median = sorted.isEmpty() ? 0 : sorted.get(sorted.size() / 2);

		for (int i = 0; i < measured.size(); i++) {
			Line line = measured.get(i);
			Band band = measuredBands.get(i);
			double width = band.right - band.left;
			double expectedWidth = line.getRight() - line.getLeft();
			double shift = shifts.get(i);
			String subject = sheet + " \"" + line.getText() + "\"";
			String message = String.format(Locale.US,
					"width %.3f in, expected %.3f in; left edge shifted %+.3f in, the sheet's lines %+.3f in",
					width, expectedWidth, shift, median);
			if (Math.abs(width - expectedWidth) > tolerance || Math.abs(shift - median) > tolerance) {
				check.fail(subject, message);
			} else {
				check.pass(subject, message);
			}
		}
		for (Line line : merged) {
			if (!measured.contains(line)) {
				check.note(sheet + " \"" + line.getText() + "\"", "not measured: no ink band of its own");
			}
		}
		if (measured.size() < LINES_NEEDED) {
			check.fail(sheet, "only " + measured.size() + " text line(s) measured, " + LINES_NEEDED + " needed");
		}
	}

	private List<Band> bands(TreeMap<Integer, double[]> rows) {
		List<Band> bands = new ArrayList<Band>();
		Band band = null;
		int previous = 0;
		for (Map.Entry<Integer, double[]> entry : rows.entrySet()) {
			int row = entry.getKey();
			double[] extent = entry.getValue();
			if (band == null || row - previous > BAND_BREAK) {
				band = new Band();
				band.top = row / ROWS_PER_INCH;
				band.left = extent[0];
				band.right = extent[1];
				bands.add(band);
			} else {
				band.left = Math.min(band.left, extent[0]);
				band.right = Math.max(band.right, extent[1]);
			}
			band.bottom = row / ROWS_PER_INCH;
			previous = row;
		}
		return bands;
	}

	private List<Line> merged(int page, List<Line> expected) {
		List<Line> lines = new ArrayList<Line>();
		for (Line line : expected) {
			if (line.getPage() == page) {
				lines.add(line);
			}
		}
		Collections.sort(lines, new Comparator<Line>() {
			public int compare(Line one, Line other) {
				return Double.compare(one.getBaseline(), other.getBaseline());
			}
		});
		List<Line> merged = new ArrayList<Line>();
		Line previous = null;
		for (Line line : lines) {
			Line last = merged.isEmpty() ? null : merged.get(merged.size() - 1);
			if (previous != null && line.getBaseline() - previous.getBaseline() <= 1d / ROWS_PER_INCH) {
				merged.set(merged.size() - 1, new Line(page, Math.min(last.getLeft(), line.getLeft()),
						Math.max(last.getRight(), line.getRight()), last.getBaseline(),
						last.getText() + " " + line.getText()));
			} else {
				merged.add(line);
			}
			previous = line;
		}
		return merged;
	}

	private boolean holds(Band band, Line line, double dy) {
		return band.top - BAND_SLACK <= line.getBaseline() + dy && line.getBaseline() + dy <= band.bottom + BAND_SLACK;
	}

	private int heldLines(Band band, List<Line> merged, double dy) {
		int count = 0;
		for (Line line : merged) {
			if (holds(band, line, dy)) {
				count++;
			}
		}
		return count;
	}
}
