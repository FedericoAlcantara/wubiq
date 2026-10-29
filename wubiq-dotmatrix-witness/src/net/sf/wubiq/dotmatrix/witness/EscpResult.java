package net.sf.wubiq.dotmatrix.witness;

import java.util.ArrayList;
import java.util.List;

/**
 * What the virtual dot-matrix printer produced for a byte stream.
 * @author Federico Alcantara
 *
 */
public class EscpResult {
	private final List<Glyph> glyphs = new ArrayList<Glyph>();
	private final List<String> warnings = new ArrayList<String>();
	private final List<String> dump = new ArrayList<String>();
	private int pageCount;
	private int formFeeds;

	/**
	 * @return Every printed cell, spaces included, in emission order.
	 */
	public List<Glyph> getGlyphs() {
		return glyphs;
	}

	/**
	 * @return Glyphs on the given sheet, in emission order.
	 */
	public List<Glyph> getGlyphs(int page) {
		List<Glyph> returnValue = new ArrayList<Glyph>();
		for (Glyph glyph : glyphs) {
			if (glyph.getPage() == page) {
				returnValue.add(glyph);
			}
		}
		return returnValue;
	}

	/**
	 * @return Anything a real printer would do differently from what the stream intends,
	 * plus commands the witness does not understand.
	 */
	public List<String> getWarnings() {
		return warnings;
	}

	/**
	 * @return Decoded command listing, one command or text chunk per line.
	 */
	public List<String> getDump() {
		return dump;
	}

	/**
	 * @return Number of physical sheets touched (at least 1).
	 */
	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getFormFeeds() {
		return formFeeds;
	}

	public void setFormFeeds(int formFeeds) {
		this.formFeeds = formFeeds;
	}

	public String getDumpText() {
		StringBuilder builder = new StringBuilder();
		for (String line : dump) {
			builder.append(line).append('\n');
		}
		return builder.toString();
	}
}
