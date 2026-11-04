package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.dotmatrix.witness.BitImage;
import net.sf.wubiq.dotmatrix.witness.EscpInterpreter;
import net.sf.wubiq.dotmatrix.witness.EscpResult;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.wrappers.EscpStreamJob;

import org.junit.After;
import org.junit.Test;

/**
 * Shapes and images are written as bit-image bands, at the place the application drew them; pages
 * with only text write none.
 * @author Federico Alcantara
 *
 */
public class EscpGraphicsTest {
	private static final double TOLERANCE = 0.02;

	@After
	public void restoreProperties() {
		System.clearProperty(PropertyKeys.WUBIQ_DOTMATRIX_GRAPHICS);
	}

	@Test
	public void aFilledRectangleIsWrittenWhereItWasDrawn() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 144, 144, 36); // 1 in, 2 in; 2 in by 1/2 in.
			}
		});
		double[] box = bounds(result);
		assertEquals("left", 1.0 + 18 / 72d, box[0], TOLERANCE);
		assertEquals("top", 2.0 + 18 / 72d, box[1], TOLERANCE);
		assertEquals("right", 3.0 + 18 / 72d, box[2], TOLERANCE);
		assertEquals("bottom", 2.5 + 18 / 72d, box[3], TOLERANCE);
		assertEquals("dots", 2.0 * 120 * 0.5 * 72, dots(result), 2.0 * 120 * 0.5 * 72 * 0.03);
	}

	@Test
	public void bitImagesAreEightDotsHighAt120DotsPerInchOnNinePin() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 72, 72, 72);
			}
		});
		BitImage band = result.getBitImages().get(0);
		assertEquals("dots high", 8, band.getDots());
		assertEquals("dot columns per inch", 120, Math.round(1 / band.getColumnWidth()));
		assertEquals("dot lines per inch", 72, Math.round(1 / band.getDotPitch()));
	}

	@Test
	public void twentyFourPinPrintersGetBandsOf24DotsAt180DotsPerInch() throws Exception {
		EscpResult result = write(PrinterProfile.twentyFourPin(), PrinterType.DOT_MATRIX_HQ, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 144, 144, 36);
			}
		});
		BitImage band = result.getBitImages().get(0);
		assertEquals("dots high", 24, band.getDots());
		assertEquals("dot columns per inch", 180, Math.round(1 / band.getColumnWidth()));
		assertEquals("dot lines per inch", 180, Math.round(1 / band.getDotPitch()));
		double[] box = bounds(result);
		assertEquals("left", 1.0 + 18 / 72d, box[0], TOLERANCE);
		assertEquals("top", 2.0 + 18 / 72d, box[1], TOLERANCE);
		assertEquals("right", 3.0 + 18 / 72d, box[2], TOLERANCE);
		assertEquals("bottom", 2.5 + 18 / 72d, box[3], TOLERANCE);
		assertEquals("dots", 2.0 * 180 * 0.5 * 180, dots(result), 2.0 * 180 * 0.5 * 180 * 0.03);
	}

	@Test
	public void graphicsFollowThePrintableAreaLikeText() throws Exception {
		// The printer counts from its printable area, 18 pt across and 36 pt down from the paper's corner; the
		// imageable area is at 18, 18. A box at 72, 72 of it is at 72 pt across and 54 pt down from where the printer counts.
		EscpResult result = write(PrinterProfile.ninePin(), null, 18, 36, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 72, 72, 72);
			}
		});
		double[] box = bounds(result);
		assertEquals("left", 1.0, box[0], TOLERANCE);
		assertEquals("top", 1.0 - 18 / 72d, box[1], TOLERANCE);
	}

	@Test
	public void applicationTransformsAndSavedTransformsApply() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				AffineTransform saved = graphics.getTransform();
				graphics.translate(72, 72);
				graphics.scale(2, 2);
				graphics.fillRect(0, 0, 36, 18); // 1 in, 1 in; 1 in by 1/2 in.
				graphics.setTransform(saved);
				graphics.fillRect(0, 288, 36, 18); // 0, 4 in; back to the page's own transform.
			}
		});
		List<BitImage> bands = result.getBitImages();
		double[] first = bounds(bands, 0.0, 3.0);
		assertEquals("scaled left", 1.0 + 18 / 72d, first[0], TOLERANCE);
		assertEquals("scaled top", 1.0 + 18 / 72d, first[1], TOLERANCE);
		assertEquals("scaled right", 2.0 + 18 / 72d, first[2], TOLERANCE);
		assertEquals("scaled bottom", 1.5 + 18 / 72d, first[3], TOLERANCE);
		double[] second = bounds(bands, 3.0, 11.0);
		assertEquals("restored left", 18 / 72d, second[0], TOLERANCE);
		assertEquals("restored top", 4.0 + 18 / 72d, second[1], TOLERANCE);
	}

	@Test
	public void aChildGraphicsDrawsOnTheSamePage() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				Graphics2D child = (Graphics2D) graphics.create();
				try {
					child.translate(144, 144);
					child.fillRect(0, 0, 72, 36);
				} finally {
					child.dispose();
				}
				graphics.fillRect(0, 0, 36, 36); // The parent is not translated.
			}
		});
		assertEquals("left of the first", 18 / 72d, bounds(result.getBitImages(), 0.0, 1.0)[0], TOLERANCE);
		assertEquals("left of the child's", 2.0 + 18 / 72d, bounds(result.getBitImages(), 2.0, 4.0)[0], TOLERANCE);
	}

	@Test
	public void whiteDrawnOverBlackErasesIt() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 72, 144, 72);
				graphics.setColor(Color.WHITE);
				graphics.fillRect(72, 72, 144, 72);
			}
		});
		assertEquals("dots", 0, dots(result));
	}

	@Test
	public void aPageWithOnlyTextWritesNoBands() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
				graphics.drawString("Text only", 36, 72);
				graphics.setColor(Color.WHITE);
				graphics.fillRect(0, 0, 612, 792); // Painting the page white leaves nothing to print.
			}
		});
		assertTrue("no bit images", result.getBitImages().isEmpty());
		assertEquals("characters", 9, result.getGlyphs().size());
	}

	@Test
	public void textAndGraphicsKeepTheirPlaceWhenMixed() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
				graphics.drawString("B", 36, 288); // Drawn first, printed last.
				graphics.fillRect(72, 144, 72, 36);
				graphics.drawString("A", 36, 72);
			}
		});
		assertEquals("characters", 2, result.getGlyphs().size());
		assertEquals("A baseline", 1.0 + 18 / 72d, result.getGlyphs().get(0).getY() + PrinterProfile.ninePin().getBaselineOffset(), TOLERANCE);
		assertEquals("B baseline", 4.0 + 18 / 72d, result.getGlyphs().get(1).getY() + PrinterProfile.ninePin().getBaselineOffset(), TOLERANCE);
		assertEquals("band top", 2.0 + 18 / 72d, bounds(result)[1], TOLERANCE);
		for (String warning : result.getWarnings()) {
			assertTrue("the printer warns about the bands: " + warning, !warning.contains("graphics") && !warning.contains("unknown command"));
		}
	}

	@Test
	public void theGraphicsPropertyTurnsBandsOff() throws Exception {
		System.setProperty(PropertyKeys.WUBIQ_DOTMATRIX_GRAPHICS, "false");
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.fillRect(72, 144, 144, 36);
				graphics.drawString("Still text", 36, 72);
			}
		});
		assertTrue("no bit images", result.getBitImages().isEmpty());
		assertEquals("characters", 10, result.getGlyphs().size());
	}

	@Test
	public void grayPrintsHalfTheDotsOfBlack() throws Exception {
		EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
			public void draw(Graphics2D graphics) {
				graphics.setColor(Color.GRAY);
				graphics.fillRect(72, 72, 144, 72);
			}
		});
		double full = 2.0 * 120 * 1.0 * 72;
		assertEquals("dots", full * (1 - 128 / 255d), dots(result), full * 0.1);
	}

	@Test
	public void thinLightLinesPrintAsDottedLinesWhateverTheirPositionAndTheAntialiasingHint() throws Exception {
		// What report engines and PDF pages draw: gridlines of light gray, a quarter of a point thick, with antialiasing asked for.
		for (int phase = 0; phase < 4; phase++) {
			final int y = 100 + phase;
			EscpResult result = write(PrinterProfile.ninePin(), null, 0, 0, new Drawing() {
				public void draw(Graphics2D graphics) {
					graphics.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
					graphics.setColor(new Color(179, 179, 179));
					graphics.setStroke(new java.awt.BasicStroke(0.25f));
					graphics.draw(new java.awt.geom.Line2D.Float(72, y, 372, y)); // 5 in: 600 dot columns.
					graphics.draw(new java.awt.geom.Line2D.Float(72, y + 40, 72, y + 140));
				}
			});
			// 30% of the dots of a line one dot thick: 600 columns across, 100 / 72 * 72 dot lines down.
			assertEquals("dots of the lines drawn at y " + y, 0.30 * (600 + 100 * 1.667), dots(result), 0.30 * (600 + 100 * 1.667) * 0.25);
		}
	}

	// ------------------------------------------------------------------ helpers

	private interface Drawing {
		void draw(Graphics2D graphics);
	}

	/**
	 * Writes a one page document, on letter paper with the imageable area 1/4 inch in, and reads it back.
	 * @param printableX Where the printer's printable area starts across the paper, in points.
	 * @param printableY Where it starts down the paper.
	 */
	private EscpResult write(PrinterProfile profile, PrinterType type, float printableX, float printableY,
			final Drawing drawing) throws Exception {
		File file = File.createTempFile("wubiq-graphics", ".escp");
		file.deleteOnExit();
		final PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(18, 18, 576, 756);
		format.setPaper(paper);
		final Printable printable = new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return NO_SUCH_PAGE;
				}
				drawing.draw((Graphics2D) graphics);
				return PAGE_EXISTS;
			}
		};
		Pageable pageable = new Pageable() {
			public int getNumberOfPages() {
				return 1;
			}

			public PageFormat getPageFormat(int pageIndex) {
				return format;
			}

			public Printable getPrintable(int pageIndex) {
				return printable;
			}
		};
		EscpStreamJob job = new EscpStreamJob(file.getPath(), 1, printableX, printableY);
		job.setPrinterType(type);
		job.printAll(pageable, 1);
		return new EscpInterpreter(profile).interpret(read(file));
	}

	/**
	 * @return Left, top, right and bottom, in inches, of the dots of all the bands.
	 */
	private static double[] bounds(EscpResult result) {
		return bounds(result.getBitImages(), 0, 100);
	}

	/**
	 * @return Left, top, right and bottom, in inches, of the dots of the bands whose top is between the two positions.
	 */
	private static double[] bounds(List<BitImage> bands, double from, double to) {
		double[] returnValue = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
		for (BitImage band : bands) {
			if (band.getY() < from || band.getY() >= to) {
				continue;
			}
			for (int column = 0; column < band.getColumns().length; column++) {
				for (int dot = 0; dot < band.getDots(); dot++) {
					if (band.isSet(column, dot)) {
						returnValue[0] = Math.min(returnValue[0], band.getX() + column * band.getColumnWidth());
						returnValue[1] = Math.min(returnValue[1], band.getY() + dot * band.getDotPitch());
						returnValue[2] = Math.max(returnValue[2], band.getX() + (column + 1) * band.getColumnWidth());
						returnValue[3] = Math.max(returnValue[3], band.getY() + (dot + 1) * band.getDotPitch());
					}
				}
			}
		}
		return returnValue;
	}

	private static int dots(EscpResult result) {
		int returnValue = 0;
		for (BitImage band : result.getBitImages()) {
			returnValue += band.getDotCount();
		}
		return returnValue;
	}

	private static byte[] read(File file) throws IOException {
		InputStream stream = new FileInputStream(file);
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int count;
			while ((count = stream.read(chunk)) > 0) {
				buffer.write(chunk, 0, count);
			}
			return buffer.toByteArray();
		} finally {
			stream.close();
		}
	}
}
