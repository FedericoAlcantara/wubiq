/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * Wraps a FontRenderContext object into a serializable one
 * @author Federico Alcantara
 *
 */
public class FontRenderContextWrapper extends FontRenderContext 
		implements Serializable {
	private static final long serialVersionUID = 1L;
	private int antiAliasingHint;
	private int fractionalMetricsHint;
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
			determineAntiAliasing(ctx);
			determineFractionalMetrics(ctx);
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
		Object returnValue = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
		if (antiAliasingHint == 1) {
			returnValue = RenderingHints.VALUE_ANTIALIAS_OFF;
		} else {
			returnValue = RenderingHints.VALUE_ANTIALIAS_ON;
		}
		return returnValue;
	}

	/**
	 * @return the fractionalMetricsHint
	 */
	@Override
	public Object getFractionalMetricsHint() {
		Object returnValue = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
		if (fractionalMetricsHint == 1) {
			returnValue = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
		} else {
			returnValue = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		}
		return returnValue;
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
	
	private void determineAntiAliasing(FontRenderContext ctx) {
		if (ctx.getAntiAliasingHint().equals(RenderingHints.VALUE_ANTIALIAS_DEFAULT)) {
			antiAliasingHint = 0;
		} else if (ctx.getAntiAliasingHint().equals(RenderingHints.VALUE_ANTIALIAS_OFF)) {
			antiAliasingHint = 1;
		} else if (ctx.getAntiAliasingHint().equals(RenderingHints.VALUE_ANTIALIAS_ON)) {
			antiAliasingHint = 2;
		}
	}
	
	private void determineFractionalMetrics(FontRenderContext ctx) {
		if (ctx.getFractionalMetricsHint().equals(RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)) {
			fractionalMetricsHint = 0;
		} else if (ctx.getFractionalMetricsHint().equals(RenderingHints.VALUE_FRACTIONALMETRICS_OFF)) {
			fractionalMetricsHint = 1;
		} else if (ctx.getFractionalMetricsHint().equals(RenderingHints.VALUE_FRACTIONALMETRICS_ON)) {
			fractionalMetricsHint = 2;
		}
	}
}
