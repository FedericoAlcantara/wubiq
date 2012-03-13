/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.RenderingHints;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Wraps the class RenderingHints
 * @author Federico Alcantara
 *
 */
public class RenderingHintsWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<RenderingHintWrapper> hints;
	
	public RenderingHintsWrapper(){
		hints = new ArrayList<RenderingHintWrapper>();
	}
	
	public RenderingHintsWrapper(Map<?, ?> renderingHints){
		this();
		for (Entry<?, ?> renderingHint : renderingHints.entrySet()) {
			RenderingHints.Key key = (RenderingHints.Key) renderingHint.getKey();
			RenderingHintWrapper hintWrapper = new RenderingHintWrapper(key, renderingHint.getValue());
			hints.add(hintWrapper);
		}
	}
	
	public RenderingHintsWrapper(RenderingHints renderingHints) {
		this();
		for (Entry<Object, Object> renderingHint : renderingHints.entrySet()) {
			RenderingHints.Key key = (RenderingHints.Key) renderingHint.getKey();
			RenderingHintWrapper hintWrapper = new RenderingHintWrapper(key, renderingHint.getValue());
			hints.add(hintWrapper);
		}
	}
	
	public RenderingHints getRenderingHints(){
		Map<RenderingHints.Key, Object> returnValue = new HashMap<RenderingHints.Key, Object>();
		for (RenderingHintWrapper hint : hints) {
			returnValue.put(hint.getKey(), hint.getValue());
		}
		return new RenderingHints(returnValue);
	}
}
