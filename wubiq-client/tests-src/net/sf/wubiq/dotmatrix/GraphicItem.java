package net.sf.wubiq.dotmatrix;

import java.awt.Graphics2D;

/**
 * A shape or image a fixture draws, in user space (points).
 * @author Federico Alcantara
 *
 */
public interface GraphicItem {
	void draw(Graphics2D graphics);
}
