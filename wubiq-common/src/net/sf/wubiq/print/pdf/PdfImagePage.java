/**
 * 
 */
package net.sf.wubiq.print.pdf;

import java.io.File;

/**
 * @author Federico Alcantara
 *
 */
public class PdfImagePage {
	private int page;
	private File imageFile;
	private float height;
	private float width;
	
	public PdfImagePage() {
	}
	
	public PdfImagePage(int page, File imageFile) {
		this();
		this.page = page;
		this.imageFile = imageFile;
	}
	
	public PdfImagePage(int page, File imageFile, float height, float width) {
		this(page, imageFile);
		this.height = height;
		this.width = width;
	}
	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}
	/**
	 * @return the imageFile
	 */
	public File getImageFile() {
		return imageFile;
	}
	/**
	 * @param imageFile the imageFile to set
	 */
	public void setImageFile(File imageFile) {
		this.imageFile = imageFile;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + page;
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PdfImagePage other = (PdfImagePage) obj;
		if (page != other.page)
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PdfImagePage [page=" + page + "]";
	}

	/**
	 * @return the height
	 */
	public float getPageHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return the width
	 */
	public float getPageWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}
	
}
