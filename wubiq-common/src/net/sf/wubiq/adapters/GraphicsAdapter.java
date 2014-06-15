/**
 * 
 */
package net.sf.wubiq.adapters;

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
import java.io.Serializable;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.wrappers.CompositeWrapper;
import net.sf.wubiq.wrappers.GlyphVectorWrapper;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.ImageObserverWrapper;
import net.sf.wubiq.wrappers.ImageWrapper;
import net.sf.wubiq.wrappers.RenderableImageWrapper;
import net.sf.wubiq.wrappers.RenderedImageWrapper;
import net.sf.wubiq.wrappers.RenderingHintWrapper;
import net.sf.wubiq.wrappers.RenderingHintsWrapper;
import net.sf.wubiq.wrappers.ShapeWrapper;
import net.sf.wubiq.wrappers.StrokeWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Graphics Adapter for communicating with remote.
 * @author Federico Alcantara
 *
 */
public class GraphicsAdapter extends Graphics2D implements IRemoteAdapter {
	private static final Log LOG = LogFactory.getLog(GraphicsAdapter.class);
	private IDirectConnectorQueue queue;
	private UUID objectUUID;

	public GraphicsAdapter(IDirectConnectorQueue queue, UUID objectUUID) {
		this.queue = queue;
		this.objectUUID = objectUUID;
		queue.registerObject(objectUUID, this);
	}

	public GraphicsAdapter(IDirectConnectorQueue queue) {
		this(queue, UUID.randomUUID());
	}
	
