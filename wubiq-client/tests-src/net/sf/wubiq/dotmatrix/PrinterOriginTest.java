package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertEquals;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.wubiq.dotmatrix.witness.EscpInterpreter;
import net.sf.wubiq.dotmatrix.witness.EscpResult;
import net.sf.wubiq.dotmatrix.witness.Glyph;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;
import net.sf.wubiq.wrappers.EscpStreamJob;

import org.junit.Test;

/**
 * Where an ESC/P stream puts the application's text, measured by the virtual printer, when the page
 * format has an imageable area (18 points on each side, like a CUPS or Windows dot-matrix driver gives)
 * and the printer's printable area is or isn't known.
 * <p>The text is drawn at 36, 72 points of the imageable area. The printer counts from where it
 * prints, so:</p>
 * <ul>
 * <li>if it prints from the imageable area's position, the text is 36, 72 points from there;</li>
 * <li>if it prints from the paper's edge, the text is 18 points further each way;</li>
 * <li>if the printable area isn't known, the printer is taken to print from the imageable area.</li>
 * </ul>
 * @author Federico Alcantara
 *
 */
public class PrinterOriginTest {
	private static final PrinterProfile PROFILE = PrinterProfile.ninePin();
	private static final double TOLERANCE = 0.02; // Inches.

	@Test
	public void printerPrintingFromTheImageableAreaGetsTheApplicationsPosition() throws Exception {
		assertPosition(print(18, 18), 0.5, 1.0);
	}

	@Test
	public void printerPrintingFromThePaperEdgeGetsTheImageableAreaPositionAdded() throws Exception {
		assertPosition(print(0, 0), 0.75, 1.25);
	}

	@Test
	public void printableAreaFurtherInThanTheImageableAreaMovesTheTextBack() throws Exception {
		assertPosition(print(36, 36), 0.25, 0.75);
	}

	@Test
	public void unknownPrintableAreaTakesThePrinterToPrintFromTheImageableArea() throws Exception {
		assertPosition(print(Float.NaN, Float.NaN), 0.5, 1.0);
	}

	@Test
	public void offsetPropertiesMoveTheText() throws Exception {
		System.setProperty("wubiq.dotmatrix.offset.x", "0.1");
		System.setProperty("wubiq.dotmatrix.offset.y", "-0.05");
		try {
			assertPosition(print(18, 18), 0.6, 0.95);
		} finally {
			System.clearProperty("wubiq.dotmatrix.offset.x");
			System.clearProperty("wubiq.dotmatrix.offset.y");
		}
	}

	/**
	 * Prints an "A" at 36, 72 of a page whose imageable area starts at 18, 18.
	 * @return The character the virtual printer printed.
	 */
	private Glyph print(float printableX, float printableY) throws Exception {
		File file = File.createTempFile("wubiq-origin", ".escp");
		file.deleteOnExit();
		PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(18, 18, 576, 756);
		format.setPaper(paper);
		Printable printable = new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex > 0) {
					return NO_SUCH_PAGE;
				}
				graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
				graphics.drawString("A", 36, 72);
				return PAGE_EXISTS;
			}
		};
		EscpStreamJob job = new EscpStreamJob(file.getPath(), 1, printableX, printableY);
		BufferedImage image = new BufferedImage(612, 792, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D graphics = image.createGraphics();
		try {
			assertEquals(Printable.PAGE_EXISTS, job.print(printable, graphics, format, 0));
		} finally {
			graphics.dispose();
		}
		EscpResult result = new EscpInterpreter(PROFILE).interpret(read(file));
		List<Glyph> glyphs = result.getGlyphs();
		assertEquals("characters printed", 1, glyphs.size());
		return glyphs.get(0);
	}

	private void assertPosition(Glyph glyph, double x, double baseline) {
		assertEquals("x", x, glyph.getX(), TOLERANCE);
		assertEquals("baseline", baseline, glyph.getY() + PROFILE.getBaselineOffset(), TOLERANCE);
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
