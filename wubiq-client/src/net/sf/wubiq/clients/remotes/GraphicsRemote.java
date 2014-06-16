/**
 * 
 */
package net.sf.wubiq.clients.remotes;

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
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.wrappers.CompositeWrapper;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Graphics Adapter for communicating with remote.
 * @author Federico Alcantara
 *
 */
public class GraphicsRemote extends Graphics2D implements IProxyMaster {
	public static final String[] FILTERED_METHODS = new String[]{
		"getClip"
	};
	
	private DirectPrintManager manager;
	private Graphics2D decoratedObject;
	private UUID objectUUID;
	
	public GraphicsRemote() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClientSlave#initialize()
	 */
	public void initialize() {
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteClientMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return decoratedObject;
	}
	
	/**
	 * @param decoratedObject
	 */
	public void setDecoratedObject(Object graphics) {
		this.decoratedObject = (Graphics2D)graphics;
	}
	
	@Override
	public void addRenderingHints(Map <?, ?> hints) {
		decoratedObject.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape shape) {
		decoratedObject.clip(shape);
	}

	@Override
	public boolean hit(Rectangle rect, Shape shape, boolean onStroke) {
		return decoratedObject.hit(rect, shape, onStroke);
	}

	@Override
	public void rotate(double theta) {
		decoratedObject.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		decoratedObject.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		decoratedObject.scale(sx, sy);
	}

	@Override
	public void shear(double shx, double shy) {
		decoratedObject.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform transform) {
		decoratedObject.transform(transform);
	}

	@Override
	public void translate(int x, int y) {
		decoratedObject.translate(x, y);
	}

	@Override
	public void translate(double tx, double ty) {
		decoratedObject.translate(tx, ty);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		decoratedObject.clearRect(x, y, width, height);
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		decoratedObject.clipRect(x, y, width, height);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx,
			int dy) {
		decoratedObject.copyArea(x, y, width, height, dx, dy);
	}

	@Override
	public Graphics create() {
		throw new NotImplementedException();
	}
	
	/**
	 * Special method for establishing a two way communications.
	 * @return Unique id of the just created decoratedObject.
	 */
	public UUID createRemote() {
		GraphicsRemote remote = (GraphicsRemote) Enhancer.create(GraphicsRemote.class,
				new ProxyRemoteMaster(manager, GraphicsRemote.FILTERED_METHODS));
		remote.initialize();
		remote.setDecoratedObject((Graphics2D)decoratedObject.create());
		return remote.getObjectUUID();
	}
	
	@Override
	public Graphics create(int x, int y, int width, int height) {
		throw new NotImplementedException();
	}

	/**
	 * Special method for establishing a two way communications.
	 * @param x 
	 * @param y
	 * @param width
	 * @param height
	 * @return
	 */
	public UUID createRemote(int x, int y, int width, int height) {
		GraphicsRemote remote = (GraphicsRemote) Enhancer.create(GraphicsRemote.class,
				new ProxyRemoteMaster(manager, GraphicsRemote.FILTERED_METHODS));
		remote.initialize();
		remote.setDecoratedObject((Graphics2D)decoratedObject.create());
		return remote.getObjectUUID();
	}

	@Override
	public void dispose() {
		decoratedObject.dispose();
	}
	
