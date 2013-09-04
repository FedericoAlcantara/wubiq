/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.awt.font.GlyphVector;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class GlyphVectorWrapper  implements Serializable {
	private static final long serialVersionUID = 1L;
	private Font font;
	private int[] codes;
	
	public GlyphVectorWrapper() {
	}

	public GlyphVectorWrapper(GlyphVector glyphVector) {
		this();
		if (glyphVector != null) {
			glyphVector.performDefaultLayout();
			this.font = glyphVector.getFont();
			initializeGlyphs(glyphVector);
		}
	}

	private void initializeGlyphs(GlyphVector glyphVector) {
		codes = new int[glyphVector.getNumGlyphs()];
		for (int glyphIndex = 0; glyphIndex < codes.length; glyphIndex++) {
			codes[glyphIndex] = glyphVector.getGlyphCode(glyphIndex);
		}
	}
	

	/**
	 * @see java.awt.font.GlyphVector#getFont()
	 */
	public Font getFont() {
		return font;
	}

	public int[] getCodes() {
		return codes;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GlyphVectorWrapper [font=" + font + "]";
	}
}
