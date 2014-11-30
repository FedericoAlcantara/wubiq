/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsChunkRecorder extends Graphics2D {
	private Set<GraphicCommand> graphicCommands;
	private transient Graphics2D originalGraphics;
	private transient int currentExecutionOrder;
	private transient DefaultDeviceConfiguration deviceConfiguration;

	public void initialize(Set<GraphicCommand> graphicCommands, Graphics2D originalGraphics) {
		currentExecutionOrder = 0;
		this.graphicCommands = graphicCommands;
		this.originalGraphics = originalGraphics;
		this.deviceConfiguration = new DefaultDeviceConfiguration(originalGraphics.getDeviceConfiguration());
		addToCommands("setTransform", new GraphicParameter(AffineTransform.class, originalGraphics.getTransform()));
		addToCommands("setBackground", new GraphicParameter(Color.class, originalGraphics.getBackground()));
		addToCommands("setColor", new GraphicParameter(Color.class, originalGraphics.getColor()));
		addToCommands("setClip", shapeParameter(originalGraphics.getClip()));
		addToCommands("setFont", new GraphicParameter(Font.class, originalGraphics.getFont()));
		addToCommands("addRenderingHints", new GraphicParameter(RenderingHintsWrapper.class, new RenderingHintsWrapper(originalGraphics.getRenderingHints())));
	}
	
	private void addToCommands(String command, GraphicParameter... parameters) {
		graphicCommands.add(new GraphicCommand(currentExecutionOrder++, command, parameters));
	}
	
	@Override
	public void addRenderingHints(Map <?, ?> hints) {
		addToCommands("addRenderingHints", new GraphicParameter(RenderingHintsWrapper.class, new RenderingHintsWrapper(hints)));
		originalGraphics.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape shape) {
		addToCommands("clip", shapeParameter(shape));
		originalGraphics.clip(shape);
	}

	@Override
	public boolean hit(Rectangle rect, Shape shape, boolean onStroke) {
		addToCommands("hit", new GraphicParameter(Rectangle.class, rect), shapeParameter(shape), 
				new GraphicParameter(boolean.class, onStroke));
		return originalGraphics.hit(rect, shape, onStroke);
	}

	@Override
	public void rotate(double theta) {
		addToCommands("rotate", new GraphicParameter(double.class, theta));
		originalGraphics.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		addToCommands("rotate", new GraphicParameter(double.class, theta), new GraphicParameter(double.class, x), 
			new GraphicParameter(double.class, y));
		originalGraphics.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		addToCommands("scale", new GraphicParameter(double.class, sx), new GraphicParameter(double.class, sy));
		originalGraphics.scale(sx, sy);
	}

	@Override
	public void shear(double shx, double shy) {
		addToCommands("shear", new GraphicParameter(double.class, shx), new GraphicParameter(double.class, shy));
		originalGraphics.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform transform) {
		addToCommands("transform", new GraphicParameter(AffineTransform.class, transform));
		originalGraphics.transform(transform);
	}

	@Override
	public void translate(int x, int y) {
		addToCommands("translate", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
		originalGraphics.translate(x, y);
	}

	@Override
	public void translate(double tx, double ty) {
		addToCommands("translate", new GraphicParameter(double.class, tx), new GraphicParameter(double.class, ty));
		originalGraphics.translate(tx, ty);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		addToCommands("clearRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		originalGraphics.clearRect(x, y, width, height);
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		addToCommands("clipRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		originalGraphics.clipRect(x, y, width, height);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx,
			int dy) {
		addToCommands("copyArea", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
			new GraphicParameter(int.class, dx), new GraphicParameter(int.class, dy));
		originalGraphics.copyArea(x, y, width, height, dx, dy);
	}

	@Override
	public Graphics create() {
		return originalGraphics.create();
	}
	
	@Override
	public Graphics create(int x, int y, int width, int height) {
		return originalGraphics.create(x, y, width, height);
	}

	@Override
	public void dispose() {
	 	//addToCommands("dispose"); // NO!!! as this can occur any time and 
		originalGraphics.dispose();
	}
	
	@Override
	public void draw(Shape s) {
		addToCommands("draw", shapeParameter(s));
		originalGraphics.draw(s);
	}

	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		addToCommands("draw3DRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height), 
			new GraphicParameter(boolean.class, raised));
		originalGraphics.draw3DRect(x, y, width, height, raised);
	}
	
	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		addToCommands("drawArc", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
			new GraphicParameter(int.class, startAngle), new GraphicParameter(int.class, arcAngle));
		originalGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		addToCommands("drawBytes", new GraphicParameter(byte[].class, data), new GraphicParameter(int.class, offset),
				new GraphicParameter(int.class, length), new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y));
		originalGraphics.drawBytes(data, offset, length, x, y);
	}
	
	@Override
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		addToCommands("drawChars", new GraphicParameter(byte[].class, data), new GraphicParameter(int.class, offset),
				new GraphicParameter(int.class, length), new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y));
		originalGraphics.drawChars(data, offset, length, x, y);
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		addToCommands("drawGlyphVector", new GraphicParameter(GlyphChunkVectorWrapper.class, new GlyphChunkVectorWrapper(g)),
				new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
		originalGraphics.drawGlyphVector(g, x, y);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y), new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
				new GraphicParameter(int.class, y), new GraphicParameter(Color.class, bgcolor),
				new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, x, y, bgcolor, observer);
	}


	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y), new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height), new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, Color bgcolor, ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
				new GraphicParameter(int.class, y), new GraphicParameter(int.class, width),
				new GraphicParameter(int.class, height), new GraphicParameter(Color.class, bgcolor), 
				new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, dx1), 
			new GraphicParameter(int.class, dy1), new GraphicParameter(int.class, dx2),
			new GraphicParameter(int.class, dy2), new GraphicParameter(int.class, sx1),
			new GraphicParameter(int.class, sy1), new GraphicParameter(int.class, sx2),
			new GraphicParameter(int.class, sy2), new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, dx1), 
			new GraphicParameter(int.class, dy1), new GraphicParameter(int.class, dx2),
			new GraphicParameter(int.class, dy2), new GraphicParameter(int.class, sx1),
			new GraphicParameter(int.class, sy1), new GraphicParameter(int.class, sx2),
			new GraphicParameter(int.class, sy2), new GraphicParameter(Color.class, bgcolor), 
			new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(observer)));
		return originalGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}
	
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform), 
				new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(obs)));
		return originalGraphics.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(BufferedImageOp.class, op), 
			new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
		originalGraphics.drawImage(img, op, x, y);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		addToCommands("drawLine", new GraphicParameter(int.class, x1), new GraphicParameter(int.class, y1), 
			new GraphicParameter(int.class, x2), new GraphicParameter(int.class, y2));
		originalGraphics.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		addToCommands("drawOval", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		originalGraphics.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		addToCommands("drawPolygon", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
			new GraphicParameter(int.class, nPoints));
		originalGraphics.drawPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawPolygon(Polygon p) {
		addToCommands("drawPolygon", new GraphicParameter(Polygon.class, p));
		originalGraphics.drawPolygon(p);
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		addToCommands("drawPolyline", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
				new GraphicParameter(int.class, nPoints));
		originalGraphics.drawPolyline(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height) {
		addToCommands("drawRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));;
		originalGraphics.drawRect(x, y, width, height);
	}
	
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		addToCommands("drawRenderableImage", new GraphicParameter(RenderableImageWrapper.class, new RenderableImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
		originalGraphics.drawRenderableImage(img, xform);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		addToCommands("drawRenderedImage", new GraphicParameter(RenderedImageWrapper.class, new RenderedImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
		originalGraphics.drawRenderedImage(img, xform);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		addToCommands("drawRoundRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, arcWidth), new GraphicParameter(int.class, arcHeight));
		originalGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}


	@Override
	public void drawString(String str, int x, int y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		originalGraphics.drawString(str, x, y);
	}

	@Override
	public void drawString(String str, float x, float y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(float.class, x), 
			new GraphicParameter(float.class, y));
		originalGraphics.drawString(str, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		addToCommands("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		originalGraphics.drawString(iterator, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		addToCommands("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
		originalGraphics.drawString(iterator, x, y);
	}

	@Override
	public void fill(Shape s) {
		addToCommands("fill", shapeParameter(s));
		originalGraphics.fill(s);
	}
	
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		addToCommands("fill3DRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height), 
			new GraphicParameter(boolean.class, raised));
		originalGraphics.fill3DRect(x, y, width, height, raised);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		addToCommands("fillArc", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, startAngle), new GraphicParameter(int.class, arcAngle));
		originalGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		addToCommands("fillOval", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		originalGraphics.fillOval(x, y, width, height);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		addToCommands("fillPolygon", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
				new GraphicParameter(int.class, nPoints));
		originalGraphics.fillPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		addToCommands("fillPolygon", new GraphicParameter(Polygon.class, p));
		originalGraphics.fillPolygon(p);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		addToCommands("fillRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));;
		originalGraphics.fillRect(x, y, width, height);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		addToCommands("fillRoundRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, arcWidth), new GraphicParameter(int.class, arcHeight));
		originalGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	@Override
	public Color getBackground() {
		return originalGraphics.getBackground();
	}

	@Override
	public Shape getClip() {
		return originalGraphics.getClip();
	}

	@Override
	public Rectangle getClipBounds() {
		return originalGraphics.getClipBounds();
	}

	@Override
	public Rectangle getClipBounds(Rectangle r) {
		return originalGraphics.getClipBounds(r);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Rectangle getClipRect() {
		return originalGraphics.getClipRect();
	}
	
	@Override
	public Color getColor() {
		return originalGraphics.getColor();
	}

	@Override
	public Composite getComposite() {
		return originalGraphics.getComposite();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		if (deviceConfiguration == null) {
			deviceConfiguration = new DefaultDeviceConfiguration(originalGraphics.getDeviceConfiguration());
		}
		return deviceConfiguration;
	}

	@Override
	public Font getFont() {
		return originalGraphics.getFont();
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		return originalGraphics.getFontMetrics(font);
	}
	
	@Override
	public FontMetrics getFontMetrics() {
		return originalGraphics.getFontMetrics();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return originalGraphics.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return originalGraphics.getPaint();
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return originalGraphics.getRenderingHint(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return originalGraphics.getRenderingHints();
	}

	@Override
	public Stroke getStroke() {
		return originalGraphics.getStroke();
	}

	@Override
	public AffineTransform getTransform() {
		return originalGraphics.getTransform();
	}

	@Override
	public boolean hitClip(int x, int y, int width, int height) {
		return originalGraphics.hitClip(x, y, width, height);
	}
	
	@Override
	public void setBackground(Color color) {
		addToCommands("setBackground", new GraphicParameter(Color.class, color));
		originalGraphics.setBackground(color);
	}

	@Override
	public void setClip(Shape shape) {
		addToCommands("setClip", shapeParameter(shape));
		originalGraphics.setClip(shape);
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("setClip", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		originalGraphics.setClip(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setColor(Color color) {
		addToCommands("setColor", new GraphicParameter(Color.class, color));
		originalGraphics.setColor(color);
	}
	
	@Override
	public void setComposite(Composite comp) {
		addToCommands("setComposite", new GraphicParameter(CompositeWrapper.class, new CompositeWrapper(comp)));
		originalGraphics.setComposite(comp);
	}

	@Override
	public void setFont(Font font) {
		addToCommands("setFont", new GraphicParameter(Font.class, font));
		originalGraphics.setFont(font);
	}

	@Override
	public void setPaintMode() {
		addToCommands("setPaintMode");
		originalGraphics.setPaintMode();
	}

	@Override
	public void setXORMode(Color arg0) {
		addToCommands("setXORMode", new GraphicParameter(Color.class, arg0));
		originalGraphics.setXORMode(arg0);
	}
	
	@Override
	public void setPaint(Paint paint) {
		if (paint instanceof Color) {
			addToCommands("setPaint", new GraphicParameter(Color.class, paint));
		} else {
			addToCommands("setPaint", new GraphicParameter(Paint.class, paint));
		}
		originalGraphics.setPaint(paint);
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		addToCommands("setRenderingHint", new GraphicParameter(RenderingHintWrapper.class, new RenderingHintWrapper(hintKey, hintValue)), new GraphicParameter(int.class, 0));
		originalGraphics.setRenderingHint(hintKey, hintValue);
	}

	@Override
	public void setRenderingHints(Map <?, ?> hints) {
		for (Entry<?, ?> entry : hints.entrySet()) {
			addToCommands("setRenderingHint", new GraphicParameter(RenderingHintWrapper.class, new RenderingHintWrapper((RenderingHints.Key) entry.getKey(), entry.getValue())), new GraphicParameter(int.class, 0));
		}
		originalGraphics.setRenderingHints(hints);
	}

	@Override
	public void setStroke(Stroke s) {
		if (s instanceof BasicStroke) { // only add it if valid stroke type
			addToCommands("setStroke", new GraphicParameter(StrokeWrapper.class, new StrokeWrapper(s)));
		}
		originalGraphics.setStroke(s);
	}

	@Override
	public void setTransform(AffineTransform transform) {
		addToCommands("setTransform", new GraphicParameter(AffineTransform.class, transform));
		originalGraphics.setTransform(transform);
	}
	
	private GraphicParameter shapeParameter(Shape shape) {
		GraphicParameter returnValue = new GraphicParameter(ShapeWrapper.class, new ShapeWrapper(shape));
		return returnValue;
	}

}
