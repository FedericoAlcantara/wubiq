package net.sf.wubiq.dotmatrix.witness;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

/**
 * Writes everything the witness knows about one job into a directory:
 * <ul>
 * <li><code>job.escp</code> raw bytes</li>
 * <li><code>dump.txt</code> decoded command listing</li>
 * <li><code>grid.txt</code> text picture of the sheets (golden file friendly)</li>
 * <li><code>lines.json</code> printed text runs, for checks written in any language</li>
 * <li><code>sheet-N.png</code> rendered sheets</li>
 * <li><code>check.txt</code> layout check, when expectations were given</li>
 * <li><code>index.html</code> all of the above on one page</li>
 * </ul>
 * @author Federico Alcantara
 *
 */
public class WitnessReport {
	private final PrinterProfile profile;
	private final int dpi;

	public WitnessReport(PrinterProfile profile, int dpi) {
		this.profile = profile;
		this.dpi = dpi;
	}

	public WitnessReport(PrinterProfile profile) {
		this(profile, 100);
	}

	/**
	 * @param directory Target directory, created if needed.
	 * @param title Job title for the html page.
	 * @param data Raw stream.
	 * @param result Interpreter output for data.
	 * @param references Optional reference renderings per sheet at this report's dpi.
	 * @param check Optional layout check.
	 */
	public void write(File directory, String title, byte[] data, EscpResult result,
			List<BufferedImage> references, CheckResult check) throws IOException {
		if (!directory.isDirectory() && !directory.mkdirs()) {
			throw new IOException("Can't create " + directory);
		}
		write(new File(directory, "job.escp"), data);
		write(new File(directory, "dump.txt"), result.getDumpText() + warningsText(result));
		String grid = TextGrid.render(result, profile);
		write(new File(directory, "grid.txt"), grid);
		write(new File(directory, "lines.json"), json(result));
		if (check != null) {
			write(new File(directory, "check.txt"), check.toString());
		}
		List<BufferedImage> images = new PageRenderer(profile, dpi).render(result, references);
		for (int index = 0; index < images.size(); index++) {
			ImageIO.write(images.get(index), "png", new File(directory, "sheet-" + (index + 1) + ".png"));
		}
		write(new File(directory, "index.html"), html(title, result, check, grid, images.size()));
	}

	/**
	 * Writes an html page linking several job reports.
	 * @param file Index file.
	 * @param title Page title.
	 * @param rows Each row: name, relative link to the job's index.html, status, details.
	 */
	public static void writeIndex(File file, String title, List<String[]> rows) throws IOException {
		StringBuilder html = new StringBuilder();
		html.append(head(title));
		html.append("<h1>").append(escape(title)).append("</h1>\n<table>\n");
		html.append("<tr><th>Job</th><th>Status</th><th>Details</th></tr>\n");
		for (String[] row : rows) {
			String status = row[2] == null ? "" : row[2];
			html.append("<tr><td><a href=\"").append(escape(row[1])).append("\">").append(escape(row[0]))
					.append("</a></td><td class=\"").append(status.startsWith("PASS") ? "pass" : "fail").append("\">")
					.append(escape(status)).append("</td><td>").append(escape(row[3] == null ? "" : row[3]))
					.append("</td></tr>\n");
		}
		html.append("</table>\n</body></html>\n");
		File parent = file.getAbsoluteFile().getParentFile();
		if (parent != null && !parent.isDirectory()) {
			parent.mkdirs();
		}
		write(file, html.toString());
	}

	private String warningsText(EscpResult result) {
		StringBuilder builder = new StringBuilder("\nWarnings: ").append(result.getWarnings().size()).append('\n');
		for (String warning : result.getWarnings()) {
			builder.append(warning).append('\n');
		}
		return builder.toString();
	}

	private String json(EscpResult result) {
		StringBuilder json = new StringBuilder("{\n");
		json.append("  \"profile\": {\"pins\": ").append(profile.isPin24() ? 24 : 9)
				.append(", \"paperWidth\": ").append(profile.getPaperWidth())
				.append(", \"paperHeight\": ").append(profile.getPaperHeight())
				.append(", \"carriageWidth\": ").append(profile.getCarriageWidth())
				.append(", \"baselineOffset\": ").append(number(profile.getBaselineOffset())).append("},\n");
		json.append("  \"sheets\": ").append(result.getPageCount()).append(",\n");
		json.append("  \"formFeeds\": ").append(result.getFormFeeds()).append(",\n");
		json.append("  \"graphics\": [");
		for (int page = 0; page < result.getPageCount(); page++) {
			List<BitImage> bitImages = result.getBitImages(page);
			int dots = 0;
			for (BitImage bitImage : bitImages) {
				dots += bitImage.getDotCount();
			}
			json.append(page == 0 ? "\n    " : ",\n    ").append("{\"sheet\": ").append(page + 1)
					.append(", \"bands\": ").append(bitImages.size()).append(", \"dots\": ").append(dots).append('}');
		}
		json.append(result.getPageCount() == 0 ? "],\n" : "\n  ],\n");
		json.append("  \"warnings\": [");
		for (int index = 0; index < result.getWarnings().size(); index++) {
			json.append(index == 0 ? "\n    " : ",\n    ").append(quote(result.getWarnings().get(index)));
		}
		json.append(result.getWarnings().isEmpty() ? "],\n" : "\n  ],\n");
		json.append("  \"runs\": [");
		boolean first = true;
		for (TextLine line : TextLine.of(result)) {
			Glyph start = null;
			StringBuilder text = new StringBuilder();
			Glyph previous = null;
			for (Glyph glyph : line.getGlyphs()) {
				if (start != null && !sameRun(previous, glyph)) {
					json.append(first ? "\n    " : ",\n    ").append(run(start, text.toString()));
					first = false;
					start = null;
				}
				if (start == null) {
					start = glyph;
					text.setLength(0);
				}
				text.append(glyph.getCharacter());
				previous = glyph;
			}
			if (start != null) {
				json.append(first ? "\n    " : ",\n    ").append(run(start, text.toString()));
				first = false;
			}
		}
		json.append(first ? "]\n" : "\n  ]\n");
		json.append("}\n");
		return json.toString();
	}

