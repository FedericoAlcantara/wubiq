/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.Font;
import java.io.Serializable;
import java.util.List;

import net.sf.wubiq.interfaces.ICompressible;

/**
 * @author Federico Alcantara
 *
 */
public class FontWrapper implements Serializable, ICompressible {
	private static final long serialVersionUID = 1L;

	private transient Font font;
	private int fontIndex; 
	
	public FontWrapper(Font font) {
		this.font = font;
	}
	
	@Override
	public void compress(GraphicsChunkRecorder graphicsRecorder) {
		fontIndex = graphicsRecorder.getFontIndex(font);
	}
	
	private Font getFont(List<Font> fontsList) {
		return fontsList.get(fontIndex);
	}
	
	/* *
	 * @see net.sf.wubiq.interfaces.ICompressible#uncompress(net.sf.wubiq.wrappers.CompressedGraphicsPage)
	 */
	@Override
	public GraphicParameter[] uncompress(CompressedGraphicsPage compressedGraphicsPage) {
		return new GraphicParameter[]{new GraphicParameter(Font.class, getFont(compressedGraphicsPage.getFontsList()))};
	}
	
}
