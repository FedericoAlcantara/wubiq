/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.image.renderable.RenderableImage;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class RenderableImageWrapper extends RenderedImageWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	public RenderableImageWrapper(){
		super();
	}
	
	public RenderableImageWrapper(RenderableImage img){
		super(img.createDefaultRendering());
	}
	
}
