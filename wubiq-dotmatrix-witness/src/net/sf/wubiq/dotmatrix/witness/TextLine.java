package net.sf.wubiq.dotmatrix.witness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * All glyphs printed on one sheet at the same head position, ordered left to right.
 * @author Federico Alcantara
 *
 */
public class TextLine {
	private static final double SAME_LINE = 0.0005;
	private final int page;
	private final double y;
	private final List<Glyph> glyphs = new ArrayList<Glyph>();

	public TextLine(int page, double y) {
		this.page = page;
		this.y = y;
	}

	/**
	 * Groups glyphs into lines, sorted by sheet, then top to bottom.
	 * @param result Interpreter output.
	 * @return Lines with their glyphs sorted by x.
	 */
	public static List<TextLine> of(EscpResult result) {
		List<TextLine> lines = new ArrayList<TextLine>();
		for (Glyph glyph : result.getGlyphs()) {
			TextLine line = null;
			for (TextLine candidate : lines) {
				if (candidate.page == glyph.getPage() && Math.abs(candidate.y - glyph.getY()) < SAME_LINE) {
					line = candidate;
					break;
				}
			}
			if (line == null) {
				line = new TextLine(glyph.getPage(), glyph.getY());
				lines.add(line);
			}
			line.glyphs.add(glyph);
		}
		for (TextLine line : lines) {
			Collections.sort(line.glyphs, new Comparator<Glyph>() {
				public int compare(Glyph first, Glyph second) {
					return Double.compare(first.getX(), second.getX());
				}
			});
		}
		Collections.sort(lines, new Comparator<TextLine>() {
			public int compare(TextLine first, TextLine second) {
				if (first.page != second.page) {
					return first.page < second.page ? -1 : 1;
				}
				return Double.compare(first.y, second.y);
			}
		});
		return lines;
	}

	public int getPage() {
		return page;
	}

	/**
	 * @return Head position (top of the cells) in inches from the top of the sheet.
	 */
	public double getY() {
		return y;
	}

	public List<Glyph> getGlyphs() {
		return glyphs;
	}

	/**
	 * @return Characters in x order, one per glyph (index matches {@link #getGlyphs()}).
	 */
	public String getText() {
		StringBuilder builder = new StringBuilder();
		for (Glyph glyph : glyphs) {
			builder.append(glyph.getCharacter());
		}
		return builder.toString();
	}
}
