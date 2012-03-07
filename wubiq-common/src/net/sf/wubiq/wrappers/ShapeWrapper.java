/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class ShapeWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private Rectangle rectangle;
	private GeneralPath generalPath;

	public ShapeWrapper() {
		rectangle = null;
		generalPath = null;
	}
	
	public ShapeWrapper(Shape shape) {
		this();
		if (shape instanceof Rectangle) {
			rectangle = (Rectangle)shape;
		} else if (shape instanceof GeneralPath){
			generalPath = (GeneralPath)shape;
		} else if (shape instanceof Rectangle2D) {
			rectangle = new Rectangle(shape.getBounds());
		}
	}
	
	public Shape getShape() {
		if (rectangle != null) {
			return rectangle;
		}
		return generalPath;
	}
}
