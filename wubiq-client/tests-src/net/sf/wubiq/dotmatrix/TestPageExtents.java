package net.sf.wubiq.dotmatrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;

/**
 * Reads the text extents of a PDF: one row per text run, with its sheet (from
 * 0), its left and right edges and its baseline, in inches from the top left
 * corner of the sheet, and its text. Writes them as the expected file of the
 * witness's {@code extents} command.
 * @author Federico Alcantara
 *
 */
public class TestPageExtents {

	/**
	 * Reads the text runs of a PDF.
	 * @param pdf The PDF stream.
	 * @return One row per text run: {@code sheet<TAB>left<TAB>right<TAB>baseline<TAB>text},
	 * inches with three decimals.
	 * @throws IOException If the stream is not a readable PDF.
	 */
	public static List<String> extract(InputStream pdf) throws IOException {
		final List<String> rows = new ArrayList<String>();
		PDDocument document = PDDocument.load(pdf);
		try {
			PDFTextStripper stripper = new PDFTextStripper() {
				int page = -1;

				@Override
				protected void startPage(PDPage pdPage) throws IOException {
					page += 1;
					super.startPage(pdPage);
				}

				@Override
				protected void writeString(String text, List<TextPosition> positions) throws IOException {
					TextPosition first = positions.get(0);
					TextPosition last = positions.get(positions.size() - 1);
					rows.add(String.format(Locale.US, "%d\t%.3f\t%.3f\t%.3f\t%s", page,
							first.getXDirAdj() / 72,
							(last.getXDirAdj() + last.getWidthDirAdj()) / 72,
							first.getYDirAdj() / 72, text));
				}
			};
			stripper.setSortByPosition(true);
			stripper.getText(document);
		} finally {
			document.close();
		}
		return rows;
	}

	/**
	 * Writes the extents file of a PDF.
	 * @param args The PDF, then the file to write.
	 * @throws IOException If the PDF cannot be read or the file cannot be written.
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("Usage: TestPageExtents PDF OUTPUT");
			System.exit(2);
		}
		File pdfFile = new File(args[0]);
		List<String> rows;
		InputStream input = new FileInputStream(pdfFile);
		try {
			rows = extract(input);
		} finally {
			input.close();
		}
		Writer writer = new OutputStreamWriter(new FileOutputStream(args[1]), "UTF-8");
		try {
			writer.write("# sheet\tleft\tright\tbaseline\ttext (inches), from " + pdfFile.getName() + "\n");
			for (String row : rows) {
				writer.write(row + "\n");
			}
		} finally {
			writer.close();
		}
	}
}
