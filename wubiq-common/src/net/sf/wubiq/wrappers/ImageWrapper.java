/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * @author Federico Alcantara
 *
 */
public class ImageWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private byte[] imageData;
	
	public ImageWrapper(){
		
	}
	
	/**
	 * Preferred constructor.
	 * @param img Image to be represented in serialization.
	 */
	public ImageWrapper(Image img){
		super();
		serializeImage(img);
	}
	
	/**
	 * Serialize the image.
	 * @param img Image object to be serialized.
	 */
	public void serializeImage(Image img) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write((BufferedImage)img, "png", output);
			setImageData(output.toByteArray());
			output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a scaled image of the previously serialized image
	 * @param xScale new horizontal scale.
	 * @param yScale new vertical scale.
	 * @return Scaled image.
	 */
	public Image getImage(double xScale, double yScale) {
		BufferedImage returnValue = null;
		if (getImageData() != null) {
			try {
				returnValue = ImageIO.read(new ByteArrayInputStream(getImageData()));
				if (xScale != 1.0 || yScale != 1.0) {
					int width = (int)Math.rint(returnValue.getWidth() * xScale);
					int height = (int)Math.rint(returnValue.getHeight() * yScale);
					returnValue = (BufferedImage)returnValue.getScaledInstance(width, height, Image.SCALE_DEFAULT);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return returnValue;
	}
	
	/**
	 * Sets the image data for serialization.
	 * @param imageData
	 */
	protected void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	
	/**
	 * Returns the serialized imageData.
	 * @return
	 */
	protected byte[] getImageData() {
		return imageData;
	}
}
