/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.io.Serializable;

/**
 * Wraps a Composite object.
 * @author Federico Alcantara
 *
 */
public class CompositeWrapper implements Serializable {
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
}
