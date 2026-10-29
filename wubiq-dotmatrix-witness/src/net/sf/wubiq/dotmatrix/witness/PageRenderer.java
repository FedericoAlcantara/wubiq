package net.sf.wubiq.dotmatrix.witness;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the printed sheets as images: a light inch/column grid, an optional
 * reference rendering (what a laser printer would print) tinted red underneath,
 * and the dot-matrix characters in dark blue on top, at their real cell positions.
 * @author Federico Alcantara
 *
 */
public class PageRenderer {
	private static final Color GRID_COLUMN = new Color(238, 238, 246);
	private static final Color GRID_INCH = new Color(205, 205, 225);
	private static final Color REFERENCE = new Color(220, 40, 40);
	private static final Color INK = new Color(10, 30, 120);
	private final PrinterProfile profile;
	private final int dpi;

	public PageRenderer(PrinterProfile profile, int dpi) {
		this.profile = profile;
		this.dpi = dpi;
	}

	/**
	 * @param result Interpreter output.
	 * @param references Reference renderings per sheet at this renderer's dpi (may be null or shorter).
	 * @return One image per sheet.
	 */
	public List<BufferedImage> render(EscpResult result, List<BufferedImage> references) {
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (int page = 0; page < result.getPageCount(); page++) {
			BufferedImage reference = references != null && page < references.size() ? references.get(page) : null;
			images.add(render(result, page, reference));
		}
		return images;
	}

	public BufferedImage render(EscpResult result, int page, BufferedImage reference) {
		int width = (int) Math.ceil(profile.getPaperWidth() * dpi);
		int height = (int) Math.ceil(profile.getPaperHeight() * dpi);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, width, height);
			drawGrid(graphics, width, height);
			if (reference != null) {
				graphics.drawImage(tint(reference, width, height), 0, 0, null);
			}
			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setColor(INK);
			for (Glyph glyph : result.getGlyphs(page)) {
				drawGlyph(graphics, glyph);
			}
		} finally {
			graphics.dispose();
		}
		return image;
	}

	private void drawGrid(Graphics2D graphics, int width, int height) {
		for (int column = 0; column * dpi / 10d < width; column++) {
			int position = (int) Math.round(column * dpi / 10d);
			graphics.setColor(column % 10 == 0 ? GRID_INCH : GRID_COLUMN);
			graphics.drawLine(position, 0, position, height);
		}
		graphics.setColor(GRID_INCH);
		for (int inch = 0; inch * dpi < height; inch++) {
			graphics.drawLine(0, inch * dpi, width, inch * dpi);
		}
		graphics.setColor(new Color(255, 200, 120));
		int carriage = (int) Math.round(profile.getCarriageWidth() * dpi);
		graphics.drawLine(carriage, 0, carriage, height);
	}

	private void drawGlyph(Graphics2D graphics, Glyph glyph) {
		if (glyph.getCharacter() == ' ' && !glyph.isUnderline()) {
			return;
		}
		double cellWidth = glyph.getWidth() * dpi;
		double ascent = profile.getBaselineOffset() * dpi;
		double baseline = (glyph.getY() + profile.getBaselineOffset()) * dpi;
		double left = glyph.getX() * dpi;
		int style = (glyph.isBold() ? Font.BOLD : 0) | (glyph.isItalic() ? Font.ITALIC : 0);
		Font font = new Font(Font.MONOSPACED, style, 100);
		FontRenderContext context = graphics.getFontRenderContext();
		String text = String.valueOf(glyph.getCharacter());
		double advance = font.getStringBounds("M", context).getWidth();
		double capHeight = font.createGlyphVector(context, "H").getVisualBounds().getHeight();
		AffineTransform scale = AffineTransform.getScaleInstance(cellWidth / advance, ascent * 0.9 / capHeight);
		graphics.setFont(font.deriveFont(scale));
		if (glyph.getCharacter() != ' ') {
			graphics.drawString(text, (float) left, (float) baseline);
		}
		if (glyph.isUnderline()) {
			graphics.setStroke(new BasicStroke((float) Math.max(1, dpi / 100d)));
			int underlineY = (int) Math.round(baseline + dpi / 72d);
			graphics.drawLine((int) Math.round(left), underlineY, (int) Math.round(left + cellWidth), underlineY);
		}
	}

	private BufferedImage tint(BufferedImage reference, int width, int height) {
		BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int maxX = Math.min(width, reference.getWidth());
		int maxY = Math.min(height, reference.getHeight());
		for (int y = 0; y < maxY; y++) {
			for (int x = 0; x < maxX; x++) {
				int rgb = reference.getRGB(x, y);
				int alphaIn = (rgb >>> 24) & 0xFF;
				int luminance = (((rgb >> 16) & 0xFF) * 30 + ((rgb >> 8) & 0xFF) * 59 + (rgb & 0xFF) * 11) / 100;
				int coverage = reference.getColorModel().hasAlpha() ? alphaIn * (255 - luminance) / 255 : 255 - luminance;
				if (coverage > 8) {
					int alpha = coverage * 110 / 255;
					tinted.setRGB(x, y, (alpha << 24) | (REFERENCE.getRGB() & 0xFFFFFF));
				}
			}
		}
		return tinted;
	}
}
