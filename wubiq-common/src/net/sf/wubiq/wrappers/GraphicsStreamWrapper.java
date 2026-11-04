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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.awt.font.TextAttribute;
import java.awt.print.PageFormat;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.sf.wubiq.dotmatrix.GraphicsRaster;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.utils.GraphicsUtils;
import net.sf.wubiq.utils.PrintServiceUtils;

/**
 * @author Federico Alcantara
 *
 */
public class GraphicsStreamWrapper extends Graphics2D {
	/** Spaces that can separate two characters drawn one by one for them to be one text. */
	private static final int MAXIMUM_JOINED_SPACES = 4;
	/** Glyph code to character, per font. Glyph vectors don't carry the text they draw. */
	private static final Map<Font, Map<Integer, Character>> GLYPH_CHARACTERS =
			Collections.synchronizedMap(new WeakHashMap<Font, Map<Integer, Character>>());
	
	private Graphics2D graphics;
	private Capture capture;
	private PrinterType printerType;
	private Font currentFont;
	private AffineTransform entryTransform;
	private float originX;
	private float originY;
	/** Where shapes and images are drawn, or null when only the text of the page matters. */
	private GraphicsRaster raster;
	private Graphics2D rasterGraphics;
	private AffineTransform rasterEntryTransform;
	
	public GraphicsStreamWrapper(Graphics2D graphics) {
		this(graphics, null);
	}
	
	/**
	 * @param graphics Graphics to wrap.
	 * @param pageFormat Page format of the page, whose imageable area is where the application's 0, 0 lies.
	 */
	public GraphicsStreamWrapper(Graphics2D graphics, PageFormat pageFormat) {
		this(graphics, pageFormat == null ? 0 : (float) pageFormat.getImageableX(),
				pageFormat == null ? 0 : (float) pageFormat.getImageableY());
	}
	
	/**
	 * @param graphics Graphics to wrap.
	 * @param originX Position, in points from the left of what the printer prints on, of the application's 0, 0.
	 * @param originY Position, in points from the top, of the application's 0, 0.
	 */
	public GraphicsStreamWrapper(Graphics2D graphics, float originX, float originY) {
		this(graphics, originX, originY, null);
	}
	
	/**
	 * Wrapper that keeps the page's text and draws its shapes and images on a raster.
	 * <p>The wrapped graphics only holds the state the application reads (transform, clip, color, font metrics)
	 * and is never drawn on. The state the application sets (transform, clip, color, paint, stroke, composite)
	 * is also set on the raster's graphics, so that the shapes and images land where the application meant them.
	 * Rendering hints are not: the raster is never antialiased, as a dot is printed or not, and a thin light
	 * line antialiased is too faint to print.</p>
	 * @param graphics Graphics to wrap.
	 * @param originX Position, in points from the left of what the printer prints on, of the application's 0, 0.
	 * @param originY Position, in points from the top, of the application's 0, 0.
	 * @param raster Where shapes and images are drawn; null to leave them out.
	 */
	public GraphicsStreamWrapper(Graphics2D graphics, float originX, float originY, GraphicsRaster raster) {
		this.graphics = graphics;
		this.entryTransform = graphics.getTransform();
		this.originX = originX;
		this.originY = originY;
		capture = new Capture();
		printerType = PrintServiceUtils.printerType(graphics.getDeviceConfiguration().getDevice().getIDstring());
		currentFont = graphics.getFont();
		if (currentFont == null) {
			currentFont = Font.decode(Font.SERIF);
		}
		if (raster != null) {
			this.raster = raster;
			rasterGraphics = raster.createGraphics();
			rasterEntryTransform = rasterGraphics.getTransform();
		}
	}

	/**
	 * Wrapper for what a child graphics (create()) draws: its text goes to the same page.
	 */
	private GraphicsStreamWrapper(GraphicsStreamWrapper parent) {
		this.graphics = (Graphics2D) parent.graphics.create();
		this.capture = parent.capture;
		this.entryTransform = parent.entryTransform;
		this.originX = parent.originX;
		this.originY = parent.originY;
		this.printerType = parent.printerType;
		this.currentFont = parent.currentFont;
		this.raster = parent.raster;
		if (parent.rasterGraphics != null) {
			this.rasterGraphics = (Graphics2D) parent.rasterGraphics.create();
			this.rasterEntryTransform = parent.rasterEntryTransform;
		}
	}
	
	/**
	 * @return The texts the application drew, in page, line and column order. Texts at the same
	 * position stay in the order they were drawn.
	 */
	public List<TextField> getTexts() {
		List<TextField> sorted = new ArrayList<TextField>(capture.texts);
		Collections.sort(sorted); // Stable.
		return joinGlyphs(sorted);
	}
	
