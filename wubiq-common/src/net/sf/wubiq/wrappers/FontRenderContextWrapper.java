/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * Wraps a FontRenderContext object into a serializable one.
 * @author Federico Alcantara
 *
 */
public class FontRenderContextWrapper extends FontRenderContext 
		implements Serializable {
	private static final long serialVersionUID = 1L;
	private RenderingHintWrapper antiAliasingHint;
	private RenderingHintWrapper fractionalMetricsHint;
	private AffineTransform transform;
	private int transformType;
	private int hashCode;
	private boolean antiAliased;
	private boolean transformed;
	private boolean usesFractionalMetrics;
	private boolean nullObject;
	
	public FontRenderContextWrapper () {
	}
	
	public FontRenderContextWrapper(FontRenderContext ctx) {
		if (ctx != null) {
			antiAliasingHint = new RenderingHintWrapper(RenderingHints.KEY_ANTIALIASING, ctx.getAntiAliasingHint());
			fractionalMetricsHint = new RenderingHintWrapper(RenderingHints.KEY_FRACTIONALMETRICS, ctx.getFractionalMetricsHint());
			transform = ctx.getTransform();
			transformType = ctx.getTransformType();
			hashCode = ctx.hashCode();
			antiAliased = ctx.isAntiAliased();
			transformed = ctx.isTransformed();
			usesFractionalMetrics = ctx.usesFractionalMetrics();
			nullObject = false;
		} else {
			nullObject = true;
		}
	}

	/**
	 * @return the antiAliasingHint
	 */
	@Override
	public Object getAntiAliasingHint() {
		return antiAliasingHint.getValue();
	}

	/**
	 * @return the fractionalMetricsHint
	 */
	@Override
	public Object getFractionalMetricsHint() {
		return fractionalMetricsHint.getValue();
	}

	/**
	 * @return the transform
	 */
	@Override
	public AffineTransform getTransform() {
		return transform;
	}

	/**
	 * @return the transformType
	 */
	public int getTransformType() {
		return transformType;
	}

	/**
	 * @return the hashCode
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * @return the antiAliased
	 */
	@Override
	public boolean isAntiAliased() {
		return antiAliased;
	}

	/**
	 * @return the transformed
	 */
	@Override
	public boolean isTransformed() {
		return transformed;
	}

	/**
	 * @return the usesFractionalMetrics
	 */
	@Override
	public boolean usesFractionalMetrics() {
		return usesFractionalMetrics;
	}

	public FontRenderContext getFontRenderContext(){
		if (nullObject) {
			return null;
		}
		return new FontRenderContext(transform, antiAliased, usesFractionalMetrics);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FontRenderContextWrapper [antiAliasingHint=" + antiAliasingHint
				+ ", fractionalMetricsHint=" + fractionalMetricsHint
				+ ", transform=" + transform + ", transformType="
				+ transformType + ", hashCode=" + hashCode + ", antiAliased="
				+ antiAliased + ", transformed=" + transformed
				+ ", usesFractionalMetrics=" + usesFractionalMetrics + "]";
	}
}
