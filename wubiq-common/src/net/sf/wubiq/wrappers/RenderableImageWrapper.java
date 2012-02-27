/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.image.BufferedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * @author Federico Alcantara
 *
 */
public class RenderableImageWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] imageData;
	
	public RenderableImageWrapper(){
		
	}
	
	public RenderableImageWrapper(RenderableImage img){
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write((BufferedImage)img, "png", output);
			imageData = output.toByteArray();
			output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * @return the imageData
	 */
	public byte[] getImageData() {
		return imageData;
	}

	/**
	 * @param imageData the imageData to set
	 */
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
}
