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
public class GraphicsRecorder extends Graphics2D {
	private static final Log LOG = LogFactory.getLog(GraphicsRecorder.class);
	private List<GraphicCommand> graphicCommands;
	private List<String>unimplemented;
	private transient Graphics2D originalGraphics;
	private transient Color background;
	private transient Composite composite;
	private transient GraphicsConfiguration deviceConfiguration;
	private transient FontRenderContext fontRenderContext;
	private transient Paint paint;
	private transient RenderingHints renderingHints;
	private transient Stroke stroke;
	private transient AffineTransform transform;
	private transient Shape clip;
	private transient Rectangle clipBounds;
	private transient Color color;
	private transient Font font;

	
	public GraphicsRecorder() {
		unimplemented = new ArrayList<String>();
	}

	public GraphicsRecorder(List<GraphicCommand> graphicCommands) {
		this();
		this.graphicCommands = graphicCommands;
	}
	
	public GraphicsRecorder(List<GraphicCommand> graphicCommands, Graphics2D originalGraphics) {
		this(graphicCommands);
		this.originalGraphics = originalGraphics;
		this.background = originalGraphics.getBackground();
		this.composite = originalGraphics.getComposite();
		this.deviceConfiguration = originalGraphics.getDeviceConfiguration();
		this.fontRenderContext = originalGraphics.getFontRenderContext();
		this.paint = originalGraphics.getPaint();
		this.renderingHints = originalGraphics.getRenderingHints();
		this.stroke = originalGraphics.getStroke();
		this.transform = originalGraphics.getTransform();
		this.clip = originalGraphics.getClip();
		this.clipBounds = originalGraphics.getClipBounds();
		this.color = originalGraphics.getColor();
		this.font = originalGraphics.getFont();
		
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
			graphicCommands.add(new GraphicCommand(command, parameters));
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
		addToCommands("addRenderingHints", new GraphicParameter(Map.class, hints));
		renderingHints.add((RenderingHints) hints);
	}

	@Override
	public void clip(Shape s) {
		addToCommands("clip", new GraphicParameter(Shape.class, s));
	}

