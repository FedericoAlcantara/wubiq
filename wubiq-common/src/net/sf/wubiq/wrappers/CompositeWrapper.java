/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.io.Serializable;

/**
 * Wraps a Composite object.
 * @author Federico Alcantara
 *
 */
public class CompositeWrapper implements Serializable, Composite {
	private static final long serialVersionUID = 1L;
	private int rule;
	private float alpha;
	private boolean nullObject;
	
	public CompositeWrapper() {
		nullObject = true;
	}
	
	public CompositeWrapper(Composite composite) {
		this();
		if (composite instanceof AlphaComposite) {
			AlphaComposite alphaComposite = (AlphaComposite)composite;
			alpha = alphaComposite.getAlpha();
			rule = alphaComposite.getRule();
			nullObject = false;
		}
	}
	
	/**
	 * @return Composite
	 */
	public Composite getComposite() {
		if (!nullObject) {
			return AlphaComposite.getInstance(rule, alpha);
		} else {
			return null;
		}
	}

	@Override
	public CompositeContext createContext(ColorModel arg0, ColorModel arg1,
			RenderingHints arg2) {
		return null;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(alpha);
		result = prime * result + (nullObject ? 1231 : 1237);
		result = prime * result + rule;
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
		if (!(obj instanceof CompositeWrapper)) {
			return false;
		}
		CompositeWrapper other = (CompositeWrapper) obj;
		if (Float.floatToIntBits(alpha) != Float.floatToIntBits(other.alpha)) {
			return false;
		}
		if (nullObject != other.nullObject) {
			return false;
		}
		if (rule != other.rule) {
			return false;
		}
		return true;
	}
	
}