	private boolean sameRun(Glyph previous, Glyph glyph) {
		return Math.abs(previous.getX() + previous.getWidth() - glyph.getX()) < 0.0005
				&& previous.getCpi() == glyph.getCpi() && previous.isBold() == glyph.isBold()
				&& previous.isItalic() == glyph.isItalic() && previous.isUnderline() == glyph.isUnderline();
	}

	private String run(Glyph start, String text) {
		return "{\"sheet\": " + (start.getPage() + 1) + ", \"x\": " + number(start.getX()) + ", \"y\": " + number(start.getY())
				+ ", \"baseline\": " + number(start.getY() + profile.getBaselineOffset())
				+ ", \"cpi\": " + number(start.getCpi()) + ", \"bold\": " + start.isBold() + ", \"italic\": " + start.isItalic()
				+ ", \"underline\": " + start.isUnderline() + ", \"text\": " + quote(text) + "}";
	}

	private String number(double value) {
		return String.format(Locale.US, "%.4f", value);
	}

	private String quote(String value) {
		StringBuilder builder = new StringBuilder("\"");
		for (char character : value.toCharArray()) {
			if (character == '"' || character == '\\') {
				builder.append('\\').append(character);
			} else if (character < 0x20 || character > 0x7E) {
				builder.append(String.format(Locale.US, "\\u%04x", (int) character));
			} else {
				builder.append(character);
			}
		}
		return builder.append('"').toString();
	}

	private String html(String title, EscpResult result, CheckResult check, String grid, int sheets) {
		StringBuilder html = new StringBuilder();
		html.append(head(title));
		html.append("<h1>").append(escape(title)).append("</h1>\n");
		html.append("<p>").append(escape(profile.toString())).append(" &middot; ").append(result.getPageCount())
				.append(" sheet(s) &middot; ").append(result.getFormFeeds()).append(" form feed(s) &middot; ")
				.append("<a href=\"dump.txt\">dump</a> &middot; <a href=\"lines.json\">lines.json</a> &middot; ")
				.append("<a href=\"job.escp\">job.escp</a></p>\n");
		if (check != null) {
			html.append("<h2 class=\"").append(check.isPassed() ? "pass" : "fail").append("\">Layout check: ")
					.append(escape(check.getSummary())).append("</h2>\n<table>\n");
			for (CheckResult.Status status : new CheckResult.Status[] {CheckResult.Status.FAIL, CheckResult.Status.NOTE, CheckResult.Status.PASS}) {
				for (CheckResult.Item item : check.getItems()) {
					if (item.getStatus() == status) {
						html.append("<tr><td class=\"").append(status.name().toLowerCase()).append("\">").append(status)
								.append("</td><td>").append(escape(item.getSubject())).append("</td><td>")
								.append(escape(item.getMessage())).append("</td></tr>\n");
					}
				}
			}
			html.append("</table>\n");
		}
		if (!result.getWarnings().isEmpty()) {
			html.append("<h2>Printer warnings</h2>\n<ul>\n");
			for (String warning : result.getWarnings()) {
				html.append("<li>").append(escape(warning)).append("</li>\n");
			}
			html.append("</ul>\n");
		}
		html.append("<h2>Text grid</h2>\n<pre>").append(escape(grid)).append("</pre>\n");
		html.append("<h2>Sheets</h2>\n<p class=\"legend\">Blue: dot-matrix text. Black: bit-image graphics, dot by dot. Red: reference rendering, when given. "
				+ "Grid: 1/10 in columns, 1 in squares. Orange: end of carriage.</p>\n");
		for (int index = 1; index <= sheets; index++) {
			html.append("<figure><img src=\"sheet-").append(index).append(".png\" alt=\"sheet ").append(index)
					.append("\"><figcaption>Sheet ").append(index).append("</figcaption></figure>\n");
		}
		html.append("</body></html>\n");
		return html.toString();
	}

	private static String head(String title) {
		return "<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>" + escape(title) + "</title>\n"
				+ "<style>body{font-family:sans-serif;margin:16px;background:#fff;color:#111}"
				+ "pre{font-size:12px;background:#f6f6f6;padding:8px;overflow:auto}"
				+ "td,th{border-bottom:1px solid #ddd;padding:3px 8px;text-align:left;vertical-align:top;font-size:13px}"
				+ "table{border-collapse:collapse}.pass{color:#176b2c}.fail{color:#b00020}.note{color:#8a5a00}"
				+ "img{max-width:100%;border:1px solid #ccc}figure{margin:12px 0}.legend{color:#555}</style>\n"
				+ "</head><body>\n";
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private static void write(File file, String text) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			writer.write(text);
		} finally {
			writer.close();
		}
	}

	private static void write(File file, byte[] data) throws IOException {
		OutputStream stream = new FileOutputStream(file);
		try {
			stream.write(data);
		} finally {
			stream.close();
		}
	}
}