	/**
	 * Some applications and renderers (a PDF page, for one) draw a text one character at a time, each at its
	 * own proportional position. A printer can't print it so: its characters have a fixed width, and
	 * characters at those positions overlap or leave holes. Characters drawn one by one on the same line
	 * and in the same style are joined into one text that starts where the first does; a gap between two
	 * of them (a space that wasn't drawn) becomes spaces. Texts drawn as a whole are left as they are.
	 */
	private List<TextField> joinGlyphs(List<TextField> sorted) {
		List<TextField> returnValue = new ArrayList<TextField>();
		List<TextField> chain = new ArrayList<TextField>();
		for (TextField textField : sorted) {
			boolean glyph = capture.glyphs.containsKey(textField);
			if (!chain.isEmpty() && !(glyph && sameStyle(chain.get(0), textField))) {
				joinChain(chain, returnValue);
				chain.clear();
			}
			if (glyph) {
				chain.add(textField);
			} else {
				returnValue.add(textField);
			}
		}
		joinChain(chain, returnValue);
		return returnValue;
	}
	
	/**
	 * Joins characters drawn one by one along a line. The renderer's font can differ from the one the JVM
	 * has, so the width the JVM gives a character is corrected by the ratio most of the characters
	 * of the line show between their distance and their width.
	 */
	private void joinChain(List<TextField> chain, List<TextField> returnValue) {
		int size = chain.size();
		if (size == 0) {
			return;
		}
		List<Float> ratios = new ArrayList<Float>();
		for (int index = 0; index < size - 1; index++) {
			float width = capture.glyphs.get(chain.get(index))[0];
			if (width > 0) {
				ratios.add((chain.get(index + 1).getX() - chain.get(index).getX()) / width);
			}
		}
		float scale = 1;
		if (!ratios.isEmpty()) {
			Collections.sort(ratios);
			scale = ratios.get(ratios.size() / 2);
		}
		TextField first = chain.get(0);
		StringBuilder text = new StringBuilder(first.getText());
		for (int index = 1; index < size; index++) {
			TextField previous = chain.get(index - 1);
			TextField current = chain.get(index);
			float[] glyph = capture.glyphs.get(previous);
			float gap = current.getX() - previous.getX() - scale * glyph[0];
			float space = scale * glyph[1];
			// The renderer's font isn't the JVM's: a gap smaller than a space is not a space.
			int spaces = space > 0 ? Math.max((int) Math.floor(gap / space + 0.1f), 0) : 0;
			if (spaces > MAXIMUM_JOINED_SPACES) {
				returnValue.add(joined(first, text));
				first = current;
				text = new StringBuilder(current.getText());
			} else {
				for (int count = 0; count < spaces; count++) {
					text.append(' ');
				}
				text.append(current.getText());
			}
		}
		returnValue.add(joined(first, text));
	}
	
	private static boolean sameStyle(TextField one, TextField other) {
		return Math.abs(one.getY() - other.getY()) < 0.5f && one.getFontSize() == other.getFontSize()
				&& one.isBold() == other.isBold() && one.isItalic() == other.isItalic()
				&& one.isUnderline() == other.isUnderline();
	}
	
	private static TextField joined(TextField first, StringBuilder text) {
		TextField returnValue = new TextField(first.getPage(), first.getX(), first.getY(), first.getFontSize(), text.toString());
		returnValue.setBold(first.isBold());
		returnValue.setItalic(first.isItalic());
		returnValue.setUnderline(first.isUnderline());
		return returnValue;
	}
	
	public PrinterType getPrinterType() {
		return printerType;
	}
	
	/**
	 * @param transform Device transform of the wrapped graphics.
	 * @return The same transform for the raster's graphics: what the application added to the page's own
	 * transform, on top of the raster's.
	 */
	private AffineTransform rasterTransform(AffineTransform transform) {
		AffineTransform returnValue = new AffineTransform(rasterEntryTransform);
		try {
			returnValue.concatenate(entryTransform.createInverse());
		} catch (NoninvertibleTransformException e) {
			// The transform is used as is.
		}
		returnValue.concatenate(transform);
		return returnValue;
	}
	
	/**
	 * Marks the raster as drawn on.
	 * @return The raster's graphics, or null if shapes and images are left out.
	 */
	private Graphics2D drawing() {
		if (rasterGraphics != null) {
			raster.markDrawn();
		}
		return rasterGraphics;
	}
	
	/**
	 * Adds a text drawn at the given user space position. The position is converted to points on the
	 * page: the transform the JDK gave the page (device scale, imageable area) is undone, what the
	 * application added (translate, scale) is applied.
	 */
	private void addText(String str, double x, double y) {
		addText(str, x, y, currentFont);
	}
	
