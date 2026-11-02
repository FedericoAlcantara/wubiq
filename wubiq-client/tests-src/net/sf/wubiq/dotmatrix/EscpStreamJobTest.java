package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertEquals;

import java.awt.Font;
import java.awt.Graphics;
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

import net.sf.wubiq.dotmatrix.witness.EscpInterpreter;
import net.sf.wubiq.dotmatrix.witness.Glyph;
import net.sf.wubiq.dotmatrix.witness.PrinterProfile;
import net.sf.wubiq.wrappers.EscpStreamJob;

import org.junit.Test;

/**
 * A whole document written to an ESC/P stream with no JDK print job: every page of every copy is
 * written once, in order, with the imageable area's position.
 * @author Federico Alcantara
 *
 */
public class EscpStreamJobTest {
	private static final PrinterProfile PROFILE = PrinterProfile.ninePin();

	@Test
	public void everyPageOfEveryCopyIsWritten() throws Exception {
		List<Glyph> glyphs = write(2, 2, 18, 18);
		assertEquals("characters printed", 4, glyphs.size());
	}

	@Test
	public void aSinglePageIsWrittenOncePerCopy() throws Exception {
		List<Glyph> glyphs = write(1, 3, 18, 18);
		assertEquals("characters printed", 3, glyphs.size());
	}

	@Test
	public void unknownNumberOfPagesEndsWithTheFirstMissingPage() throws Exception {
		List<Glyph> glyphs = write(Pageable.UNKNOWN_NUMBER_OF_PAGES, 1, 18, 18);
		assertEquals("characters printed", 2, glyphs.size());
	}

	@Test
	public void pagesAreDrawnFromTheImageableArea() throws Exception {
		Glyph glyph = write(1, 1, 18, 18).get(0);
		assertEquals("x", 0.5, glyph.getX(), 0.02);
		assertEquals("baseline", 1.0, glyph.getY() + PROFILE.getBaselineOffset(), 0.02);
	}

	/**
	 * Writes a document whose pages hold an "A" at 36, 72 of the imageable area.
	 * @param pages Number of pages, or {@link Pageable#UNKNOWN_NUMBER_OF_PAGES} for a document of two pages that doesn't say so.
	 */
	private List<Glyph> write(final int pages, int copies, float printableX, float printableY) throws Exception {
		File file = File.createTempFile("wubiq-stream", ".escp");
		file.deleteOnExit();
		final int real = pages == Pageable.UNKNOWN_NUMBER_OF_PAGES ? 2 : pages;
		final PageFormat format = new PageFormat();
		Paper paper = new Paper();
		paper.setSize(612, 792);
		paper.setImageableArea(18, 18, 576, 756);
		format.setPaper(paper);
		final Printable printable = new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex >= real) {
					return NO_SUCH_PAGE;
				}
				graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
				graphics.drawString("A", 36, 72);
				return PAGE_EXISTS;
			}
		};
		Pageable pageable = new Pageable() {
			public int getNumberOfPages() {
				return pages;
			}

			public PageFormat getPageFormat(int pageIndex) {
				return format;
			}

			public Printable getPrintable(int pageIndex) {
				return printable;
			}
		};
		new EscpStreamJob(file.getPath(), pages, printableX, printableY).printAll(pageable, copies);
		return new EscpInterpreter(PROFILE).interpret(read(file)).getGlyphs();
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
