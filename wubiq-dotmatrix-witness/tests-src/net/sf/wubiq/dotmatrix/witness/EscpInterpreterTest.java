package net.sf.wubiq.dotmatrix.witness;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Tests the virtual printer itself with hand-built ESC/P streams.
 * @author Federico Alcantara
 *
 */
public class EscpInterpreterTest {
	private static final int ESC = 0x1B;
	private static final int CR = 0x0D;
	private static final int LF = 0x0A;
	private static final int FF = 0x0C;
	private static final double DELTA = 0.0001;

	@Test
	public void plainTextLinesAndFormFeed() {
		EscpResult result = interpret(ESC, '@', ESC, 'P', "Hello", CR, LF, "World", CR, FF);
		assertTrue(result.getWarnings().toString(), result.getWarnings().isEmpty());
		assertEquals(1, result.getPageCount());
		assertEquals(1, result.getFormFeeds());
		Glyph w = glyph(result, 'W');
		assertEquals(0, w.getX(), DELTA);
		assertEquals(1d / 6d, w.getY(), DELTA);
		assertEquals(0.4, glyph(result, 'o').getX(), DELTA);
	}

	@Test
	public void pitches() {
		EscpResult result = interpret(ESC, '@', ESC, 'M', "AB", CR, LF, ESC, 'g', "CD", CR, LF,
				ESC, 'P', 0x0F, "EF", 0x12, CR, LF, ESC, 'M', 0x0F, "GH", 0x12, CR, LF, ESC, 'P', ESC, 'W', 1, "IJ", CR, FF);
		assertEquals(1d / 12d, glyph(result, 'B').getX(), DELTA);
		assertEquals(1d / 15d, glyph(result, 'D').getX(), DELTA);
		assertEquals(7d / 120d, glyph(result, 'F').getX(), DELTA);
		assertEquals("17.14", glyph(result, 'F').getPitchLabel());
		assertEquals(6d / 120d, glyph(result, 'H').getX(), DELTA);
		assertEquals(2d / 10d, glyph(result, 'J').getX(), DELTA);
		assertTrue(glyph(result, 'J').isDoubleWidth());
	}

	@Test
	public void absoluteAndRelativePositioning() {
		EscpResult result = interpret(ESC, '@', ESC, '$', 60, 0, "A", ESC, '\\', 12, 0, "B", CR, FF);
		assertEquals(1.0, glyph(result, 'A').getX(), DELTA);
		assertEquals(1.0 + 0.1 + 0.1, glyph(result, 'B').getX(), DELTA);
	}

	@Test
	public void lineSpacingAndPaperAdvance() {
		EscpResult result = interpret(ESC, '@', ESC, '3', 18, LF, LF, "A", ESC, 'J', 108, CR, "B", CR, FF);
		assertEquals(36d / 216d, glyph(result, 'A').getY(), DELTA);
		assertEquals(36d / 216d + 0.5, glyph(result, 'B').getY(), DELTA);
		EscpResult pin24 = new EscpInterpreter(PrinterProfile.twentyFourPin())
				.interpret(bytes(ESC, '@', ESC, '3', 30, LF, "A", CR, FF));
		assertEquals(1d / 6d, glyph(pin24, 'A').getY(), DELTA);
	}

	@Test
	public void shortPageLengthMakesFormFeedUseless() {
		EscpResult result = interpret(ESC, '@', ESC, '3', 6, ESC, 'C', 1, "A", CR, FF, "B", CR, FF);
		assertEquals(1, result.getPageCount());
		assertEquals(6d / 216d, glyph(result, 'B').getY(), DELTA);
		assertTrue(contains(result.getWarnings(), "page length 0.0278 in"));
	}

	@Test
	public void formFeedsMoveToNextSheets() {
		EscpResult result = interpret(ESC, '@', ESC, 'C', 0, 11, "A", CR, FF, "B", CR, FF, LF, "C", CR, FF);
		assertTrue(result.getWarnings().toString(), result.getWarnings().isEmpty());
		assertEquals(3, result.getPageCount());
		assertEquals(1, glyph(result, 'B').getPage());
		assertEquals(0, glyph(result, 'B').getY(), DELTA);
		assertEquals(2, glyph(result, 'C').getPage());
		assertEquals(1d / 6d, glyph(result, 'C').getY(), DELTA);
	}

	@Test
	public void characterTables() {
		EscpResult result = interpret(ESC, '@', ESC, '(', 't', 3, 0, 1, 1, 0, ESC, 't', 1,
				"a", 0xA4, "o ", 0x82, CR, FF);
		assertTrue(result.getWarnings().toString(), result.getWarnings().isEmpty());
		assertEquals("año é", TextLine.of(result).get(0).getText());
		EscpResult latin = interpret(ESC, '@', ESC, 'R', 12, "|", CR, FF);
		assertEquals('ñ', latin.getGlyphs().get(0).getCharacter());
	}