	@Override
	public void draw(Shape s) {
		addToCommands("draw", new GraphicParameter(Shape.class, s));
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		addToCommands("drawGlyphVector", new GraphicParameter(GlyphVector.class, g), new GraphicParameter(float.class, x), 
			new GraphicParameter(float.class, y));
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		addToCommands("drawImage", new GraphicParameter(ImageWrapper.class, new ImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform), 
				new GraphicParameter(ImageObserverWrapper.class, new ImageObserverWrapper(obs)));
		return true;
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		addToCommands("drawImage", new GraphicParameter(BufferedImage.class, img), new GraphicParameter(BufferedImageOp.class, op), 
			new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		addToCommands("drawRenderableImage", new GraphicParameter(RenderableImageWrapper.class, new RenderableImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		addToCommands("drawRenderedImage", new GraphicParameter(RenderedImage.class, img), new GraphicParameter(AffineTransform.class, xform));
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
	}

	@Override
	public void fill(Shape s) {
		addToCommands("fill", new GraphicParameter(Shape.class, s));
	}

	@Override
	public Color getBackground() {
		return background;
	}

	@Override
	public Composite getComposite() {
		return composite;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return deviceConfiguration;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return fontRenderContext;
	}

	@Override
	public Paint getPaint() {
		return paint;
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return renderingHints.get(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return renderingHints;
	}

	@Override
	public Stroke getStroke() {
		return stroke;
	}

	@Override
	public AffineTransform getTransform() {
		return transform;
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return false;
	}

	@Override
	public void rotate(double theta) {
		addToCommands("rotate", new GraphicParameter(double.class, theta));
	}

	@Override
	public void rotate(double theta, double x, double y) {
		addToCommands("rotate", new GraphicParameter(double.class, theta), new GraphicParameter(double.class, x), 
			new GraphicParameter(double.class, y));
	}

	@Override
	public void scale(double sx, double sy) {
		addToCommands("scale", new GraphicParameter(double.class, sx), new GraphicParameter(double.class, sy));
	}

	@Override
	public void setBackground(Color color) {
		addToCommands("setBackground", new GraphicParameter(Color.class, color));
		this.background = color;
	}

	@Override
	public void setComposite(Composite comp) {
		addToCommands("setComposite", new GraphicParameter(Composite.class, comp));
		this.composite = comp;
	}

	@Override
	public void setPaint(Paint paint) {
		addToCommands("setPaint", new GraphicParameter(Paint.class, paint));
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		addToCommands("setRenderingHint", new GraphicParameter(Key.class, hintKey), new GraphicParameter(Object.class, hintValue));
	}

	@Override
	public void setRenderingHints(Map <?, ?> hints) {
		addToCommands("setRenderingHints", new GraphicParameter(Map.class, hints));
	}

	@Override
	public void setStroke(Stroke s) {
		addToCommands("setStroke", new GraphicParameter(Stroke.class, s));
	}

	@Override
	public void setTransform(AffineTransform Tx) {
		addToCommands("setTransform", new GraphicParameter(AffineTransform.class, Tx));
	}

	@Override
	public void shear(double shx, double shy) {
		addToCommands("shear", new GraphicParameter(double.class, shx), new GraphicParameter(double.class, shy));
	}

	@Override
	public void transform(AffineTransform Tx) {
		addToCommands("transform", new GraphicParameter(AffineTransform.class, Tx));
	}

	@Override
	public void translate(int x, int y) {
		addToCommands("translate", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
	}

	@Override
	public void translate(double tx, double ty) {
		addToCommands("translate", new GraphicParameter(double.class, tx), new GraphicParameter(double.class, ty));
	}

	@Override
	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("clearRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("clipRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		addToCommands("copyArea", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
			new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5));
	}

	@Override
	public Graphics create() {
		return null;
	}

	@Override
	public void dispose() {
		addToCommands("dispose");
	}

	@Override
	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		addToCommands("drawArc", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
			new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5));
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(ImageObserver.class, arg3));
		return true;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
				new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
				new GraphicParameter(ImageObserver.class, arg4));
		return true;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
			new GraphicParameter(int.class, arg4), new GraphicParameter(ImageObserver.class, arg5));
		return true;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
			new GraphicParameter(int.class, arg4), new GraphicParameter(Color.class, arg5),
			new GraphicParameter(ImageObserver.class, arg6));
		return true;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
			new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5),
			new GraphicParameter(int.class, arg6), new GraphicParameter(int.class, arg7),
			new GraphicParameter(ImageObserver.class, arg9));
		return true;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		addToCommands("drawImage", new GraphicParameter(Image.class, arg0), new GraphicParameter(int.class, arg1), 
				new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
				new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5),
				new GraphicParameter(int.class, arg6), new GraphicParameter(int.class, arg7),
				new GraphicParameter(Color.class, arg9), new GraphicParameter(ImageObserver.class, arg10));
		return true;
	}

	@Override
	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("drawLine", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("drawOval", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		addToCommands("drawPolygon", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
	}

	@Override
	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		addToCommands("drawPolyline", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
	}

	@Override
	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		addToCommands("drawRoundRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
				new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
				new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5));
	}

	@Override
	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		addToCommands("fillArc", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
				new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
				new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5));
	}

	@Override
	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("fillOval", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		addToCommands("fillPolygon", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
	}

	@Override
	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("fillRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		addToCommands("fillRoundRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
				new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3),
				new GraphicParameter(int.class, arg4), new GraphicParameter(int.class, arg5));
	}

	@Override
	public Shape getClip() {
		return clip;
	}

	@Override
	public Rectangle getClipBounds() {
		return clipBounds;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public Font getFont() {
		return font;
	}

	@Override
	public FontMetrics getFontMetrics(Font arg0) {
		return originalGraphics.getFontMetrics(arg0);
	}

	@Override
	public void setClip(Shape arg0) {
		addToCommands("setClip", new GraphicParameter(Shape.class, arg0));
		clip = arg0;
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		addToCommands("setClip", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
	}

	@Override
	public void setColor(Color arg0) {
		addToCommands("setColor", new GraphicParameter(Color.class, arg0));
		color = arg0;
	}

	@Override
	public void setFont(Font arg0) {
		addToCommands("setFont", new GraphicParameter(Font.class, arg0));
		font = arg0;
	}

	@Override
	public void setPaintMode() {
		addToCommands("setPaintMode");
	}

	@Override
	public void setXORMode(Color arg0) {
		addToCommands("setXORMode", new GraphicParameter(Color.class, arg0));
	}
	

}
