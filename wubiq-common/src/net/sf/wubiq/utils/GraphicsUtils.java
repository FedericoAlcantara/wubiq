/**
 * 
 */
package net.sf.wubiq.utils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;

import net.sf.wubiq.enums.PrinterType;

/**
 * Handle fonts issues.
 * @author Federico Alcantara
 *
 */
public enum GraphicsUtils {
	INSTANCE;
	private final String[] FONT_STYLES = {"PLAIN", "BOLD", "ITALIC", "BOLDITALIC"};

	private Boolean isWindows;
	
	public Font properFont(Font originalFont, PrinterType printerType){
		if (isWindows == null) {
			String os = System.getProperty("os.name");
			if (os.startsWith("Windows")) {
				isWindows = true;
			} else {
				isWindows = false;
			}
		}
		if (isWindows) {
			if (printerType.equals(PrinterType.DOT_MATRIX) ||
					printerType.equals(PrinterType.DOT_MATRIX_HQ)) {
				String fontName = originalFont.getName();
				String style = FONT_STYLES [originalFont.getStyle()];
				if (fontName.toLowerCase().contains("arial") ||
						fontName.toLowerCase().contains("helvetica")) {
					Font font = originalFont.deriveFont(originalFont.getStyle());
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
					return font;
				}
			}
		}
		return originalFont;
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
}