	@Test
	public void unknownRegisteredTableIsReported() {
		EscpResult result = interpret(ESC, '@', ESC, '(', 't', 3, 0, 1, 12, 0, ESC, 't', 1, "x", CR, FF);
		assertTrue(contains(result.getWarnings(), "registered character table 12/0"));
	}

	@Test
	public void utf8LeakageIsReported() {
		EscpResult result = interpret(ESC, '@', "a", 0xC3, 0xB1, "o", CR, FF);
		assertTrue(contains(result.getWarnings(), "UTF-8 for 'ñ'"));
		assertEquals(4, result.getGlyphs().size());
	}

	@Test
	public void rightMarginWraps() {
		StringBuilder line = new StringBuilder();
		for (int index = 0; index < 81; index++) {
			line.append((char) ('0' + index % 10));
		}
		EscpResult result = interpret(ESC, '@', line.toString(), CR, FF);
		Glyph last = result.getGlyphs().get(80);
		assertEquals(0, last.getX(), DELTA);
		assertEquals(1d / 6d, last.getY(), DELTA);
		assertTrue(contains(result.getWarnings(), "right margin"));
	}

	@Test
	public void styles() {
		EscpResult result = interpret(ESC, '@', ESC, 'E', "B", ESC, 'F', ESC, '4', "I", ESC, '5',
				ESC, '-', 1, "U", ESC, '-', 0, ESC, '!', 8 + 128, "M", CR, FF);
		assertTrue(glyph(result, 'B').isBold());
		assertFalse(glyph(result, 'I').isBold());
		assertTrue(glyph(result, 'I').isItalic());
		assertTrue(glyph(result, 'U').isUnderline());
		assertTrue(glyph(result, 'M').isBold());
		assertTrue(glyph(result, 'M').isUnderline());
	}

	@Test
	public void missingResetFormFeedAndUnknownCommandsAreReported() {
		EscpResult result = interpret("A", ESC, 'z', "B");
		assertTrue(contains(result.getWarnings(), "does not start with ESC @"));
		assertTrue(contains(result.getWarnings(), "does not end with a form feed"));
		assertTrue(contains(result.getWarnings(), "unknown command ESC 0x7A"));
	}

	@Test
	public void layoutCheckAndReport() throws Exception {
		PrinterProfile profile = PrinterProfile.ninePin();
		// "Total" at 1 inch, baseline 1/6 + 7/72 inch.
		EscpResult result = interpret(ESC, '@', ESC, 'P', LF, ESC, '$', 60, 0, ESC, 'E', "Total", ESC, 'F', CR, FF);
		List<ExpectedText> expectations = new ArrayList<ExpectedText>();
		double baseline = 1d / 6d + profile.getBaselineOffset();
		expectations.add(new ExpectedText(0, "Total", 1.04, baseline + 0.02).bold(true).cpi(10));
		CheckResult check = new LayoutCheck(profile).check(result, expectations, 1);
		assertTrue(check.toString(), check.isPassed());

		expectations.clear();
		expectations.add(new ExpectedText(0, "Total", 2, baseline).bold(true));
		expectations.add(new ExpectedText(0, "Missing", 0, baseline));
		check = new LayoutCheck(profile).check(result, expectations, 1);
		assertFalse(check.isPassed());
		assertEquals(2, check.count(CheckResult.Status.FAIL));

		File directory = new File(System.getProperty("java.io.tmpdir"), "witness-test-" + System.nanoTime());
		new WitnessReport(profile, 50).write(directory, "unit test", bytes(ESC, '@', "x", CR, FF), result, null, check);
		for (String name : new String[] {"job.escp", "dump.txt", "grid.txt", "lines.json", "check.txt", "sheet-1.png", "index.html"}) {
			File file = new File(directory, name);
			assertTrue(name, file.length() > 0);
			file.delete();
		}
		directory.delete();
		String grid = TextGrid.render(result, profile);
		assertTrue(grid, grid.contains("  0.1667 |           Total   <10cpi bold>"));
	}

	private EscpResult interpret(Object... parts) {
		return new EscpInterpreter(PrinterProfile.ninePin()).interpret(bytes(parts));
	}

	private Glyph glyph(EscpResult result, char character) {
		for (Glyph glyph : result.getGlyphs()) {
			if (glyph.getCharacter() == character) {
				return glyph;
			}
		}
		throw new AssertionError("No glyph '" + character + "' in " + result.getGlyphs());
	}

	private boolean contains(List<String> warnings, String text) {
		for (String warning : warnings) {
			if (warning.contains(text)) {
				return true;
			}
		}
		return false;
	}

	static byte[] bytes(Object... parts) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (Object part : parts) {
			if (part instanceof Integer) {
				buffer.write((Integer) part);
			} else if (part instanceof Character) {
				buffer.write((Character) part);
			} else {
				try {
					byte[] text = ((String) part).getBytes("ISO-8859-1");
					buffer.write(text, 0, text.length);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}
			}
		}
		return buffer.toByteArray();
	}
}
