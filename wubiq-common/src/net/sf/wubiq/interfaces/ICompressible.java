/**
 * 
 */
package net.sf.wubiq.interfaces;

import net.sf.wubiq.wrappers.CompressedGraphicsPage;
import net.sf.wubiq.wrappers.GraphicParameter;
import net.sf.wubiq.wrappers.GraphicsChunkRecorder;

/**
 * Represents a element that can be compressed.
 * @author Federico Alcantara
 *
 */
public interface ICompressible {
	/**
	 * Perform any needed action for compressing the element.
	 * @param graphicsRecorder Associated graphics recorder.
	 */
	void compress(GraphicsChunkRecorder graphicsRecorder);
	
	/**
	 * Uncompress the value.
	 * @param compressedGraphicsPage Associated compressed graphics page.
	 * @return An array of graphics parameters.
	 */
	GraphicParameter[] uncompress(CompressedGraphicsPage compressedGraphicsPage);
}