	@Override
	public void draw(Shape s) {
		decoratedObject.draw(s);
	}

	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		decoratedObject.draw3DRect(x, y, width, height, raised);
	}
	
	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		decoratedObject.drawArc(x, y, width, height, startAngle, arcAngle);

	}
	
	@Override
	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		decoratedObject.drawBytes(data, offset, length, x, y);
	}
	
	@Override
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		decoratedObject.drawChars(data, offset, length, x, y);
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		decoratedObject.drawGlyphVector(g, x, y);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return decoratedObject.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		return decoratedObject.drawImage(img, x, y, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, ImageObserver observer) {
		return decoratedObject.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, Color bgcolor, ImageObserver observer) {
		return decoratedObject.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, ImageObserver observer) {
		return decoratedObject.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		return decoratedObject.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}
	
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return decoratedObject.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		decoratedObject.drawImage(img, op, x, y);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		decoratedObject.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		decoratedObject.drawOval(x, y, width, height);
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		decoratedObject.drawPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawPolygon(Polygon p) {
		decoratedObject.drawPolygon(p);
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		decoratedObject.drawPolyline(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height) {
		decoratedObject.drawRect(x, y, width, height);
	}
	
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		decoratedObject.drawRenderableImage(img, xform);
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		decoratedObject.drawRenderedImage(img, xform);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		decoratedObject.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
	}


	@Override
	public void drawString(String str, int x, int y) {
		decoratedObject.drawString(str, x, y);
	}

	@Override
	public void drawString(String str, float x, float y) {
		decoratedObject.drawString(str, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		decoratedObject.drawString(iterator, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		decoratedObject.drawString(iterator, x, y);
	}

	@Override
	public void fill(Shape s) {
		decoratedObject.fill(s);
	}
	
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		decoratedObject.fill3DRect(x, y, width, height, raised);
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		decoratedObject.fillArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		decoratedObject.fillOval(x, y, width, height);
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		decoratedObject.fillPolygon(xPoints, yPoints, nPoints);
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		decoratedObject.fillPolygon(p);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		decoratedObject.fillRect(x, y, width, height);
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		decoratedObject.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}

	@Override
	public Color getBackground() {
		return decoratedObject.getBackground();
	}

	@Override
	public Shape getClip() {
		Shape shape = decoratedObject.getClip();
		return new GeneralPath(shape);
	}

	@Override
	public Rectangle getClipBounds() {
		return decoratedObject.getClipBounds();
	}

	@Override
	public Rectangle getClipBounds(Rectangle r) {
		return decoratedObject.getClipBounds(r);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Rectangle getClipRect() {
		return decoratedObject.getClipRect();
	}
	
	@Override
	public Color getColor() {
		return decoratedObject.getColor();
	}

	@Override
	public Composite getComposite() {
		return new CompositeWrapper(decoratedObject.getComposite());
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		throw new NotImplementedException();
	}
	
	/**
	 * Special method for getting a remote device configuration.
	 * @return Unique object UUID for communication.
	 */
	public UUID getDeviceConfigurationRemote() {
		GraphicsConfigurationRemote remote = (GraphicsConfigurationRemote)
				Enhancer.create(GraphicsConfigurationRemote.class,
						new ProxyRemoteMaster(manager, GraphicsConfigurationRemote.FILTERED_METHODS));
		remote.initialize();
		remote.setDecoratedObject(decoratedObject.getDeviceConfiguration());
		return remote.getObjectUUID();
	}

	@Override
	public Font getFont() {
		return decoratedObject.getFont();
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		return decoratedObject.getFontMetrics(font);
	}
	
	@Override
	public FontMetrics getFontMetrics() {
		return decoratedObject.getFontMetrics();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return decoratedObject.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return decoratedObject.getPaint();
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return decoratedObject.getRenderingHint(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return decoratedObject.getRenderingHints();
	}

	@Override
	public Stroke getStroke() {
		return decoratedObject.getStroke();
	}

	@Override
	public AffineTransform getTransform() {
		return decoratedObject.getTransform();
	}

	@Override
	public boolean hitClip(int x, int y, int width, int height) {
		return decoratedObject.hitClip(x, y, width, height);
	}
	
	@Override
	public void setBackground(Color color) {
		decoratedObject.setBackground(color);
	}

	@Override
	public void setClip(Shape clip) {
		decoratedObject.setClip(clip);
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		decoratedObject.setClip(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setColor(Color color) {
		decoratedObject.setColor(color);
	}
	
	@Override
	public void setComposite(Composite comp) {
		decoratedObject.setComposite(comp);
	}

	@Override
	public void setFont(Font font) {
		decoratedObject.setFont(font);
	}

	@Override
	public void setPaintMode() {
		decoratedObject.setPaintMode();
	}

	@Override
	public void setXORMode(Color color) {
		decoratedObject.setXORMode(color);
	}
	
	@Override
	public void setPaint(Paint paint) {
		decoratedObject.setPaint(paint);
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		decoratedObject.setRenderingHint(hintKey, hintValue);
	}

	@Override
	public void setRenderingHints(Map <?, ?> hints) {
		decoratedObject.setRenderingHints(hints);
	}

	@Override
	public void setStroke(Stroke s) {
		decoratedObject.setStroke(s);
	}

	@Override
	public void setTransform(AffineTransform transform) {
		decoratedObject.setTransform(transform);
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IClientRemote#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return objectUUID;
	}

}
