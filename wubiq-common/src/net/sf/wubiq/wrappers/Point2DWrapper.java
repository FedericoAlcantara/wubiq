/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class Point2DWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private Point2D.Double point2D;
	
	public Point2DWrapper(){
	}
	
	public Point2DWrapper(Point2D point2D) {
		if (point2D != null) {
			this.point2D = new Point2D.Double(point2D.getX(), point2D.getY());
		}
	}
	
	public Point2D getPoint2D() {
		return point2D;
	}
}