	private void addText(String str, double x, double y, Font font) {
		AffineTransform relative = new AffineTransform();
		try {
			relative.concatenate(entryTransform.createInverse());
		} catch (NoninvertibleTransformException e) {
			// The page transform is used as is.
		}
		relative.concatenate(graphics.getTransform());
		Point2D position = relative.transform(new Point2D.Double(x, y), null);
		double scale = Math.sqrt(Math.abs(relative.getDeterminant()));
		int size = (int) Math.round(font.getSize2D() * scale);
		TextField textField = new TextField(0, originX + (float) position.getX(), originY + (float) position.getY(), size, str);
		textField.setBold(font.isBold());
		textField.setItalic(font.isItalic());
		textField.setUnderline(TextAttribute.UNDERLINE_ON.equals(font.getAttributes().get(TextAttribute.UNDERLINE)));
		if (capture.add(textField) && str.length() == 1) {
			FontRenderContext context = new FontRenderContext(null, true, true);
			capture.glyphs.put(textField, new float[] {
					(float) (font.getStringBounds(str, context).getWidth() * scale),
					(float) (font.getStringBounds(" ", context).getWidth() * scale)});
		}
	}
	
	/**
	 * @return The characters a glyph vector draws, '?' for a glyph that no character of the font gives.
	 */
	private String glyphText(GlyphVector glyphVector) {
		Font font = glyphVector.getFont();
		Map<Integer, Character> characters = GLYPH_CHARACTERS.get(font);
		if (characters == null) {
			characters = new HashMap<Integer, Character>();
			FontRenderContext context = glyphVector.getFontRenderContext();
			int[][] ranges = {{0x20, 0x7E}, {0xA0, 0x17F}, {0x2013, 0x2014}, {0x2018, 0x201D}, {0x20AC, 0x20AC}};
			for (int[] range : ranges) {
				for (int character = range[0]; character <= range[1]; character++) {
					int code = font.createGlyphVector(context, new char[] {(char) character}).getGlyphCode(0);
					if (code != font.getMissingGlyphCode() && !characters.containsKey(code)) {
						characters.put(code, (char) character);
					}
				}
			}
			GLYPH_CHARACTERS.put(font, characters);
		}
		StringBuilder returnValue = new StringBuilder();
		for (int index = 0; index < glyphVector.getNumGlyphs(); index++) {
			Character character = characters.get(glyphVector.getGlyphCode(index));
			returnValue.append(character == null ? '?' : character.charValue());
		}
		return returnValue.toString();
	}
	
	/**
	 * What the page's graphics and their children (create()) have drawn.
	 */
	private static class Capture {
		private List<TextField> texts = new ArrayList<TextField>();
		private Set<String> drawn = new HashSet<String>();
		/** Width of the character and of a space, in points, of the texts drawn as one character. */
		private Map<TextField, float[]> glyphs = new IdentityHashMap<TextField, float[]>();
		
		/**
		 * Adds the text, unless the same text was already drawn at the same place in the same style
		 * (applications draw a text twice to make it look bold; the printer would only overstrike it).
		 * Different texts at the same place are all kept.
		 */
		boolean add(TextField textField) {
			String key = textField.getX() + "|" + textField.getY() + "|" + textField.getFontSize() + "|"
					+ textField.isBold() + "|" + textField.isItalic() + "|" + textField.isUnderline() + "|"
					+ textField.getText();
			if (drawn.add(key)) {
				texts.add(textField);
				return true;
			}
			return false;
		}
	}
	
	@Override
	public void clearRect(int x, int y, int width, int height) {
		Graphics2D g = drawing();
		if (g != null) {
			g.clearRect(x, y, width, height);
		}
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		if (rasterGraphics != null) {
			rasterGraphics.clipRect(x, y, width, height);
		}
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		Graphics2D g = drawing();
		if (g != null) {
			g.copyArea(x, y, width, height, dx, dy);
		}
	}

	@Override
	public Graphics create() {
		return new GraphicsStreamWrapper(this);
	}

