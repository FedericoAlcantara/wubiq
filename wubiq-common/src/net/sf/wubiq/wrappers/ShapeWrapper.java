/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class ShapeWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private GeneralPath generalPath;

	public ShapeWrapper() {
		generalPath = null;
	}
	
	public ShapeWrapper(Shape shape) {
		this();
		if (shape != null) {
			generalPath = new GeneralPath(shape);
		}
	}
	
	public Shape getShape() {
		return generalPath;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (generalPath != null) {
			return "ShapeWrapper [generalPath=" + ((Shape)generalPath).getBounds() + "]";
		} else {
			return "ShapeWrapper [generalPath=null]";
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((generalPath == null) ? 0 : generalPath.hashCode());
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
		if (!(obj instanceof ShapeWrapper)) {
			return false;
		}
		ShapeWrapper other = (ShapeWrapper) obj;
		return this.toString().equals(other.toString());
	}
	
}
