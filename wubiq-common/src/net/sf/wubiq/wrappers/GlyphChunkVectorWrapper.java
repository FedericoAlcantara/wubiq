/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.awt.font.GlyphVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Federico Alcantara
 *
 */
public class GlyphChunkVectorWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private Font font;
	private transient static String utfCharacters = null;
	private transient static Map<Font, Map<String, Character>> charactersMap;
	
	private char[] characters;
	
	public GlyphChunkVectorWrapper() {
	}

	public GlyphChunkVectorWrapper(GlyphVector glyphVector) {
		this();
		if (glyphVector != null) {
			glyphVector.performDefaultLayout();
			this.font = glyphVector.getFont();
			initializeGlyphs(glyphVector);
		}
	}

	private void initializeGlyphs(GlyphVector glyphVector) {
		parseFont(glyphVector);
		int[]  codes = new int[glyphVector.getNumGlyphs()];
		for (int glyphIndex = 0; glyphIndex < codes.length; glyphIndex++) {
			codes[glyphIndex] = glyphVector.getGlyphCode(glyphIndex);
		}
		createCharacterCodes(codes);
	}
	

	/**
	 * @see java.awt.font.GlyphVector#getFont()
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GlyphVectorWrapper [font=" + font + "]";
	}
	
	public String getUtfCharacters() {
		if (utfCharacters == null) {
			StringBuffer utf = new StringBuffer("");
			char character = '\u0020';
			do {
				utf.append(character++);
			} while (character <=  '\u007e');
			
			character = '\u00A1';
			do {
				utf.append(character++);
			} while (character <=  '\u00FF');
			utfCharacters = utf.toString();
		}
		return GlyphChunkVectorWrapper.utfCharacters;
	}
	
	/**
	 * Parses the glyphVector and creates a map of glyphs and characters.
	 * @param glyphVector glyphVector to analyze and parse its characters.
	 */
	private void parseFont(GlyphVector glyphVector) {
		if (charactersMap == null) {
			charactersMap = new HashMap<Font, Map<String, Character>>();
		}
		Map<String, Character> characterMap = charactersMap.get(glyphVector.getFont());
		if (characterMap == null) {
			characterMap = new HashMap<String, Character>();
			for (int index = 0; index < getUtfCharacters().length(); index++) {
				char charAt = getUtfCharacters().charAt(index);
				GlyphVector vector = glyphVector.getFont().createGlyphVector(glyphVector.getFontRenderContext(), new char[]{charAt});
				int[] vectorCodes = new int[vector.getNumGlyphs()];
				for (int glyphIndex = 0; glyphIndex < vectorCodes.length; glyphIndex++) {
					vectorCodes[glyphIndex] = vector.getGlyphCode(glyphIndex);
				}
				characterMap.put(intArrayToString(vectorCodes), charAt);
			}
			charactersMap.put(glyphVector.getFont(), characterMap);
		}
	}
	
	/**
	 * Creates the set of characters.
	 * @param codes given codes.
	 */
	public void createCharacterCodes(int[] codes) {
		List<Character> converted = new ArrayList<Character>();
		Map<String, Character> characterMap = charactersMap.get(getFont());
		if (characterMap != null) {
			Character character = characterMap.get(intArrayToString(codes));
			if (character != null) {
				converted.add(character);
			} else { // might be a sequence of letters
				for (int code : codes) {
					character = characterMap.get(intArrayToString(new int[]{code}));
					if (character != null) {
						converted.add(character);
					}
				}
			}
		}
		characters = new char[converted.size()];
		for (int index = 0; index < characters.length; index++) {
			characters[index] = converted.get(index);
		}
	}
	
	public char[] getCharacters() {
		return characters;
	}
	
	/**
	 * Makes a string representation of a integer array.
	 * @param codes Integer array.
	 * @return To string representation.
	 */
	private String intArrayToString(int[] codes) {
		StringBuffer returnValue = new StringBuffer("");
		for (int index = 0; index < codes.length; index++) {
			if (returnValue.length() > 0) {
				returnValue.append(',');
			}
			returnValue.append(codes[index]);
		}
		returnValue.insert(0, '[');
		returnValue.append(']');
		return returnValue.toString();
	}
}