	@Override
	public void dispose() {
		graphics.dispose();
		if (rasterGraphics != null) {
			rasterGraphics.dispose();
		}
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawArc(x, y, width, height, startAngle, arcAngle);
		}
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor,
			ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, x, y, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height,
			Color bgcolor, ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2,
			int sx1, int sy1, int sx2, int sy2, Color bgcolor,
			ImageObserver observer) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawLine(x1, y1, x2, y2);
		}
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawOval(x, y, width, height);
		}
	}

	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawPolygon(xPoints, yPoints, nPoints);
		}
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawPolyline(xPoints, yPoints, nPoints);
		}
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		}
	}

	@Override
	public void drawString(String str, int x, int y) {
		addText(str, x, y);
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
		Graphics2D g = drawing();
		if (g != null) {
			g.fillArc(x, y, width, height, startAngle, arcAngle);
		}
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		Graphics2D g = drawing();
		if (g != null) {
			g.fillOval(x, y, width, height);
		}
	}

	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Graphics2D g = drawing();
		if (g != null) {
			g.fillPolygon(xPoints, yPoints, nPoints);
		}
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		Graphics2D g = drawing();
		if (g != null) {
			g.fillRect(x, y, width, height);
		}
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Graphics2D g = drawing();
		if (g != null) {
			g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
		}
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
		if (rasterGraphics != null) {
			rasterGraphics.setClip(clip);
		}
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		graphics.setClip(x, y, width, height);
		if (rasterGraphics != null) {
			rasterGraphics.setClip(x, y, width, height);
		}
	}

	@Override
	public void setColor(Color c) {
		graphics.setColor(c);
		if (rasterGraphics != null) {
			rasterGraphics.setColor(c);
		}
	}

	@Override
	public void setFont(Font originalFont) {
		currentFont = GraphicsUtils.INSTANCE.properFont(originalFont, printerType);
		graphics.setFont(currentFont);
	}

	@Override
	public void setPaintMode() {
		graphics.setPaintMode();
		if (rasterGraphics != null) {
			rasterGraphics.setPaintMode();
		}
	}

	@Override
	public void setXORMode(Color c1) {
		graphics.setXORMode(c1);
		if (rasterGraphics != null) {
			rasterGraphics.setXORMode(c1);
		}
	}

	@Override
	public void translate(int x, int y) {
		graphics.translate(x, y);
		if (rasterGraphics != null) {
			rasterGraphics.translate(x, y);
		}
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		graphics.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape s) {
		graphics.clip(s);
		if (rasterGraphics != null) {
			rasterGraphics.clip(s);
		}
	}

	@Override
	public void draw(Shape s) {
		Graphics2D g = drawing();
		if (g != null) {
			g.draw(s);
		}
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		String text = glyphText(g);
		if (text.length() > 0) {
			addText(text, x, y, g.getFont());
		}
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform,
			ImageObserver obs) {
		Graphics2D g = drawing();
		return g == null || g.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x,
			int y) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawImage(img, op, x, y);
		}
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawRenderableImage(img, xform);
		}
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		Graphics2D g = drawing();
		if (g != null) {
			g.drawRenderedImage(img, xform);
		}
	}

	@Override
	public void drawString(String str, float x, float y) {
		addText(str, x, y);
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x,
			float y) {
		StringBuffer str = new StringBuffer("");
		for (char charAt = iterator.first(); charAt != CharacterIterator.DONE; charAt = iterator.next()) {
			str.append(charAt);
		}
		if (str.length() > 0) {
			addText(str.toString(), x, y);
		}
	}

	@Override
	public void fill(Shape s) {
		Graphics2D g = drawing();
		if (g != null) {
			g.fill(s);
		}
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
		if (rasterGraphics != null) {
			rasterGraphics.rotate(theta);
		}
	}

	@Override
	public void rotate(double theta, double x, double y) {
		graphics.rotate(theta, x, y);
		if (rasterGraphics != null) {
			rasterGraphics.rotate(theta, x, y);
		}
	}

	@Override
	public void scale(double sx, double sy) {
		graphics.scale(sx, sy);
		if (rasterGraphics != null) {
			rasterGraphics.scale(sx, sy);
		}
	}

	@Override
	public void setBackground(Color color) {
		graphics.setBackground(color);
		if (rasterGraphics != null) {
			rasterGraphics.setBackground(color);
		}
	}

	@Override
	public void setComposite(Composite comp) {
		graphics.setComposite(comp);
		if (rasterGraphics != null) {
			rasterGraphics.setComposite(comp);
		}
	}

	@Override
	public void setPaint(Paint paint) {
		graphics.setPaint(paint);
		if (rasterGraphics != null) {
			rasterGraphics.setPaint(paint);
		}
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
		if (rasterGraphics != null) {
			rasterGraphics.setStroke(s);
		}
	}

	@Override
	public void setTransform(AffineTransform tx) {
		graphics.setTransform(tx);
		if (rasterGraphics != null) {
			rasterGraphics.setTransform(rasterTransform(tx));
		}
	}

	@Override
	public void shear(double shx, double shy) {
		graphics.shear(shx, shy);
		if (rasterGraphics != null) {
			rasterGraphics.shear(shx, shy);
		}
	}

	@Override
	public void transform(AffineTransform tx) {
		graphics.transform(tx);
		if (rasterGraphics != null) {
			rasterGraphics.transform(tx);
		}
	}

	@Override
	public void translate(double tx, double ty) {
		graphics.translate(tx, ty);
		if (rasterGraphics != null) {
			rasterGraphics.translate(tx, ty);
		}
	}	
}
