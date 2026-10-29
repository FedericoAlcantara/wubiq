package net.sf.wubiq.dotmatrix.witness;

/**
 * Physical characteristics of the emulated dot-matrix printer and its paper.
 * All values are in inches.
 * @author Federico Alcantara
 *
 */
public class PrinterProfile {
	private boolean pin24;
	private double paperWidth = 8.5;
	private double paperHeight = 11;
	private double carriageWidth = 8.0;

	/**
	 * @return 9-pin printer, US Letter paper, 80 column carriage.
	 */
	public static PrinterProfile ninePin() {
		return new PrinterProfile();
	}

	/**
	 * @return 24-pin printer, US Letter paper, 80 column carriage.
	 */
	public static PrinterProfile twentyFourPin() {
		PrinterProfile returnValue = new PrinterProfile();
		returnValue.setPin24(true);
		return returnValue;
	}

	public boolean isPin24() {
		return pin24;
	}

	public PrinterProfile setPin24(boolean pin24) {
		this.pin24 = pin24;
		return this;
	}

	public double getPaperWidth() {
		return paperWidth;
	}

	public PrinterProfile setPaperWidth(double paperWidth) {
		this.paperWidth = paperWidth;
		return this;
	}

	/**
	 * @return Height of one physical sheet (or fan-fold page).
	 */
	public double getPaperHeight() {
		return paperHeight;
	}

	public PrinterProfile setPaperHeight(double paperHeight) {
		this.paperHeight = paperHeight;
		return this;
	}

	/**
	 * @return Maximum printable line width (8.0 for 80 columns, 13.6 for 136 columns).
	 */
	public double getCarriageWidth() {
		return carriageWidth;
	}

	public PrinterProfile setCarriageWidth(double carriageWidth) {
		this.carriageWidth = carriageWidth;
		return this;
	}

	/**
	 * Distance from the print head position (top of the character cell) to the
	 * character baseline. Draft characters use the upper pins for the body and
	 * the lower ones for descenders: about 7/72 inch on 9-pin, 18/180 on 24-pin.
	 * @return Baseline offset in inches.
	 */
	public double getBaselineOffset() {
		return pin24 ? 18d / 180d : 7d / 72d;
	}

	/**
	 * @return Vertical unit for ESC 3 and ESC J (1/216 or 1/180 inch).
	 */
	public double getFineLineUnit() {
		return pin24 ? 1d / 180d : 1d / 216d;
	}

	@Override
	public String toString() {
		return (pin24 ? "24-pin" : "9-pin") + ", paper " + paperWidth + "x" + paperHeight
				+ " in, carriage " + carriageWidth + " in";
	}
}
