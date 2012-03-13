/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.font.GlyphJustificationInfo;
import java.io.Serializable;

/**
 * Wraps GlyphJustification info.
 * @author Federico Alcantara
 *
 */
public class GlyphJustificationInfoWrapper
		implements Serializable {
	private static final long serialVersionUID = 1L;
	public float weight;
	public boolean growAbsorb;
	public int growPriority;
	public float growLeftLimit;
	public float growRightLimit;
	public boolean shrinkAbsorb;
	public int shrinkPriority;
	public float shrinkLeftLimit;
	public float shrinkRightLimit;
	public boolean nullObject;
	
	public GlyphJustificationInfoWrapper(){
	}
	
	public GlyphJustificationInfoWrapper(GlyphJustificationInfo glyphJustification) {
		if (glyphJustification != null) {
			this.weight = glyphJustification.weight;
			this.growAbsorb = glyphJustification.growAbsorb;
			this.growPriority = glyphJustification.growPriority;
			this.growLeftLimit = glyphJustification.growLeftLimit;
			this.growRightLimit = glyphJustification.growRightLimit;
			this.shrinkAbsorb = glyphJustification.shrinkAbsorb;
			this.shrinkPriority = glyphJustification.shrinkPriority;
			this.shrinkLeftLimit = glyphJustification.shrinkLeftLimit;
			this.shrinkRightLimit = glyphJustification.shrinkRightLimit;
			nullObject = false;
		} else {
			nullObject = true;
		}
	}
	
	public GlyphJustificationInfo getInfo() {
		if (nullObject) {
			return null;
		}
		return new GlyphJustificationInfo(weight, 
				growAbsorb, growPriority,
				growLeftLimit, growRightLimit,
				shrinkAbsorb, shrinkPriority,
				shrinkLeftLimit, shrinkRightLimit);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GlyphJustificationInfoWrapper [weight=" + weight
				+ ", growAbsorb=" + growAbsorb + ", growPriority="
				+ growPriority + ", growLeftLimit=" + growLeftLimit
				+ ", growRightLimit=" + growRightLimit + ", shrinkAbsorb="
				+ shrinkAbsorb + ", shrinkPriority=" + shrinkPriority
				+ ", shrinkLeftLimit=" + shrinkLeftLimit
				+ ", shrinkRightLimit=" + shrinkRightLimit + "]";
	}
}
