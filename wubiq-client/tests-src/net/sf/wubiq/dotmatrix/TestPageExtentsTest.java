package net.sf.wubiq.dotmatrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

/**
 * The text extents read from a PDF: where each text run starts, ends and sits,
 * in inches, and on which sheet.
 * @author Federico Alcantara
 *
 */
public class TestPageExtentsTest {

	@Test
	public void extractsTheExtentsOfATextRun() throws Exception {
		List<String> rows = TestPageExtents.extract(new ByteArrayInputStream(pdf("Hello")));
		String[] f = rows.get(0).split("\t");
		assertEquals(1, rows.size());
		assertEquals("0", f[0]);
		assertEquals("1.000", f[1]);
		assertEquals(1.38, Double.parseDouble(f[2]), 0.01);
		assertEquals("1.000", f[3]);
		assertEquals("Hello", f[4]);
	}

	@Test
	public void sheetsAreNumberedFromZero() throws Exception {
		List<String> rows = TestPageExtents.extract(new ByteArrayInputStream(pdf("One", "Two")));
		assertTrue(rows.get(0).startsWith("0\t"));
		assertTrue(rows.get(1).startsWith("1\t"));
	}

	@Test
	public void aStreamThatIsNotAPdfIsRefused() throws Exception {
		try {
			TestPageExtents.extract(new ByteArrayInputStream("not a pdf".getBytes("UTF-8")));
			fail();
		} catch (IOException expected) {
		}
	}

	/**
	 * A US Letter PDF with one page per text, each drawn in Helvetica 12 at 72
	 * points from the left and 720 points from the bottom.
	 */
	private byte[] pdf(String... texts) throws Exception {
		PDDocument document = new PDDocument();
		for (String text : texts) {
			PDPage page = new PDPage();
			document.addPage(page);
			PDPageContentStream content = new PDPageContentStream(document, page);
			content.beginText();
			content.setFont(PDType1Font.HELVETICA, 12);
			content.moveTextPositionByAmount(72, 720);
			content.drawString(text);
			content.endText();
			content.close();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		document.save(out);
		document.close();
		return out.toByteArray();
	}
}
