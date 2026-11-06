package net.sf.wubiq.dotmatrix;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.wubiq.dotmatrix.DrawItem.Method;

/**
 * The print jobs used to check dot-matrix output. Everything stays inside an
 * 8 inch (80 column) carriage.
 * @author Federico Alcantara
 *
 */
public final class Fixtures {
	private static final String MONO = Font.MONOSPACED;
	private static final String SANS = Font.SANS_SERIF;

	private Fixtures() {
	}

	public static List<WitnessFixture> all() {
		List<WitnessFixture> fixtures = new ArrayList<WitnessFixture>();
		fixtures.add(ruler());
		fixtures.add(invoice());
		fixtures.add(fontSizes());
		fixtures.add(stylesAndCharset());
		fixtures.add(drawPaths());
		fixtures.add(transforms());
		fixtures.add(multiPage());
		return fixtures;
	}

	/**
	 * Labels on a 1 inch by 1/6 inch grid: the basic row/column sanity check.
	 */
	static WitnessFixture ruler() {
		WitnessFixture fixture = new WitnessFixture("ruler", 1);
		Font font = new Font(MONO, Font.PLAIN, 10);
		for (int row = 0; row < 10; row++) {
			for (int column = 0; column < 7; column++) {
				fixture.add(0, String.format("R%dC%d", row, column), 36 + 72 * column, 72 + 12 * row, font);
			}
		}
		return fixture;
	}

	/**
	 * A typical business document with right aligned amounts measured with font metrics.
	 */
	static WitnessFixture invoice() {
		WitnessFixture fixture = new WitnessFixture("invoice", 1);
		Font title = new Font(SANS, Font.BOLD, 14);
		Font body = new Font(SANS, Font.PLAIN, 10);
		Font header = new Font(SANS, Font.BOLD, 10);
		Font small = new Font(SANS, Font.PLAIN, 8);
		fixture.add(0, "WUBIQ SUPPLIES S.R.L.", 36, 60, title);
		fixture.addRight(0, "INVOICE 000123", 540, 60, header);
		fixture.add(0, "Date: 2026-10-29", 36, 84, body);
		fixture.add(0, "Customer: Acme Corp.", 36, 96, body);
		fixture.add(0, "Qty", 36, 124, header);
		fixture.add(0, "Description", 84, 124, header);
		fixture.addRight(0, "Price", 450, 124, header);
		fixture.addRight(0, "Amount", 540, 124, header);
		String[][] lines = {
				{"2", "Printer ribbon", "12.50", "25.00"},
				{"1", "Continuous paper box", "38.00", "38.00"},
				{"10", "Labels 4x6", "0.75", "7.50"},
				{"3", "USB cable", "4.20", "12.60"},
				{"1", "Service", "60.00", "60.00"},
		};
		float y = 140;
		for (String[] line : lines) {
			fixture.addRight(0, line[0], 60, y, body);
			fixture.add(0, line[1], 84, y, body);
			fixture.addRight(0, line[2], 450, y, body);
			fixture.addRight(0, line[3], 540, y, body);
			y += 14;
		}
		fixture.add(0, "Total", 380, y + 12, header);
		fixture.addRight(0, "143.10", 540, y + 12, header);
		fixture.add(0, "Thank you for your business", 36, y + 40, small);
		return fixture;
	}

	/**
	 * One line per font size, covering every pitch of the size table.
	 */
	static WitnessFixture fontSizes() {
		WitnessFixture fixture = new WitnessFixture("font-sizes", 1);
		int[] sizes = {6, 7, 8, 9, 10, 12, 14, 17, 20, 24};
		float y = 60;
		for (int size : sizes) {
			y += size + 8;
			fixture.add(0, "Size " + size + " ABCDE 12345", 36, y, new Font(MONO, Font.PLAIN, size));
		}
		return fixture;
	}

	/**
	 * Bold, italic, underline and the Spanish characters a Latin American invoice uses.
	 */
	static WitnessFixture stylesAndCharset() {
		WitnessFixture fixture = new WitnessFixture("styles-charset", 1);
		Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
		attributes.put(TextAttribute.FAMILY, SANS);
		attributes.put(TextAttribute.SIZE, 10f);
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		fixture.add(0, "Plain text", 36, 72, new Font(SANS, Font.PLAIN, 10));
		fixture.add(0, "Bold text", 36, 84, new Font(SANS, Font.BOLD, 10));
		fixture.add(0, "Italic text", 36, 96, new Font(SANS, Font.ITALIC, 10));
		fixture.add(0, "Bold italic text", 36, 108, new Font(SANS, Font.BOLD | Font.ITALIC, 10));
		fixture.add(0, "Underlined text", 36, 120, new Font(attributes));
		Font body = new Font(SANS, Font.PLAIN, 10);
		fixture.add(0, "Año: ñandú, acción, pingüino", 36, 144, body);
		fixture.add(0, "¿Qué? ¡Sí! ÁÉÍÓÚ Ñ", 36, 156, body);
		return fixture;
	}

	/**
	 * The same kind of text through every Java2D text entry point.
	 */
	static WitnessFixture drawPaths() {
		WitnessFixture fixture = new WitnessFixture("draw-paths", 1);
		Font font = new Font(SANS, Font.PLAIN, 10);
		fixture.add(0, "drawString(int) sample", 36, 72, font).method(Method.STRING_INT);
		fixture.add(0, "drawString(float) sample", 36.6f, 96, font).method(Method.STRING_FLOAT);
		fixture.add(0, "drawString(iterator) sample", 36, 120, font).method(Method.ATTRIBUTED);
		fixture.add(0, "drawGlyphVector sample", 36, 144, font).method(Method.GLYPH_VECTOR);
		fixture.add(0, "drawChars sample", 36, 168, font).method(Method.CHARS);
		fixture.add(0, "create() child sample", 36, 192, font).method(Method.CHILD_GRAPHICS);
		return fixture;
	}

	/**
	 * Text drawn under application transforms (report engines translate per band and scale to zoom).
	 */
	static WitnessFixture transforms() {
		WitnessFixture fixture = new WitnessFixture("transforms", 1);
		Font font = new Font(SANS, Font.PLAIN, 10);
		fixture.add(0, "No transform", 36, 72, font);
		fixture.add(0, "Translated band", 0, 0, font).transform(AffineTransform.getTranslateInstance(36, 96));
		fixture.add(0, "Scaled 2x", 18, 60, new Font(SANS, Font.PLAIN, 6)).transform(AffineTransform.getScaleInstance(2, 2));
		AffineTransform both = AffineTransform.getTranslateInstance(36, 168);
		both.scale(0.5, 0.5);
		fixture.add(0, "Translated and scaled half", 0, 0, new Font(SANS, Font.PLAIN, 20)).transform(both);
		return fixture;
	}

	/**
	 * Three pages: page indexes, form feeds and text near the bottom of the sheet.
	 */
	static WitnessFixture multiPage() {
		WitnessFixture fixture = new WitnessFixture("multi-page", 3);
		Font header = new Font(SANS, Font.BOLD, 10);
		Font body = new Font(SANS, Font.PLAIN, 10);
		for (int page = 0; page < 3; page++) {
			fixture.add(page, "Page " + (page + 1) + " of 3", 36, 54, header);
			fixture.add(page, "Body line on page " + (page + 1), 36, 108, body);
			fixture.add(page, "Footer " + (page + 1), 36, 756, body);
		}
		return fixture;
	}
}
