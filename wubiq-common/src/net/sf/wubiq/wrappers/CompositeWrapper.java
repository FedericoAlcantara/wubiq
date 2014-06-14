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
}
