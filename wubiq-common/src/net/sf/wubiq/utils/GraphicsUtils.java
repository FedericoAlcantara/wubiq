/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;

import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.enums.PrinterType;

/**
 * Handle fonts issues.
 * @author Federico Alcantara
 *
 */
public enum GraphicsUtils {
	INSTANCE;
	private final String[] FONT_STYLES = {"PLAIN", "BOLD", "ITALIC", "BOLDITALIC"};

	private String osName;
	private String defaultDotMatrixFont;
	private Boolean dotMatrixUseLogicalFonts;
	
	/**
	 * For dot matrix printer transforms the fonts to a more easily printable.
	 * @param originalFont Sent font.
	 * @param printerType Type of printer.
	 * @return Original Font sent or a fixed font. 
	 */
	public Font properFont(Font originalFont, PrinterType printerType){
		Font font = originalFont.deriveFont(originalFont.getStyle());
		
		if (osName == null) {
			osName = System.getProperty("os.name");
		}
		if (defaultDotMatrixFont == null) {
			defaultDotMatrixFont = System.getProperty(PropertyKeys.WUBIQ_FONTS_DOTMATRIX_DEFAULT);
			if (Is.emptyString(defaultDotMatrixFont)) {
				defaultDotMatrixFont = "Serif";
			}
		}
		if (dotMatrixUseLogicalFonts == null) {
			dotMatrixUseLogicalFonts = "TRUE".equalsIgnoreCase(System.getProperty(PropertyKeys.WUBIQ_FONTS_DOTMATRIX_FORCE_LOGICAL));
		}
		if (printerType.equals(PrinterType.DOT_MATRIX) ||
				printerType.equals(PrinterType.DOT_MATRIX_HQ)) {
			if (dotMatrixUseLogicalFonts) {
				font = useDefaultFonts(font);
			} else {
				if (osName.startsWith("Windows")) {
					font = windowsProperFont(font, printerType);
				} else if (osName.startsWith("Linux")) {
					font = linuxProperFont(font, printerType);
				} else if (osName.startsWith("MacOS") ||
						osName.startsWith("Mac OS")) {
					font = macOsProperFont(font, printerType);
				}
			}
		}
		return font;
	}
	
	/**
	 * Forces the use of a java default font.
	 * @param originalFont Original font.
	 * @return Replaced font, will always be SansSerif, Serif or Monospaced.
	 */
	private Font useDefaultFonts(Font originalFont) {
		Font font = originalFont.deriveFont(originalFont.getStyle());
		String fontName = originalFont.getName();
		String style = determineStyle(originalFont);

		// Determine font name
		if (fontName.toLowerCase().contains("arial") ||
				fontName.toLowerCase().contains("helvetica")) {
			fontName = "SansSerif";
			if ("arial bold".equalsIgnoreCase(fontName)
				|| "arialMT".equalsIgnoreCase(originalFont.getFontName())
				|| "helvetica-bold".equalsIgnoreCase(originalFont.getFontName())) {
				style = FONT_STYLES[(originalFont.getStyle() | Font.BOLD)];
			}
		} else if (fontName.toLowerCase().contains("times") ||
				fontName.toLowerCase().contains("georgia")) {
			fontName = "Serif";
		} else if (fontName.toLowerCase().contains("courier new")) {
			fontName = "Monospaced";
		} else if (fontName.toLowerCase().contains("comic sans") ||
				fontName.toLowerCase().contains("tahoma") ||
				fontName.toLowerCase().contains("verdana")) {
			fontName = "SansSerif";
		} else {
			fontName = defaultDotMatrixFont;
		}
		String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
		font = Font.decode(decodeString);
		return font;
	}
	
