/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class Rectangle2DWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private Rectangle2D.Double rectangle2D;
	
	public Rectangle2DWrapper() {
		
	}
	
	public Rectangle2DWrapper(Rectangle2D rectangle2D) {
		this.rectangle2D = new Rectangle2D.Double(rectangle2D.getBounds().x, 
				rectangle2D.getBounds().y, 
				rectangle2D.getBounds().width,
				rectangle2D.getBounds().height);
	}
	
	public Rectangle2D getRectangle2D() {
		return rectangle2D;
	}
}

