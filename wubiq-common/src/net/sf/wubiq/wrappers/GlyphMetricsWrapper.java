/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.font.GlyphMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class GlyphMetricsWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private float advance;
	private float advanceX;
	private float advanceY;
	private Rectangle2DWrapper bounds2D;
	private float LSB;
	private float RSB;
	private int type;
	private boolean combining;
	private boolean component;
	private boolean ligature;
	private boolean standard;
	private boolean whitespace;
	private boolean nullObject;
	
	public GlyphMetricsWrapper(){
		
	}
	
	public GlyphMetricsWrapper(GlyphMetrics glyphMetrics) {
		if (glyphMetrics != null) {
			this.advance = glyphMetrics.getAdvance();
			this.advanceX = glyphMetrics.getAdvanceX();
			this.advanceY = glyphMetrics.getAdvanceY();
			this.bounds2D = new Rectangle2DWrapper(glyphMetrics.getBounds2D());
			this.LSB = glyphMetrics.getLSB();
			this.RSB = glyphMetrics.getRSB();
			this.type = glyphMetrics.getType();
			this.combining = glyphMetrics.isCombining();
			this.component = glyphMetrics.isComponent();
			this.ligature = glyphMetrics.isLigature();
			this.standard = glyphMetrics.isStandard();
			this.whitespace = glyphMetrics.isWhitespace();
			nullObject = false;
		} else {
			nullObject = true;
		}
	}
	
	/**
	 * @return the advance
	 */
	public float getAdvance() {
		return advance;
	}
	/**
	 * @return the advanceX
	 */
	public float getAdvanceX() {
		return advanceX;
	}
	/**
	 * @return the advanceY
	 */
	public float getAdvanceY() {
		return advanceY;
	}
	/**
	 * @return the getBounds2D
	 */
	public Rectangle2D getGetBounds2D() {
		return bounds2D.getRectangle2D();
	}
	/**
	 * @return the lSB
	 */
	public float getLSB() {
		return LSB;
	}
	/**
	 * @return the rSB
	 */
	public float getRSB() {
		return RSB;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the combining
	 */
	public boolean isCombining() {
		return combining;
	}
	/**
	 * @return the component
	 */
	public boolean isComponent() {
		return component;
	}
	/**
	 * @return the ligature
	 */
	public boolean isLigature() {
		return ligature;
	}
	/**
	 * @return the standard
	 */
	public boolean isStandard() {
		return standard;
	}
	/**
	 * @return the whitespace
	 */
	public boolean isWhitespace() {
		return whitespace;
	}
	
	public GlyphMetrics getGlyphMetrics() {
		if (nullObject) {
			return null;
		}
		return new GlyphMetrics(advance, bounds2D.getRectangle2D(), (byte) type);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GlyphMetricsWrapper [advance=" + advance + ", advanceX="
				+ advanceX + ", advanceY=" + advanceY + ", bounds2D="
				+ bounds2D + ", LSB=" + LSB + ", RSB=" + RSB + ", type=" + type
				+ ", combining=" + combining + ", component=" + component
				+ ", ligature=" + ligature + ", standard=" + standard
				+ ", whitespace=" + whitespace + "]";
	}
}