	/**
	 * For dot matrix printer transforms the fonts to a more easily printable if the system is windows.
	 * @param originalFont Sent font.
	 * @param printerType Type of printer.
	 * @return Original Font sent or a fixed font. 
	 */
	private Font windowsProperFont(Font originalFont, PrinterType printerType) {
		Font font = originalFont.deriveFont(originalFont.getStyle());
		String fontName = originalFont.getName();
		String style = determineStyle(originalFont);
		
		if (fontName.toLowerCase().contains("arial") ||
				fontName.toLowerCase().contains("helvetica")) {
			if ("arial bold".equalsIgnoreCase(fontName)
				|| "arialMT".equalsIgnoreCase(originalFont.getFontName())
				|| "helvetica-bold".equalsIgnoreCase(originalFont.getFontName())) {
				fontName = "SansSerif";
				style = FONT_STYLES[(originalFont.getStyle() | Font.BOLD)];
			} else {
				fontName = "SansSerif";
			}
			String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
			font = Font.decode(decodeString);
		} else if (fontName.toLowerCase().contains("times")) {
			fontName = "Serif";
			String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
			font = Font.decode(decodeString);
		} else if (fontName.toLowerCase().contains("courier new")) {
			fontName = "Monospaced";
			String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
			font = Font.decode(decodeString);
		} else if (fontName.toLowerCase().contains("verdana")) {
			fontName = "SansSerif";
			String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
			font = Font.decode(decodeString);
		}
		return font;
	}

	/**
	 * For dot matrix printer transforms the fonts to a more easily printable if the system is linux based.
	 * @param originalFont Sent font.
	 * @param printerType Type of printer.
	 * @return Original Font sent or a fixed font. 
	 */
	private Font linuxProperFont(Font originalFont, PrinterType printerType) {
		Font font = originalFont;
		return font;
	}
	
	/**
	 * For dot matrix printer transforms the fonts to a more easily printable if the system is osx (Mac).
	 * @param originalFont Sent font.
	 * @param printerType Type of printer.
	 * @return Original Font sent or a fixed font. 
	 */
	private Font macOsProperFont(Font originalFont, PrinterType printerType) {
		Font font = originalFont;
		String fontName = originalFont.getName();
		String style = FONT_STYLES [originalFont.getStyle()];
		if (fontName.toLowerCase().contains("helvetica")) {
			if ("helvetica-bold".equalsIgnoreCase(originalFont.getFontName())) {
				style = FONT_STYLES[(originalFont.getStyle() | Font.BOLD)];
			} else if ("helvetica-light".equalsIgnoreCase(originalFont.getFontName())) {
				style = FONT_STYLES[Font.PLAIN];
			}
			fontName = "Helvetica";
			String decodeString = fontName + "-" + style + "-" + originalFont.getSize();
			font = Font.decode(decodeString);
		}
		return font;
	}	
	/**
	 * Creates a transformation with scaled graphics.
	 * @param graph Graphics environment.
	 * @param pageFormat Page format.
	 * @return x and y scales.
	 */
	public Point2D scaleGraphics(Graphics2D graph, PageFormat pageFormat, boolean noScale) {
		int xOffset = 0; // Arbitrary;
		int yOffset = 0; // Arbitrary;

		AffineTransform newScale = new AffineTransform();
		
		double x = graph.getDeviceConfiguration().getBounds().getWidth()
				/ (pageFormat.getWidth() + xOffset);
		double y = graph.getDeviceConfiguration().getBounds().getHeight()
				/ (pageFormat.getHeight() + yOffset);
		// If we don't have a device information, we must scale based on page format / paper information.
		if (x <= 0 && y <= 0) { 
			x = pageFormat.getWidth() / (pageFormat.getImageableWidth());
			y = pageFormat.getHeight() / (pageFormat.getImageableHeight());
		} else {
			newScale.scale(x, y);
		}
		if (!noScale) {
			graph.setTransform(newScale);
		}
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Determine the font style.
	 * @param originalFont Original font.
	 * @return Style of the original font.
	 */
	private String determineStyle(Font originalFont) {
		String fontName = originalFont.getName();
		String style = FONT_STYLES[originalFont.getStyle()];
		// Determine style
		if (fontName.toLowerCase().contains("bold")) {
			if (fontName.toLowerCase().contains("italic")) {
				style = FONT_STYLES[3];
			} else {
				style = FONT_STYLES[1];
			}
		} else if (fontName.toLowerCase().contains("italic")) {
			style = FONT_STYLES[2];
		}
		return style;
	}
}
