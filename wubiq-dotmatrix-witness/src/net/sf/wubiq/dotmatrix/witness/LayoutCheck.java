package net.sf.wubiq.dotmatrix.witness;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Compares what the application drew ({@link ExpectedText}) with what the
 * virtual printer laid down.
 * <ul>
 * <li>Each string must be printed once, on the right sheet.</li>
 * <li>The printed baseline must be within half a 6 lpi line (1/12 in) of the intended one.</li>
 * <li>The first character must start within one character cell of the intended x.</li>
 * <li>Bold, italic and underline must match.</li>
 * <li>A different pitch is only reported as a note, unless strict pitch is enabled.</li>
 * <li>Interpreter warnings fail the job, unless disabled.</li>
 * </ul>
 * @author Federico Alcantara
 *
 */
public class LayoutCheck {
	private final PrinterProfile profile;
	private double verticalTolerance = 1d / 12d;
	private double horizontalCells = 1;
	private boolean strictPitch;
	private boolean failOnWarnings = true;

	public LayoutCheck(PrinterProfile profile) {
		this.profile = profile;
	}

	public LayoutCheck verticalTolerance(double inches) {
		this.verticalTolerance = inches;
		return this;
	}

	public LayoutCheck horizontalCells(double cells) {
		this.horizontalCells = cells;
		return this;
	}

	public LayoutCheck strictPitch(boolean strictPitch) {
		this.strictPitch = strictPitch;
		return this;
	}

	public LayoutCheck failOnWarnings(boolean failOnWarnings) {
		this.failOnWarnings = failOnWarnings;
		return this;
	}

	public CheckResult check(EscpResult result, List<ExpectedText> expectations, int expectedPages) {
		CheckResult check = new CheckResult();
		List<TextLine> lines = TextLine.of(result);
		for (ExpectedText expected : expectations) {
			check(expected, lines, check);
		}
		if (expectedPages > 0 && result.getPageCount() != expectedPages) {
			check.fail("job", "expected " + expectedPages + " sheet(s), printer used " + result.getPageCount());
		}
		if (expectedPages > 0 && result.getFormFeeds() < expectedPages) {
			check.fail("job", "expected at least " + expectedPages + " form feed(s), stream has " + result.getFormFeeds());
		}
		for (String warning : result.getWarnings()) {
			if (failOnWarnings) {
				check.fail("printer", warning);
			} else {
				check.note("printer", warning);
			}
		}
		return check;
	}

	private void check(ExpectedText expected, List<TextLine> lines, CheckResult check) {
		String subject = expected.toString();
		List<Glyph> matches = new ArrayList<Glyph>();
		for (TextLine line : lines) {
			if (line.getPage() != expected.getPage()) {
				continue;
			}
			String text = line.getText();
			int index = text.indexOf(expected.getText());
			while (index >= 0) {
				matches.add(line.getGlyphs().get(index));
				index = text.indexOf(expected.getText(), index + 1);
			}
		}
		if (matches.isEmpty()) {
			check.fail(subject, "not printed on sheet " + (expected.getPage() + 1) + where(expected, lines));
			return;
		}
		Glyph best = null;
		double bestDistance = Double.MAX_VALUE;
		int near = 0;
		for (Glyph glyph : matches) {
			double dx = glyph.getX() - expected.getX();
			double dy = glyph.getY() + profile.getBaselineOffset() - expected.getBaseline();
			double distance = Math.hypot(dx, dy * 2);
			if (Math.abs(dy) <= verticalTolerance && Math.abs(dx) <= glyph.getWidth() * horizontalCells + 0.0001) {
				near++;
			}
			if (distance < bestDistance) {
				bestDistance = distance;
				best = glyph;
			}
		}
		double dx = best.getX() - expected.getX();
		double dy = best.getY() + profile.getBaselineOffset() - expected.getBaseline();
		double cells = dx / best.getWidth();
		List<String> problems = new ArrayList<String>();
		if (Math.abs(dy) > verticalTolerance) {
			problems.add(String.format(Locale.US, "baseline off by %+.3f in (%+.1f lines at 6 lpi)", dy, dy * 6));
		}
		if (Math.abs(dx) > best.getWidth() * horizontalCells + 0.0001) {
			problems.add(String.format(Locale.US, "x off by %+.3f in (%+.1f columns at %s cpi)", dx, cells, best.getPitchLabel()));
		}
		if (near > 1) {
			problems.add("printed " + near + " times");
		}
		if (best.isBold() != expected.isBold()) {
			problems.add("bold " + best.isBold() + ", expected " + expected.isBold());
		}
		if (best.isItalic() != expected.isItalic()) {
			problems.add("italic " + best.isItalic() + ", expected " + expected.isItalic());
		}
		if (best.isUnderline() != expected.isUnderline()) {
			problems.add("underline " + best.isUnderline() + ", expected " + expected.isUnderline());
		}
		String position = String.format(Locale.US, "at x %.3f baseline %.3f (%+.1f col, %+.2f lines)", best.getX(),
				best.getY() + profile.getBaselineOffset(), cells, dy * 6);
		if (problems.isEmpty()) {
			check.pass(subject, position);
		} else {
			check.fail(subject, join(problems) + "; " + position);
		}
		if (expected.getCpi() > 0 && Math.abs(best.getCpi() - expected.getCpi()) > 0.05) {
			String message = "printed at " + best.getPitchLabel() + " cpi, expected " + expected.getCpi();
			if (strictPitch) {
				check.fail(subject, message);
			} else {
				check.note(subject, message);
			}
		}
	}

	private String where(ExpectedText expected, List<TextLine> lines) {
		String first = expected.getText().trim();
		if (first.length() > 3) {
			first = first.substring(0, 3);
		}
		for (TextLine line : lines) {
			int index = line.getText().indexOf(first);
			if (index >= 0) {
				return String.format(Locale.US, " (closest: sheet %d y %.3f \"%s\")", line.getPage() + 1, line.getY(),
						line.getText().trim());
			}
		}
		return "";
	}

	private String join(List<String> values) {
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (builder.length() > 0) {
				builder.append(", ");
			}
			builder.append(value);
		}
		return builder.toString();
	}
}
