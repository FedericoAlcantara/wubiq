package net.sf.wubiq.dotmatrix;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.AttributedString;

import net.sf.wubiq.dotmatrix.witness.ExpectedText;
import net.sf.wubiq.wrappers.TextField;

/**
 * One string a fixture draws, the Java2D call used to draw it, and the
 * expectation derived from it.
 * @author Federico Alcantara
 *
 */
public class DrawItem {
	public enum Method { STRING_INT, STRING_FLOAT, ATTRIBUTED, GLYPH_VECTOR, CHARS, CHILD_GRAPHICS }

	private final int page;
	private final String text;
	private final float x;
	private final float y;
	private final Font font;
	private Method method = Method.STRING_FLOAT;
	private AffineTransform transform;

	/**
	 * @param page Zero based page.
	 * @param text Text to draw.
	 * @param x Left of the text, in points (user space).
	 * @param y Baseline, in points (user space).
	 * @param font Font to draw with.
	 */
	public DrawItem(int page, String text, float x, float y, Font font) {
		this.page = page;
		this.text = text;
		this.x = x;
		this.y = y;
		this.font = font;
	}

	public DrawItem method(Method method) {
		this.method = method;
		return this;
	}

	/**
	 * @param transform Applied to the graphics before drawing (on top of the device transform).
	 */
	public DrawItem transform(AffineTransform transform) {
		this.transform = transform;
		return this;
	}

	public int getPage() {
		return page;
	}

	public void draw(Graphics2D graphics) {
		AffineTransform saved = graphics.getTransform();
		if (transform != null) {
			graphics.transform(transform);
		}
		graphics.setFont(font);
		switch (method) {
		case STRING_INT:
			graphics.drawString(text, Math.round(x), Math.round(y));
			break;
		case ATTRIBUTED:
			AttributedString attributed = new AttributedString(text);
			attributed.addAttribute(TextAttribute.FONT, font);
			graphics.drawString(attributed.getIterator(), x, y);
			break;
		case GLYPH_VECTOR:
			graphics.drawGlyphVector(font.createGlyphVector(graphics.getFontRenderContext(), text), x, y);
			break;
		case CHARS:
			graphics.drawChars(text.toCharArray(), 0, text.length(), Math.round(x), Math.round(y));
			break;
		case CHILD_GRAPHICS:
			Graphics2D child = (Graphics2D) graphics.create();
			try {
				child.setFont(font);
				child.drawString(text, x, y);
			} finally {
				child.dispose();
			}
			break;
		default:
			graphics.drawString(text, x, y);
		}
		graphics.setTransform(saved);
	}

	/**
	 * @return Where the application meant the text to be, on the sheet.
	 */
	public ExpectedText expected() {
		Point2D point = new Point2D.Float(x, y);
		double size = font.getSize2D();
		if (transform != null) {
			point = transform.transform(point, null);
			size *= Math.sqrt(Math.abs(transform.getDeterminant()));
		}
		boolean underline = TextAttribute.UNDERLINE_ON.equals(font.getAttributes().get(TextAttribute.UNDERLINE));
		float cpi = new TextField(page, 0, 0, (int) Math.round(size), text).getTextFontCPI();
		return ExpectedText.points(page, text, point.getX(), point.getY())
				.bold(font.isBold()).italic(font.isItalic()).underline(underline).cpi(cpi);
	}
}
