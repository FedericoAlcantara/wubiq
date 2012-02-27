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
				.insert(0,  command)
				.append(')');
			if (!unimplemented.contains(fullCommand.toString())) {
				unimplemented.add(fullCommand.toString());
				LOG.info("Method not implemented: " + fullCommand.toString());
			}
		}
	}
	
	public GraphicsRecorder() {
		unimplemented = new ArrayList<String>();
	}

	public GraphicsRecorder(List<GraphicCommand> graphicCommands) {
		this();
		this.graphicCommands = graphicCommands;
	}

	@Override
	public void addRenderingHints(Map <?, ?>  hints) {
		addToCommands("addRenderingHints", new GraphicParameter(Map.class, hints));
		
	}

	@Override
	public void clip(Shape  s) {
		addToCommands("clip", new GraphicParameter(Shape.class, s));
		
	}

	@Override
	public void draw(Shape  s) {
		addToCommands("draw", new GraphicParameter(Shape.class, s));
		
	}

	@Override
	public void drawGlyphVector(GlyphVector  g, float  x, float  y) {
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
	public void drawImage(BufferedImage  img, BufferedImageOp  op, int  x, int  y) {
		addToCommands("drawImage", new GraphicParameter(BufferedImage.class, img), new GraphicParameter(BufferedImageOp.class, op), 
			new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
	}

	@Override
	public void drawRenderableImage(RenderableImage  img, AffineTransform  xform) {
		addToCommands("drawRenderableImage", new GraphicParameter(RenderableImageWrapper.class, new RenderableImageWrapper(img)), new GraphicParameter(AffineTransform.class, xform));
	}

	@Override
	public void drawRenderedImage(RenderedImage  img, AffineTransform  xform) {
		addToCommands("drawRenderedImage", new GraphicParameter(RenderedImage.class, img), new GraphicParameter(AffineTransform.class, xform));
		
	}

	@Override
	public void drawString(String  str, int  x, int  y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		
	}

	@Override
	public void drawString(String  str, float  x, float  y) {
		addToCommands("drawString", new GraphicParameter(String.class, str), new GraphicParameter(float.class, x), 
			new GraphicParameter(float.class, y));
		
	}

	@Override
	public void drawString(AttributedCharacterIterator  iterator, int  x, int  y) {
		addToCommands("drawString", new GraphicParameter(AttributedCharacterIterator.class, iterator), new GraphicParameter(int.class, x), 
			new GraphicParameter(int.class, y));
		
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		
	}

	@Override
	public void fill(Shape  s) {
		addToCommands("fill", new GraphicParameter(Shape.class, s));
		
	}

	@Override
	public Color getBackground() {
		return null;
	}

	@Override
	public Composite getComposite() {
		return null;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return null;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return null;
	}

	@Override
	public Paint getPaint() {
		return null;
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	@Override
	public Stroke getStroke() {
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		return null;
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return false;
	}

	@Override
	public void rotate(double  theta) {
		addToCommands("rotate", new GraphicParameter(double.class, theta));
		
	}

	@Override
	public void rotate(double  theta, double  x, double  y) {
		addToCommands("rotate", new GraphicParameter(double.class, theta), new GraphicParameter(double.class, x), 
			new GraphicParameter(double.class, y));
		
	}

	@Override
	public void scale(double  sx, double  sy) {
		addToCommands("scale", new GraphicParameter(double.class, sx), new GraphicParameter(double.class, sy));
		
	}

	@Override
	public void setBackground(Color  color) {
		addToCommands("setBackground", new GraphicParameter(Color.class, color));
		
	}

	@Override
	public void setComposite(Composite  comp) {
		addToCommands("setComposite", new GraphicParameter(Composite.class, comp));
		
	}

	@Override
	public void setPaint(Paint  paint) {
		addToCommands("setPaint", new GraphicParameter(Paint.class, paint));
		
	}

	@Override
	public void setRenderingHint(Key  hintKey, Object  hintValue) {
		addToCommands("setRenderingHint", new GraphicParameter(Key.class, hintKey), new GraphicParameter(Object.class, hintValue));
		
	}

	@Override
	public void setRenderingHints(Map <?, ?>  hints) {
		addToCommands("setRenderingHints", new GraphicParameter(Map.class, hints));
		
	}

	@Override
	public void setStroke(Stroke  s) {
		addToCommands("setStroke", new GraphicParameter(Stroke.class, s));
		
	}

	@Override
	public void setTransform(AffineTransform  Tx) {
		addToCommands("setTransform", new GraphicParameter(AffineTransform.class, Tx));
		
	}

	@Override
	public void shear(double  shx, double  shy) {
		addToCommands("shear", new GraphicParameter(double.class, shx), new GraphicParameter(double.class, shy));
		
	}

	@Override
	public void transform(AffineTransform  Tx) {
		addToCommands("transform", new GraphicParameter(AffineTransform.class, Tx));
		
	}

	@Override
	public void translate(int  x, int  y) {
		addToCommands("translate", new GraphicParameter(int.class, x), new GraphicParameter(int.class, y));
		
	}

	@Override
	public void translate(double  tx, double  ty) {
		addToCommands("translate", new GraphicParameter(double.class, tx), new GraphicParameter(double.class, ty));
		
	}

	@Override
	public void clearRect(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("clearRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void clipRect(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("clipRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
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
		
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		return false;
	}

	@Override
	public void drawLine(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("drawLine", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void drawOval(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("drawOval", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void drawPolygon(int[]  arg0, int[]  arg1, int  arg2) {
		addToCommands("drawPolygon", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
		
	}

	@Override
	public void drawPolyline(int[]  arg0, int[]  arg1, int  arg2) {
		addToCommands("drawPolyline", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
		
	}

	@Override
	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	@Override
	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	@Override
	public void fillOval(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("fillOval", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void fillPolygon(int[]  arg0, int[]  arg1, int  arg2) {
		addToCommands("fillPolygon", new GraphicParameter(int[].class, arg0), new GraphicParameter(int[].class, arg1), 
			new GraphicParameter(int.class, arg2));
		
	}

	@Override
	public void fillRect(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("fillRect", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		
	}

	@Override
	public Shape getClip() {
		return null;
	}

	@Override
	public Rectangle getClipBounds() {
		return null;
	}

	@Override
	public Color getColor() {
		return null;
	}

	@Override
	public Font getFont() {
		return null;
	}

	@Override
	public FontMetrics getFontMetrics(Font arg0) {
		return null;
	}

	@Override
	public void setClip(Shape  arg0) {
		addToCommands("setClip", new GraphicParameter(Shape.class, arg0));
		
	}

	@Override
	public void setClip(int  arg0, int  arg1, int  arg2, int  arg3) {
		addToCommands("setClip", new GraphicParameter(int.class, arg0), new GraphicParameter(int.class, arg1), 
			new GraphicParameter(int.class, arg2), new GraphicParameter(int.class, arg3));
		
	}

	@Override
	public void setColor(Color  arg0) {
		addToCommands("setColor", new GraphicParameter(Color.class, arg0));
		
	}

	@Override
	public void setFont(Font  arg0) {
		addToCommands("setFont", new GraphicParameter(Font.class, arg0));
		
	}

	@Override
	public void setPaintMode() {
		addToCommands("setPaintMode");
		
	}

	@Override
	public void setXORMode(Color  arg0) {
		addToCommands("setXORMode", new GraphicParameter(Color.class, arg0));
		
	}
	

}
