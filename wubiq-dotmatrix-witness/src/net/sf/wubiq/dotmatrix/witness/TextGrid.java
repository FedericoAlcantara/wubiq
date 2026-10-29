package net.sf.wubiq.dotmatrix.witness;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Plain text picture of the printed sheets: one row per head position, characters
 * placed on a 10 cpi column grid. Narrower pitches are packed so no character is lost.
 * Meant for humans and for golden file diffs.
 * @author Federico Alcantara
 *
 */
public final class TextGrid {
	private TextGrid() {
	}

	public static String render(EscpResult result, PrinterProfile profile) {
		List<TextLine> lines = TextLine.of(result);
		int columns = (int) Math.round(profile.getCarriageWidth() * 10);
		StringBuilder builder = new StringBuilder();
		String tens = tensRuler(columns);
		String units = unitsRuler(columns);
		for (int page = 0; page < result.getPageCount(); page++) {
			builder.append("Sheet ").append(page + 1).append(" of ").append(result.getPageCount())
					.append(" (").append(profile).append(")\n");
			builder.append("    y in | ").append(tens).append('\n');
			builder.append("         | ").append(units).append('\n');
			for (TextLine line : lines) {
				if (line.getPage() == page) {
					builder.append(String.format(Locale.US, "%8.4f | ", line.getY())).append(row(line)).append('\n');
				}
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private static String row(TextLine line) {
		StringBuilder text = new StringBuilder();
		Set<String> attributes = new TreeSet<String>();
		int lastColumn = -1;
		for (Glyph glyph : line.getGlyphs()) {
			int column = (int) Math.round(glyph.getX() * 10);
			if (column <= lastColumn) {
				column = lastColumn + 1;
			}
			while (text.length() < column) {
				text.append(' ');
			}
			text.append(glyph.getCharacter());
			lastColumn = column;
			if (glyph.getCharacter() != ' ') {
				attributes.add(glyph.getPitchLabel() + "cpi");
				if (glyph.isBold()) {
					attributes.add("bold");
				}
				if (glyph.isItalic()) {
					attributes.add("italic");
				}
				if (glyph.isUnderline()) {
					attributes.add("underline");
				}
			}
		}
		String value = text.toString().replaceAll("\\s+$", "");
		if (!attributes.isEmpty()) {
			value += "   <" + join(attributes) + ">";
		}
		return value;
	}

	private static String join(Set<String> values) {
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(value);
		}
		return builder.toString();
	}

	private static String tensRuler(int columns) {
		StringBuilder builder = new StringBuilder();
		for (int column = 0; column < columns; column++) {
			builder.append(column % 10 == 0 ? Character.forDigit((column / 10) % 10, 10) : ' ');
		}
		return builder.toString();
	}

	private static String unitsRuler(int columns) {
		StringBuilder builder = new StringBuilder();
		for (int column = 0; column < columns; column++) {
			builder.append(Character.forDigit(column % 10, 10));
		}
		return builder.toString();
	}
}