	private void sendCommand(String command, GraphicParameter... parameters) {
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
			RemoteCommand remoteCommand = new RemoteCommand(getObjectUUID(), 
					command, parameters);
			queue.sendCommand(remoteCommand);
		} else {
			LOG.info("Method not implemented: " + fullCommand.toString());
		}
	}
	
	@Override
	public void addRenderingHints(Map <?, ?> hints) {
		sendCommand("addRenderingHints", new GraphicParameter(RenderingHintsWrapper.class, new RenderingHintsWrapper(hints)));
		queue().returnData();
	}

	@Override
	public void clip(Shape shape) {
		sendCommand("clip", shapeParameter(shape));
		queue().returnData();
	}

	@Override
	public boolean hit(Rectangle rect, Shape shape, boolean onStroke) {
		sendCommand("hit", new GraphicParameter(Rectangle.class, rect), shapeParameter(shape), 
				new GraphicParameter(boolean.class, onStroke));
		return (Boolean)queue().returnData();
	}

	@Override
	public void rotate(double theta) {
		sendCommand("rotate", new GraphicParameter(double.class, theta));
		queue().returnData();
	}

	@Override
	public void rotate(double theta, double x, double y) {
		sendCommand("rotate", new GraphicParameter(double.class, theta), new GraphicParameter(double.class, x), 
			new GraphicParameter(double.class, y));
		queue().returnData();
	}

	@Override
	public void scale(double sx, double sy) {
		sendCommand("scale", new GraphicParameter(double.class, sx), new GraphicParameter(double.class, sy));
		queue().returnData();
	}

	@Override
	public void shear(double shx, double shy) {
		sendCommand("shear", new GraphicParameter(double.class, shx), new GraphicParameter(double.class, shy));
		queue().returnData();
	}

	@Override
	public void transform(AffineTransform transform) {
		sendCommand("transform", new GraphicParameter(AffineTransform.class, transform));
		queue().returnData();
	}

	@Override
	public void translate(int x, int y) {
		sendCommand("translate", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
		queue().returnData();
	}

	@Override
	public void translate(double tx, double ty) {
		sendCommand("translate", new GraphicParameter(double.class, tx), new GraphicParameter(double.class, ty));
		queue().returnData();
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		sendCommand("clearRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		queue().returnData();
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		sendCommand("clipRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		queue().returnData();
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx,
			int dy) {
		sendCommand("copyArea", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
			new GraphicParameter(int.class, dx), new GraphicParameter(int.class, dy));
		queue().returnData();
	}

	@Override
	public Graphics create() {
		sendCommand("createRemote");
		UUID remoteUUID = (UUID)queue().returnData();
		return new GraphicsAdapter(queue(), remoteUUID);
	}
	
	@Override
	public Graphics create(int x, int y, int width, int height) {
		sendCommand("createRemote",
				new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y),
				new GraphicParameter(int.class, width),
				new GraphicParameter(int.class, height));
		UUID remoteUUID = (UUID)queue().returnData();
		return new GraphicsAdapter(queue(), remoteUUID);
	}

	@Override
	public void dispose() {
		sendCommand("dispose");
	}
	
	@Override
	public void draw(Shape s) {
		sendCommand("draw", shapeParameter(s));
		queue().returnData();
	}

	@Override
	public void draw3DRect(int x, int y, int width, int height, boolean raised) {
		sendCommand("draw3DRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height), 
			new GraphicParameter(boolean.class, raised));
		queue().returnData();
	}
	
	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		sendCommand("drawArc", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
			new GraphicParameter(int.class, startAngle), new GraphicParameter(int.class, arcAngle));
		queue().returnData();
	}

	@Override
	public void drawBytes(byte[] data, int offset, int length, int x, int y) {
		sendCommand("drawBytes", new GraphicParameter(byte[].class, data), new GraphicParameter(int.class, offset),
				new GraphicParameter(int.class, length), new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y));
		queue().returnData();
	}
	
	@Override
	public void drawChars(char[] data, int offset, int length, int x, int y) {
		sendCommand("drawChars", new GraphicParameter(byte[].class, data), new GraphicParameter(int.class, offset),
				new GraphicParameter(int.class, length), new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y));
		queue().returnData();
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		sendCommand("drawGlyphVector", new GraphicParameter(GlyphVectorWrapper.class, new GlyphVectorWrapper(g)),
				new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
		queue().returnData();
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y), new GraphicParameter(ImageObserver.class, observer));
		return (Boolean)queue().returnData();
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
				new GraphicParameter(int.class, y), new GraphicParameter(Color.class, bgcolor),
				new GraphicParameter(ImageObserver.class, observer));
		return (Boolean)queue().returnData();
	}


	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y), new GraphicParameter(int.class, width),
			new GraphicParameter(int.class, height), new GraphicParameter(ImageObserver.class, observer));
		return (Boolean) queue().returnData();
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, 
			int height, Color bgcolor, ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, x), 
				new GraphicParameter(int.class, y), new GraphicParameter(int.class, width),
				new GraphicParameter(int.class, height), new GraphicParameter(Color.class, bgcolor), 
				new GraphicParameter(ImageObserver.class, observer));
		return (Boolean) queue().returnData();
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, dx1), 
			new GraphicParameter(int.class, dy1), new GraphicParameter(int.class, dx2),
			new GraphicParameter(int.class, dy2), new GraphicParameter(int.class, sx1),
			new GraphicParameter(int.class, sy1), new GraphicParameter(int.class, sx2),
			new GraphicParameter(int.class, sy2), new GraphicParameter(ImageObserver.class, observer));
		return (Boolean) queue().returnData();
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, 
			int dy2, int sx1, int sy1, 
			int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(int.class, dx1), 
			new GraphicParameter(int.class, dy1), new GraphicParameter(int.class, dx2),
			new GraphicParameter(int.class, dy2), new GraphicParameter(int.class, sx1),
			new GraphicParameter(int.class, sy1), new GraphicParameter(int.class, sx2),
			new GraphicParameter(int.class, sy2), new GraphicParameter(Color.class, bgcolor), 
			new GraphicParameter(ImageObserver.class, observer));
		return (Boolean) queue().returnData();
	}
	
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform), 
				new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(obs)));
		return (Boolean) queue().returnData();
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		sendCommand("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(BufferedImageOp.class, op), 
			new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
		queue().returnData();
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		sendCommand("drawLine", new GraphicParameter(int.class, x1), new GraphicParameter(int.class, y1), 
			new GraphicParameter(int.class, x2), new GraphicParameter(int.class, y2));
		queue().returnData();
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		sendCommand("drawOval", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		queue().returnData();
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		sendCommand("drawPolygon", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
			new GraphicParameter(int.class, nPoints));
		queue().returnData();
	}
	
	@Override
	public void drawPolygon(Polygon p) {
		sendCommand("drawPolygon", new GraphicParameter(Polygon.class, p));
		queue().returnData();
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		sendCommand("drawPolyline", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
				new GraphicParameter(int.class, nPoints));
		queue().returnData();
	}
	
	@Override
	public void drawRect(int x, int y, int width, int height) {
		sendCommand("drawRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));;
		queue().returnData();
	}
	
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		sendCommand("drawRenderableImage", new GraphicParameter(RenderableImageWrapper.class, new RenderableImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
		queue().returnData();
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		sendCommand("drawRenderedImage", new GraphicParameter(RenderedImageWrapper.class, new RenderedImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
		queue().returnData();
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		sendCommand("drawRoundRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, arcWidth), new GraphicParameter(int.class, arcHeight));
		queue().returnData();
	}


	@Override
	public void drawString(String str, int x, int y) {
		sendCommand("drawString", new GraphicParameter(String.class, str), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		queue().returnData();
	}

	@Override
	public void drawString(String str, float x, float y) {
		sendCommand("drawString", new GraphicParameter(String.class, str), new GraphicParameter(float.class, x), 
			new GraphicParameter(float.class, y));
		queue().returnData();
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		sendCommand("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		queue().returnData();
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		sendCommand("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(float.class, x), 
				new GraphicParameter(float.class, y));
		queue().returnData();
	}

	@Override
	public void fill(Shape s) {
		sendCommand("fill", shapeParameter(s));
		queue().returnData();
	}
	
	@Override
	public void fill3DRect(int x, int y, int width, int height, boolean raised) {
		sendCommand("fill3DRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
			new GraphicParameter(int.class, width), new GraphicParameter(int.class, height), 
			new GraphicParameter(boolean.class, raised));
		queue().returnData();
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		sendCommand("fillArc", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, startAngle), new GraphicParameter(int.class, arcAngle));
		queue().returnData();
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		sendCommand("fillOval", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));
		queue().returnData();
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		sendCommand("fillPolygon", new GraphicParameter(int[].class, xPoints), new GraphicParameter(int[].class, yPoints), 
				new GraphicParameter(int.class, nPoints));
		queue().returnData();
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		sendCommand("fillPolygon", new GraphicParameter(Polygon.class, p));
		queue().returnData();
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		sendCommand("fillRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height));;
		queue().returnData();
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth,
			int arcHeight) {
		sendCommand("fillRoundRect", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y), 
				new GraphicParameter(int.class, width), new GraphicParameter(int.class, height),
				new GraphicParameter(int.class, arcWidth), new GraphicParameter(int.class, arcHeight));
		queue().returnData();
	}

	@Override
	public Color getBackground() {
		sendCommand(methodName());
		return (Color) queue().returnData();
	}

	@Override
	public Shape getClip() {
		sendCommand(methodName());
		return (Shape) queue().returnData();
	}

	@Override
	public Rectangle getClipBounds() {
		sendCommand(methodName());
		return (Rectangle) queue().returnData();
	}

	@Override
	public Rectangle getClipBounds(Rectangle r) {
		sendCommand(methodName(), new GraphicParameter(Rectangle.class, r));
		return (Rectangle) queue().returnData();
	}
	
	@Override
	public Rectangle getClipRect() {
		sendCommand(methodName());
		return (Rectangle) queue().returnData();
	}
	
	@Override
	public Color getColor() {
		sendCommand(methodName());
		return (Color) queue().returnData();
	}

	@Override
	public Composite getComposite() {
		sendCommand(methodName());
		CompositeWrapper wrapper = (CompositeWrapper) queue().returnData();
		return wrapper.getComposite();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		sendCommand("getDeviceConfigurationRemote");
		UUID remoteUUID = (UUID)queue().returnData();
		GraphicsConfigurationAdapter remote = 
				new GraphicsConfigurationAdapter(queue(), remoteUUID);
		sendCommand(methodName());
		return remote;
	}

	@Override
	public Font getFont() {
		sendCommand(methodName());
		return (Font) queue().returnData();
	}

	@Override
	public FontMetrics getFontMetrics(Font font) {
		sendCommand(methodName(), new GraphicParameter(Font.class, font));
		return (FontMetrics) queue().returnData();
	}
	
	@Override
	public FontMetrics getFontMetrics() {
		sendCommand(methodName());
		return (FontMetrics) queue().returnData();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		sendCommand("getFontRenderContextRemote");
		UUID remoteUUID = (UUID) queue().returnData();
		FontRenderContextAdapter remote = 
				new FontRenderContextAdapter(queue(), remoteUUID);
		return remote;
	}

	@Override
	public Paint getPaint() {
		sendCommand(methodName());
		return (Paint) queue().returnData();
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		sendCommand(methodName());
		return (Object) queue().returnData();
	}

	@Override
	public RenderingHints getRenderingHints() {
		sendCommand(methodName());
		return (RenderingHints) queue().returnData();
	}

	@Override
	public Stroke getStroke() {
		sendCommand(methodName());
		return (Stroke) queue().returnData();
	}

	@Override
	public AffineTransform getTransform() {
		sendCommand(methodName());
		return (AffineTransform) queue().returnData();
	}

	@Override
	public boolean hitClip(int x, int y, int width, int height) {
		sendCommand(methodName(), 
				new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y),
				new GraphicParameter(int.class, width),
				new GraphicParameter(int.class, height));
		return (Boolean) queue().returnData();
	}
	
	@Override
	public void setBackground(Color color) {
		sendCommand("setBackground", new GraphicParameter(Color.class, color));
		queue().returnData();
	}

	@Override
	public void setClip(Shape shape) {
		sendCommand("setClip", shapeParameter(shape));
		queue().returnData();
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		sendCommand("setClip", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		queue().returnData();
	}

	@Override
	public void setColor(Color color) {
		sendCommand("setColor", new GraphicParameter(Color.class, color));
		queue().returnData();
	}
	
	@Override
	public void setComposite(Composite comp) {
		sendCommand("setComposite", new GraphicParameter(CompositeWrapper.class, new CompositeWrapper(comp)));
		queue().returnData();
	}

	@Override
	public void setFont(Font font) {
		sendCommand("setFont", new GraphicParameter(Font.class, font));
		queue().returnData();
	}

	@Override
	public void setPaintMode() {
		sendCommand("setPaintMode");
		queue().returnData();
	}

	@Override
	public void setXORMode(Color arg0) {
		sendCommand("setXORMode", new GraphicParameter(Color.class, arg0));
		queue().returnData();
	}
	
	@Override
	public void setPaint(Paint paint) {
		if (paint instanceof Color) {
			sendCommand("setPaint", new GraphicParameter(Color.class, paint));
		} else {
			sendCommand("setPaint", new GraphicParameter(Paint.class, paint));
		}
		queue().returnData();
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		sendCommand("setRenderingHint", new GraphicParameter(RenderingHintWrapper.class, new RenderingHintWrapper(hintKey, hintValue)), new GraphicParameter(int.class, 0));
		queue().returnData();
	}

	@Override
	public void setRenderingHints(Map <?, ?> hints) {
		for (Entry<?, ?> entry : hints.entrySet()) {
			sendCommand("setRenderingHint", new GraphicParameter(RenderingHintWrapper.class, new RenderingHintWrapper((RenderingHints.Key) entry.getKey(), entry.getValue())), new GraphicParameter(int.class, 0));
		}
		queue().returnData();
	}

	@Override
	public void setStroke(Stroke s) {
		if (s instanceof BasicStroke) { // only add it if valid stroke type
			sendCommand("setStroke", new GraphicParameter(StrokeWrapper.class, new StrokeWrapper(s)));
		}
		queue().returnData();
	}

	@Override
	public void setTransform(AffineTransform transform) {
		sendCommand("setTransform", new GraphicParameter(AffineTransform.class, transform));
		queue().returnData();
	}
	
	private GraphicParameter shapeParameter(Shape shape) {
		GraphicParameter returnValue = new GraphicParameter(ShapeWrapper.class, new ShapeWrapper(shape));
		return returnValue;
	}
	
	/* *****************************************
	 * IRemoteAdapter interface implementation
	 * *****************************************
	 */
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#queue()
	 */
	@Override
	public IDirectConnectorQueue queue() {
		return queue;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#addListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public void addListener(IRemoteListener listener) {
		queue.addListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#removeListener(net.sf.wubiq.interfaces.IRemoteListener)
	 */
	@Override
	public boolean removeListener(IRemoteListener listener) {
		return queue.removeListener(listener);
	}

	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#listeners()
	 */
	public Set<IRemoteListener> listeners() {
		return queue.listeners();
	}
	
	/* **************************************
	 * SUPPORT ROUTINES
	 * *************************************
	 */

	/**
	 * @return The invoking method name.
	 */
	private String methodName() {
		if (Thread.currentThread().getStackTrace().length >= 3) {
			return Thread.currentThread().getStackTrace()[2].getMethodName();
		} else { 
			return null;
		}
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}

}
