/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Federico Alcantara
 *
 */
public class StrokeWrapper implements Stroke, Serializable {
	private static final long serialVersionUID = 1L;
	private float width;
	private int cap;
	private int join;
	private float miterLimit;
	private float[] dash;
	private float dashPhase;
	private boolean nullObject;
	
	public StrokeWrapper(){
		nullObject = true;
	}
	
	public StrokeWrapper(Stroke stroke) {
		this();
		if (stroke != null && stroke instanceof BasicStroke) {
			BasicStroke basicStroke = (BasicStroke)stroke;
			width = basicStroke.getLineWidth();
			cap = basicStroke.getEndCap();
			join = basicStroke.getLineJoin();
			miterLimit = basicStroke.getMiterLimit();
			dash = basicStroke.getDashArray();
			dashPhase = basicStroke.getDashPhase();
			nullObject = false;
		}
	}
	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}
	/**
	 * @return the cap
	 */
	public int getCap() {
		return cap;
	}
	/**
	 * @param cap the cap to set
	 */
	public void setCap(int cap) {
		this.cap = cap;
	}
	/**
	 * @return the join
	 */
	public int getJoin() {
		return join;
	}
	/**
	 * @param join the join to set
	 */
	public void setJoin(int join) {
		this.join = join;
	}
	/**
	 * @return the miterlimit
	 */
	public float getMiterLimit() {
		return miterLimit;
	}
	/**
	 * @param miterLimit the miterLimit to set
	 */
	public void setMiterlimit(float miterLimit) {
		this.miterLimit = miterLimit;
	}
	/**
	 * @return the dash
	 */
	public float[] getDash() {
		return dash;
	}
	/**
	 * @param dash the dash to set
	 */
	public void setDash(float[] dash) {
		this.dash = dash;
	}
	/**
	 * @return the dashPhase
	 */
	public float getDashPhase() {
		return dashPhase;
	}
	/**
	 * @param dashPhase the dashPhase to set
	 */
	public void setDashPhase(float dashPhase) {
		this.dashPhase = dashPhase;
	}
	
	public Stroke getStroke(double xScale, double yScale) {
		if (!nullObject) {
			return new BasicStroke((float)(width / xScale), cap, join);
		}
		return null;
	}

	public Stroke getStroke() {
		return getStroke(1, 1);
	}

	@Override
	public Shape createStrokedShape(Shape shape) {
		Stroke stroke = getStroke();
		if (stroke != null) {
			return stroke.createStrokedShape(shape);
		}
		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StrokeWrapper [width=" + width + ", cap=" + cap + ", join="
				+ join + ", miterLimit=" + miterLimit + ", dash="
				+ Arrays.toString(dash) + ", dashPhase=" + dashPhase + "]";
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cap;
		result = prime * result + Arrays.hashCode(dash);
		result = prime * result + Float.floatToIntBits(dashPhase);
		result = prime * result + join;
		result = prime * result + Float.floatToIntBits(miterLimit);
		result = prime * result + (nullObject ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(width);
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StrokeWrapper)) {
			return false;
		}
		StrokeWrapper other = (StrokeWrapper) obj;
		if (cap != other.cap) {
			return false;
		}
		if (!Arrays.equals(dash, other.dash)) {
			return false;
		}
		if (Float.floatToIntBits(dashPhase) != Float
				.floatToIntBits(other.dashPhase)) {
			return false;
		}
		if (join != other.join) {
			return false;
		}
		if (Float.floatToIntBits(miterLimit) != Float
				.floatToIntBits(other.miterLimit)) {
			return false;
		}
		if (nullObject != other.nullObject) {
			return false;
		}
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width)) {
			return false;
		}
		return true;
	}
}
