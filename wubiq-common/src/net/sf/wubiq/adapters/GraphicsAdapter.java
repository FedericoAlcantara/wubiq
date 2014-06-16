/**
 * 
 */
package net.sf.wubiq.adapters;

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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.interfaces.IProxySlave;
import net.sf.wubiq.interfaces.IRemoteAdapter;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.wrappers.CompositeWrapper;
import net.sf.wubiq.wrappers.GraphicParameter;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsAdapter extends Graphics2D implements IRemoteAdapter,
		IProxySlave {

	private IDirectConnectorQueue queue;
	private UUID objectUUID;
	
	public static final String[] FILTERED_METHODS = new String[]{
		"create",
		"dispose",
		"composite",
		"getDeviceConfiguration"
	};

	/**
	 * @see net.sf.wubiq.interfaces.IProxySlave#initialize()
	 */
	@Override
	public void initialize() {
	}

	/**
	 * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
	 */
	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		
	}

	/**
	 * @see java.awt.Graphics2D#clip(java.awt.Shape)
	 */
	@Override
	public void clip(Shape s) {
		
	}

	/**
	 * @see java.awt.Graphics2D#draw(java.awt.Shape)
	 */
	@Override
	public void draw(Shape s) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
	 */
	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		return false;
	}

	/**
	 * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
	 */
	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
	 */
	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
	 */
	@Override
	public void drawString(String str, int x, int y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
	 */
	@Override
	public void drawString(String str, float x, float y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
	 */
	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#fill(java.awt.Shape)
	 */
	@Override
	public void fill(Shape s) {
		
	}

	/**
	 * @see java.awt.Graphics2D#getBackground()
	 */
	@Override
	public Color getBackground() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getComposite()
	 */
	@Override
	public Composite getComposite() {
		queue.sendCommand(new RemoteCommand(objectUUID, "getComposite"));
		CompositeWrapper wrapper = (CompositeWrapper) queue().returnData();
		return wrapper.getComposite();
	}

	/**
	 * @see java.awt.Graphics2D#getDeviceConfiguration()
	 */
	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		queue.sendCommand(new RemoteCommand(objectUUID, "getDeviceConfigurationRemote"));
		UUID remoteUUID = (UUID)queue().returnData();
		GraphicsConfigurationAdapter remote = 
				new GraphicsConfigurationAdapter(queue(), remoteUUID);
		return remote;
	}

	/**
	 * @see java.awt.Graphics2D#getFontRenderContext()
	 */
	@Override
	public FontRenderContext getFontRenderContext() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getPaint()
	 */
	@Override
	public Paint getPaint() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
	 */
	@Override
	public Object getRenderingHint(Key hintKey) {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getRenderingHints()
	 */
	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getStroke()
	 */
	@Override
	public Stroke getStroke() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#getTransform()
	 */
	@Override
	public AffineTransform getTransform() {
		return null;
	}

	/**
	 * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
	 */
	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return false;
	}

	/**
	 * @see java.awt.Graphics2D#rotate(double)
	 */
	@Override
	public void rotate(double theta) {
		
	}

	/**
	 * @see java.awt.Graphics2D#rotate(double, double, double)
	 */
	@Override
	public void rotate(double theta, double x, double y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#scale(double, double)
	 */
	@Override
	public void scale(double sx, double sy) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color color) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
	 */
	@Override
	public void setComposite(Composite comp) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
	 */
	@Override
	public void setPaint(Paint paint) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
	 */
	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
	 */
	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
	 */
	@Override
	public void setStroke(Stroke s) {
		
	}

	/**
	 * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
	 */
	@Override
	public void setTransform(AffineTransform Tx) {
		
	}

	/**
	 * @see java.awt.Graphics2D#shear(double, double)
	 */
	@Override
	public void shear(double shx, double shy) {
		
	}

	/**
	 * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
	 */
	@Override
	public void transform(AffineTransform Tx) {
		
	}

	/**
	 * @see java.awt.Graphics2D#translate(int, int)
	 */
	@Override
	public void translate(int x, int y) {
		
	}

	/**
	 * @see java.awt.Graphics2D#translate(double, double)
	 */
	@Override
	public void translate(double tx, double ty) {
		
	}

	/**
	 * @see java.awt.Graphics#clearRect(int, int, int, int)
	 */
	@Override
	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#clipRect(int, int, int, int)
	 */
	@Override
	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	@Override
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	/**
	 * @see java.awt.Graphics#create()
	 */
	@Override
	public Graphics create() {
		queue.sendCommand(new RemoteCommand(objectUUID, "createRemote"));
		UUID remoteUUID = (UUID)queue().returnData();
		GraphicsAdapter adapter = (GraphicsAdapter)
				Enhancer.create(GraphicsAdapter.class,
						new ProxyAdapterSlave(queue, remoteUUID, 
								GraphicsAdapter.FILTERED_METHODS));
		adapter.initialize();
		return adapter;
	}
	
	/**
	 * @see java.awt.Graphics#create(int, int, int, int)
	 */
	@Override
	public Graphics create(int x, int y, int width, int height) {
		queue.sendCommand(new RemoteCommand(objectUUID, "createRemote",
				new GraphicParameter(int.class, x),
				new GraphicParameter(int.class, y),
				new GraphicParameter(int.class, width),
				new GraphicParameter(int.class, height)));
		UUID remoteUUID = (UUID)queue().returnData();
		GraphicsAdapter adapter = (GraphicsAdapter)
				Enhancer.create(GraphicsAdapter.class,
						new ProxyAdapterSlave(queue, remoteUUID, 
								GraphicsAdapter.FILTERED_METHODS));
		adapter.initialize();
		return adapter;
	}

	/**
	 * @see java.awt.Graphics#dispose()
	 */
	@Override
	public void dispose() {
		
	}

	/**
	 * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
	 */
	@Override
	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 */
	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		return false;
	}

	/**
	 * @see java.awt.Graphics#drawLine(int, int, int, int)
	 */
	@Override
	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#drawOval(int, int, int, int)
	 */
	@Override
	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#drawPolygon(int[], int[], int)
	 */
	@Override
	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		
	}

	/**
	 * @see java.awt.Graphics#drawPolyline(int[], int[], int)
	 */
	@Override
	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		
	}

	/**
	 * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	/**
	 * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
	 */
	@Override
	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	/**
	 * @see java.awt.Graphics#fillOval(int, int, int, int)
	 */
	@Override
	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#fillPolygon(int[], int[], int)
	 */
	@Override
	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		
	}

	/**
	 * @see java.awt.Graphics#fillRect(int, int, int, int)
	 */
	@Override
	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
	 */
	@Override
	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	/**
	 * @see java.awt.Graphics#getClip()
	 */
	@Override
	public Shape getClip() {
		return null;
	}

	/**
	 * @see java.awt.Graphics#getClipBounds()
	 */
	@Override
	public Rectangle getClipBounds() {
		return null;
	}

	/**
	 * @see java.awt.Graphics#getColor()
	 */
	@Override
	public Color getColor() {
		return null;
	}

	/**
	 * @see java.awt.Graphics#getFont()
	 */
	@Override
	public Font getFont() {
		return null;
	}

	/**
	 * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
	 */
	@Override
	public FontMetrics getFontMetrics(Font arg0) {
		return null;
	}

	/**
	 * @see java.awt.Graphics#setClip(java.awt.Shape)
	 */
	@Override
	public void setClip(Shape arg0) {
		
	}

	/**
	 * @see java.awt.Graphics#setClip(int, int, int, int)
	 */
	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		
	}

	/**
	 * @see java.awt.Graphics#setColor(java.awt.Color)
	 */
	@Override
	public void setColor(Color arg0) {
		
	}

	/**
	 * @see java.awt.Graphics#setFont(java.awt.Font)
	 */
	@Override
	public void setFont(Font arg0) {
		
	}

	/**
	 * @see java.awt.Graphics#setPaintMode()
	 */
	@Override
	public void setPaintMode() {
		
	}

	/**
	 * @see java.awt.Graphics#setXORMode(java.awt.Color)
	 */
	@Override
	public void setXORMode(Color arg0) {
		
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
	
	/**
	 * @see net.sf.wubiq.interfaces.IRemoteAdapter#getObjectUUID()
	 */
	public UUID getObjectUUID() {
		return this.objectUUID;
	}


}
