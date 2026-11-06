/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsStreamWrapper extends Graphics2D {
	private Graphics2D graphics;
	private Set<TextField> texts;
	private PrinterType printerType;
	private Font currentFont;
	
	public GraphicsStreamWrapper(Graphics2D graphics) {
		this.graphics = graphics;
		texts = new TreeSet<TextField>();
		printerType = PrintServiceUtils.printerType(graphics.getDeviceConfiguration().getDevice().getIDstring());
		currentFont = graphics.getFont();
		if (currentFont == null) {
			currentFont = Font.decode(Font.SERIF);
		}
	}

	public Set<TextField> getTexts() {
		return texts;
	}
	
	@Override
	public void clearRect(int x, int y, int width, int height) {
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
	}

	@Override
	public Graphics create() {
		return graphics.create();
	}

	@Override
	public void dispose() {
		graphics.dispose();
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return true;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return true;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		return true;
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		return true;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return true;
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		return true;
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
	}

	@Override
	public void drawString(String str, int x, int y) {
		TextField textField = new TextField(0, x, y, currentFont, str);
		
		texts.add(textField);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		StringBuffer str = new StringBuffer("");
		for (char charAt = iterator.first(); charAt != CharacterIterator.DONE; charAt = iterator.next()) {
			str.append(charAt);
		}
		if (str.length() > 0) {
			drawString(str.toString(), x, y);
		}
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
	}

	@Override
	public Shape getClip() {
		return graphics.getClip();
	}

	@Override
	public Rectangle getClipBounds() {
		return graphics.getClipBounds();
	}

	@Override
	public Color getColor() {
		return graphics.getColor();
	}

	@Override
	public Font getFont() {
		return currentFont;
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return graphics.getFontMetrics();
	}

	@Override
	public void setClip(Shape clip) {
		graphics.setClip(clip);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		graphics.setClip(x, y, width, height);
	}

	@Override
	public void setColor(Color c) {
		graphics.setColor(c);
	}

	@Override
	public void setFont(Font originalFont) {
		currentFont = GraphicsUtils.INSTANCE.properFont(originalFont, printerType);
		graphics.setFont(currentFont);
	}

	@Override
	public void setPaintMode() {
		graphics.setPaintMode();
	}

	@Override
	public void setXORMode(Color c1) {
		graphics.setXORMode(c1);
	}

	@Override
	public void translate(int x, int y) {
		graphics.translate(x, y);
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		graphics.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape s) {
		graphics.clip(s);
	}

	@Override
	public void draw(Shape s) {
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform,
			ImageObserver obs) {
		return true;
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x,
			int y) {
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
	}

	@Override
	public void drawString(String str, float x, float y) {
		drawString(str, new Float(x).intValue(), new Float(y).intValue());
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		drawString(iterator, new Float(x).intValue(), new Float(y).intValue());
	}

	@Override
	public void fill(Shape s) {
	}

	@Override
	public Color getBackground() {
		return graphics.getBackground();
	}

	@Override
	public Composite getComposite() {
		return graphics.getComposite();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return graphics.getDeviceConfiguration();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return graphics.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return graphics.getPaint();
	}
	
	@Override
	public Object getRenderingHint(Key hintKey) {
		return graphics.getRenderingHint(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return graphics.getRenderingHints();
	}

	@Override
	public Stroke getStroke() {
		return graphics.getStroke();
	}

	@Override
	public AffineTransform getTransform() {
		return graphics.getTransform();
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return graphics.hit(rect, s, onStroke);
	}

	@Override
	public void rotate(double theta) {
		graphics.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		graphics.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		graphics.scale(sx, sy);
	}

	@Override
	public void setBackground(Color color) {
		graphics.setBackground(color);
	}

	@Override
	public void setComposite(Composite comp) {
		graphics.setComposite(comp);
	}

	@Override
	public void setPaint(Paint paint) {
		graphics.setPaint(paint);
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		graphics.setRenderingHint(hintKey, hintValue);
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		graphics.setRenderingHints(hints);
	}

	@Override
	public void setStroke(Stroke s) {
		graphics.setStroke(s);
	}

	@Override
	public void setTransform(AffineTransform tx) {
		graphics.setTransform(tx);
	}

	@Override
	public void shear(double shx, double shy) {
		graphics.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform tx) {
		graphics.transform(tx);
	}

	@Override
	public void translate(double tx, double ty) {
		graphics.translate(tx, ty);
	}	
}
