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
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsPdfRecorder extends Graphics2D {
	private static final Log LOG = LogFactory.getLog(GraphicsPdfRecorder.class);
	private List<String> unimplemented;
	private transient Graphics2D originalGraphics;
	private transient int currentExecutionOrder;
	private transient GraphicCommand lastGraphicCommand;

	private GraphicsPdfRecorder() {
		unimplemented = new ArrayList<String>();
		currentExecutionOrder = 0;
	}
	
	public GraphicsPdfRecorder(Graphics2D originalGraphics) {
		this();
		this.originalGraphics = originalGraphics;
	}
	
	private void addToCommands(String command, GraphicParameter... parameters) {
		boolean serializable = true;
		StringBuffer fullCommand = new StringBuffer("");
		for (GraphicParameter parameter: parameters) {
			if (fullCommand.length() > 0) {
				fullCommand.append(',')
					.append(' ');
			}
			if (!parameter.getParameterType().isPrimitive() && !Serializable.class.isAssignableFrom(parameter.getParameterType())) {
				serializable = false;
				fullCommand.append('*');
			}
			fullCommand.append(parameter.getParameterType().getSimpleName());
			
		}
		if (serializable) {
			GraphicCommand graphicCommand = new GraphicCommand(currentExecutionOrder++, command, parameters);
			if (lastGraphicCommand == null || !graphicCommand.equals(lastGraphicCommand)) {
				//graphicCommands.add(graphicCommand);
				lastGraphicCommand = graphicCommand;
			} else {
				currentExecutionOrder--;
			}
		} else {
			fullCommand.insert(0, '(')
				.insert(0, command)
				.append(')');
			if (!unimplemented.contains(fullCommand.toString())) {
				unimplemented.add(fullCommand.toString());
				LOG.info("Method not implemented: " + fullCommand.toString());
			}
		}
	}
	
	@Override
	public void addRenderingHints(Map <?, ?> hints) {
		originalGraphics.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape shape) {
		originalGraphics.clip(shape);
	}

	@Override
	public boolean hit(Rectangle rect, Shape shape, boolean onStroke) {
		return originalGraphics.hit(rect, shape, onStroke);
	}

	@Override
	public void rotate(double theta) {
		originalGraphics.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		originalGraphics.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		originalGraphics.scale(sx, sy);
	}

	@Override
	public void shear(double shx, double shy) {
		originalGraphics.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform transform) {
		originalGraphics.transform(transform);
	}

	@Override
	public void translate(int x, int y) {
		originalGraphics.translate(x, y);
	}

	@Override
	public void translate(double tx, double ty) {
		originalGraphics.translate(tx, ty);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		originalGraphics.clearRect(x, y, width, height);
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		originalGraphics.clipRect(x, y, width, height);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx,
			int dy) {
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
		originalGraphics.dispose();
	}
	
	@Override
	public void draw(Shape s) {
		//addToCommands("draw", shapeParameter(s));
		originalGraphics.draw(s);
	}

	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		originalGraphics.draw3DRect(x, y, width, height, raised);
	}
	
	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		originalGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		originalGraphics.drawBytes(data, offset, length, x, y);
	}
	
	@Override
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		addToCommands("drawChars", new GraphicParameter(byte[].class, data), new GraphicParameter(int.class, offset),
				new GraphicParameter(int.class, length), new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y));
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		addToCommands("drawGlyphVector", new GraphicParameter(GlyphVectorWrapper.class, new GlyphVectorWrapper(g)),
				new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return originalGraphics.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return originalGraphics.drawImage(img, x, y, bgcolor, observer);
	}


	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, ImageObserver observer) {
		return originalGraphics.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, Color bgcolor, ImageObserver observer) {
		return originalGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, ImageObserver observer) {
		return originalGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		return originalGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}
	
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return originalGraphics.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		originalGraphics.drawImage(img, op, x, y);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		originalGraphics.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		originalGraphics.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		originalGraphics.drawPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawPolygon(Polygon p) {
		originalGraphics.drawPolygon(p);
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		originalGraphics.drawPolyline(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height) {
		originalGraphics.drawRect(x, y, width, height);
	}
	
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		originalGraphics.drawRenderableImage(img, xform);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		originalGraphics.drawRenderedImage(img, xform);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		originalGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}


	@Override
	public void drawString(String str, int x, int y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
	}

	@Override
	public void drawString(String str, float x, float y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(float.class, x), 
			new GraphicParameter(float.class, y));
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		addToCommands("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		addToCommands("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
	}

	@Override
	public void fill(Shape s) {
		addToCommands("clip", shapeParameter(s));
		originalGraphics.fill(s);
	}
	
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		originalGraphics.fill3DRect(x, y, width, height, raised);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		originalGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		originalGraphics.fillOval(x, y, width, height);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		originalGraphics.fillPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		originalGraphics.fillPolygon(p);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		originalGraphics.fillRect(x, y, width, height);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
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
		return originalGraphics.getDeviceConfiguration();
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
		originalGraphics.setBackground(color);
	}

	@Override
	public void setClip(Shape shape) {
		originalGraphics.setClip(shape);
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		originalGraphics.setClip(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setColor(Color color) {
		originalGraphics.setColor(color);
	}
	
	@Override
	public void setComposite(Composite comp) {
		originalGraphics.setComposite(comp);
	}

	@Override
	public void setFont(Font font) {
		originalGraphics.setFont(font);
	}

	@Override
	public void setPaintMode() {
		originalGraphics.setPaintMode();
	}

	@Override
	public void setXORMode(Color arg0) {
		originalGraphics.setXORMode(arg0);
	}
	
	@Override
	public void setPaint(Paint paint) {
		originalGraphics.setPaint(paint);
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		originalGraphics.setRenderingHint(hintKey, hintValue);
	}

	@Override
	public void setRenderingHints(Map <?, ?> hints) {
		originalGraphics.setRenderingHints(hints);
	}

	@Override
	public void setStroke(Stroke s) {
		originalGraphics.setStroke(s);
	}

	@Override
	public void setTransform(AffineTransform transform) {
		originalGraphics.setTransform(transform);
	}
	
	private GraphicParameter shapeParameter(Shape shape) {
		GraphicParameter returnValue = new GraphicParameter(ShapeWrapper.class, new ShapeWrapper(shape));
		return returnValue;
	}

}
