package net.sf.wubiq.dotmatrix.witness;

import java.util.Locale;

/**
 * A string the application drew, and where it drew it. Positions are in inches
 * on the sheet: x is the left edge of the first character, y is the baseline.
 * @author Federico Alcantara
 *
 */
public class ExpectedText {
	private final int page;
	private final String text;
	private final double x;
	private final double baseline;
	private double cpi;
	private boolean bold;
	private boolean italic;
	private boolean underline;

	public ExpectedText(int page, String text, double x, double baseline) {
		this.page = page;
		this.text = text;
		this.x = x;
		this.baseline = baseline;
	}

	/**
	 * Builds an expectation from Java2D user space coordinates (1/72 inch).
	 */
	public static ExpectedText points(int page, String text, double x, double baseline) {
		return new ExpectedText(page, text, x / 72d, baseline / 72d);
	}

	public int getPage() {
		return page;
	}

	public String getText() {
		return text;
	}

	public double getX() {
		return x;
	}

	public double getBaseline() {
		return baseline;
	}

	/**
	 * @return Expected pitch, or 0 when any pitch is acceptable.
	 */
	public double getCpi() {
		return cpi;
	}

	public ExpectedText cpi(double cpi) {
		this.cpi = cpi;
		return this;
	}

	public boolean isBold() {
		return bold;
	}

	public ExpectedText bold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public boolean isItalic() {
		return italic;
	}

	public ExpectedText italic(boolean italic) {
		this.italic = italic;
		return this;
	}

	public boolean isUnderline() {
		return underline;
	}

	public ExpectedText underline(boolean underline) {
		this.underline = underline;
		return this;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "sheet %d x %.3f baseline %.3f \"%s\"", page + 1, x, baseline, text);
	}
}
