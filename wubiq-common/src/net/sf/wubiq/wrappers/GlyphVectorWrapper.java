/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class GlyphVectorWrapper extends GlyphVector implements Serializable {
	private static final long serialVersionUID = 1L;
	private Font font;
	private FontRenderContextWrapper fontRenderContext;
	private int[] codes;
	private GlyphJustificationInfoWrapper[] justificationInfos;
	private ShapeWrapper[] glyphLogicalBounds;
	private GlyphMetricsWrapper[] metrics;
	private ShapeWrapper[] outlines;
	private float[] floatPositions;
	private AffineTransform[] transforms;
	private ShapeWrapper[] glyphVisualBounds;
	private int layoutFlags;
	private Rectangle2DWrapper logicalBounds;
	private int numGlyphs;
	private ShapeWrapper outline;
	private Rectangle2DWrapper visualBounds;
	
	public GlyphVectorWrapper() {
	}

	public GlyphVectorWrapper(GlyphVector glyphVector) {
		if (glyphVector != null) {
			glyphVector.performDefaultLayout();
			this.font = glyphVector.getFont();
			if (font.getName().toLowerCase().contains("draft")) {
				//font = new Font("Courier new", font.getStyle(), font.getSize());
			}
			this.fontRenderContext = new FontRenderContextWrapper(glyphVector.getFontRenderContext());
			this.layoutFlags = glyphVector.getLayoutFlags();
			this.logicalBounds = new Rectangle2DWrapper(glyphVector.getLogicalBounds());
			this.numGlyphs = glyphVector.getNumGlyphs();
			this.outline = new ShapeWrapper(glyphVector.getOutline());
			this.visualBounds = new Rectangle2DWrapper(glyphVector.getVisualBounds());
			initializeGlyphs(glyphVector);
		}
	}

	private void initializeGlyphs(GlyphVector glyphVector) {
		codes = new int[numGlyphs];
		justificationInfos = new GlyphJustificationInfoWrapper[numGlyphs];
		glyphLogicalBounds = new ShapeWrapper[numGlyphs];
		metrics = new GlyphMetricsWrapper[numGlyphs];
		outlines = new ShapeWrapper[numGlyphs];
		transforms = new AffineTransform[numGlyphs];
		glyphVisualBounds = new ShapeWrapper[numGlyphs];
		floatPositions = glyphVector.getGlyphPositions(0, numGlyphs + 1, null);
		for (int glyphIndex = 0; glyphIndex < numGlyphs; glyphIndex++) {
			codes[glyphIndex] = glyphVector.getGlyphCode(glyphIndex);
			justificationInfos[glyphIndex] = new GlyphJustificationInfoWrapper(glyphVector.getGlyphJustificationInfo(glyphIndex));
			glyphLogicalBounds[glyphIndex] = new ShapeWrapper(glyphVector.getGlyphLogicalBounds(glyphIndex));
			metrics[glyphIndex] = new GlyphMetricsWrapper(glyphVector.getGlyphMetrics(glyphIndex));
			outlines[glyphIndex] = new ShapeWrapper(glyphVector.getGlyphOutline(glyphIndex));
			transforms[glyphIndex] = glyphVector.getGlyphTransform(glyphIndex);
			glyphVisualBounds[glyphIndex] = new ShapeWrapper(glyphVector.getGlyphVisualBounds(glyphIndex));
		}
	}
	
	/**
	 * @see java.awt.font.GlyphVector#equals(java.awt.font.GlyphVector)
	 */
	@Override
	public boolean equals(GlyphVector other) {
		return false;
	}

	/**
	 * @see java.awt.font.GlyphVector#getFont()
	 */
	@Override
	public Font getFont() {
		return font;
	}

	/**
	 * @see java.awt.font.GlyphVector#getFontRenderContext()
	 */
	@Override
	public FontRenderContext getFontRenderContext() {
		return fontRenderContext;
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphCode(int)
	 */
	@Override
	public int getGlyphCode(int glyphIndex) {
		return codes[glyphIndex];
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphCodes(int, int, int[])
	 */
	@Override
	public int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn) {
		int[] returnValue = codeReturn;
		if (codeReturn == null) {
			returnValue = new int[numEntries];
		}
		for (int i = 0; i < numEntries; i++) {
			returnValue[i] = codes[i + beginGlyphIndex];
		}
		return returnValue;
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphJustificationInfo(int)
	 */
	@Override
	public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
		return justificationInfos[glyphIndex].getInfo();
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphLogicalBounds(int)
	 */
	@Override
	public Shape getGlyphLogicalBounds(int glyphIndex) {
		return glyphLogicalBounds[glyphIndex].getShape();
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphMetrics(int)
	 */
	@Override
	public GlyphMetrics getGlyphMetrics(int glyphIndex) {
		return metrics[glyphIndex].getGlyphMetrics();
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphOutline(int)
	 */
	@Override
	public Shape getGlyphOutline(int glyphIndex) {
		return outlines[glyphIndex].getShape();
	}

	@Override
	public Shape getGlyphOutline(int glyphIndex, float x, float y) {
		GeneralPath gp = new GeneralPath(getGlyphOutline(glyphIndex));
		gp.transform(AffineTransform.getTranslateInstance(x, y));
		return gp;
	}
	/**
	 * @see java.awt.font.GlyphVector#getGlyphPosition(int)
	 */
	@Override
	public Point2D getGlyphPosition(int glyphIndex) {
		int index = glyphIndex * 2;
		return new Point2D.Float(floatPositions[index], floatPositions[index + 1]);
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphPositions(int, int, float[])
	 */
	@Override
	public float[] getGlyphPositions(int beginGlyphIndex, int numEntries, float[] positionReturn) {
		float[] returnValue = positionReturn;
		if (positionReturn == null) {
			returnValue = new float[numEntries * 2];
		}
		for (int i = 0; i < numEntries; i++) {
			int index = i * beginGlyphIndex;
			returnValue[i] = floatPositions[index];
			returnValue[i + 1] = floatPositions[index + 1];
		}
		return returnValue;
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphTransform(int)
	 */
	@Override
	public AffineTransform getGlyphTransform(int glyphIndex) {
		return transforms[glyphIndex];
	}

	/**
	 * @see java.awt.font.GlyphVector#getGlyphVisualBounds(int)
	 */
	@Override
	public Shape getGlyphVisualBounds(int glyphIndex) {
		return glyphVisualBounds[glyphIndex].getShape();
	}

	@Override
	public int getLayoutFlags() {
		return layoutFlags;
	}
	
	/**
	 * @see java.awt.font.GlyphVector#getLogicalBounds()
	 */
	@Override
	public Rectangle2D getLogicalBounds() {
		return logicalBounds.getRectangle2D();
	}

	/**
	 * @see java.awt.font.GlyphVector#getNumGlyphs()
	 */
	@Override
	public int getNumGlyphs() {
		return numGlyphs;
	}

	/**
	 * @see java.awt.font.GlyphVector#getOutline()
	 */
	@Override
	public Shape getOutline() {
		return outline.getShape();
	}

	/**
	 * @see java.awt.font.GlyphVector#getOutline(float, float)
	 */
	@Override
	public Shape getOutline(float x, float y) {
		GeneralPath gp = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		for (int glyphIndex = 0; glyphIndex < numGlyphs; glyphIndex++) {
			GeneralPath igp = new GeneralPath(getGlyphOutline(glyphIndex, x, y));
			PathIterator pi = igp.getPathIterator(null);
			gp.append(pi, false);
		}
		return gp;
	}

	/**
	 * @see java.awt.font.GlyphVector#getVisualBounds()
	 */
	@Override
	public Rectangle2D getVisualBounds() {
		return visualBounds.getRectangle2D();
	}

	/**
	 * @see java.awt.font.GlyphVector#performDefaultLayout()
	 */
	@Override
	public void performDefaultLayout() {
	}

	/**
	 * @see java.awt.font.GlyphVector#setGlyphPosition(int, java.awt.geom.Point2D)
	 */
	@Override
	public void setGlyphPosition(int glyphIndex, Point2D position) {
		int index = glyphIndex * 2;
		floatPositions[index] = (float) position.getX();
		floatPositions[index + 1] = (float) position.getY();
	}

	/**
	 * @see java.awt.font.GlyphVector#setGlyphTransform(int, java.awt.geom.AffineTransform)
	 */
	@Override
	public void setGlyphTransform(int glyphIndex, AffineTransform transform) {
		transforms[glyphIndex] = transform;
	}

	public void translateGlyphs(double x, double y) {
		for (int glyphIndex = 0; glyphIndex < numGlyphs; glyphIndex++) {
			AffineTransform transform = getGlyphTransform(glyphIndex);
		}
	}

	public GlyphVector getGlyphVector(){
		return this;
	}
}
