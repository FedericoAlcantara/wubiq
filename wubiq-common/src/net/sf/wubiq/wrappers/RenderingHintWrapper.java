/**
 * 
 */
package net.sf.wubiq.wrappers;

import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.io.Serializable;

/**
 * @author Federico Alcantara
 *
 */
public class RenderingHintWrapper implements Serializable {
	private static final long serialVersionUID = 1L;
	int keyValue;
	int valueValue;
	
	public RenderingHintWrapper(){
		keyValue = -1;
		valueValue = -1;
	}
	
	public RenderingHintWrapper(RenderingHints.Key key, Object value) {
		this();
		if (key.equals(RenderingHints.KEY_ALPHA_INTERPOLATION)) {
			setAlphaValue(value);
		} else if (key.equals(RenderingHints.KEY_ANTIALIASING)) {
			setAntiAliasValue(value);
		} else if (key.equals(RenderingHints.KEY_COLOR_RENDERING)) {
			setColorRenderingValue(value);
		} else if (key.equals(RenderingHints.KEY_DITHERING)) {
			setDitheringValue(value);
		} else if (key.equals(RenderingHints.KEY_FRACTIONALMETRICS)) {
			setFractionalMetricsValue(value);
		} else if (key.equals(RenderingHints.KEY_INTERPOLATION)) {
			setInterpolationValue(value);
		} else if (key.equals(RenderingHints.KEY_RENDERING)) {
			setRenderingValue(value);
		} else if (key.equals(RenderingHints.KEY_STROKE_CONTROL)) {
			setStrokeValue(value);
		} else if (key.equals(RenderingHints.KEY_TEXT_ANTIALIASING)) {
			setTextAntiAliasValue(value);
		} else if (key.equals(RenderingHints.KEY_TEXT_LCD_CONTRAST)) {
			setTextLcdContrastValue(value);
		}
	}
	
	/**
	 * Converts a key value to internal wrapper representation.
	 * @param key Key to convert.
	 * @return Integer representing the key.
	 */
	public static int keyRepresentation(RenderingHints.Key key) {
		if (key.equals(RenderingHints.KEY_ALPHA_INTERPOLATION)) {
			return 0;
		} else if (key.equals(RenderingHints.KEY_ANTIALIASING)) {
			return 1;
		} else if (key.equals(RenderingHints.KEY_COLOR_RENDERING)) {
			return 2;
		} else if (key.equals(RenderingHints.KEY_DITHERING)) {
			return 3;
		} else if (key.equals(RenderingHints.KEY_FRACTIONALMETRICS)) {
			return 4;
		} else if (key.equals(RenderingHints.KEY_INTERPOLATION)) {
			return 5;
		} else if (key.equals(RenderingHints.KEY_RENDERING)) {
			return 6;
		} else if (key.equals(RenderingHints.KEY_STROKE_CONTROL)) {
			return 7;
		} else if (key.equals(RenderingHints.KEY_TEXT_ANTIALIASING)) {
			return 8;
		} else {
			return 9;
		}
	}

	
	/**
	 * Transform a key representation into a RenderingHints.key.
	 * @param keyRepresentation Key representation.
	 * @return RenderingHints.key.
	 */
	public static Key getKey(int keyRepresentation) {
		if (keyRepresentation == 0) {
			return RenderingHints.KEY_ALPHA_INTERPOLATION;
		} else if (keyRepresentation == 1) {
			return RenderingHints.KEY_ANTIALIASING;
		} else if (keyRepresentation == 2) {
			return RenderingHints.KEY_COLOR_RENDERING;
		} else if (keyRepresentation == 3) {
			return RenderingHints.KEY_DITHERING;
		} else if (keyRepresentation == 4) {
			return RenderingHints.KEY_FRACTIONALMETRICS;
		} else if (keyRepresentation == 5) {
			return RenderingHints.KEY_INTERPOLATION;
		} else if (keyRepresentation == 6) {
			return RenderingHints.KEY_RENDERING;
		} else if (keyRepresentation == 7) {
			return RenderingHints.KEY_STROKE_CONTROL;
		} else if (keyRepresentation == 8) {
			return RenderingHints.KEY_TEXT_ANTIALIASING;
		} else {
			return RenderingHints.KEY_TEXT_LCD_CONTRAST;
		}
	}
	
	private void setAlphaValue(Object value) {
		keyValue = 0;
		valueValue = 0; // default
		if (value.equals(RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)) {
			valueValue = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setAntiAliasValue(Object value) {
		keyValue = 1;
		valueValue = 0; // default
		if (value.equals(RenderingHints.VALUE_ANTIALIAS_OFF)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setColorRenderingValue(Object value) {
		keyValue = 2;
		valueValue = 0; // default
		if (value.equals(RenderingHints.VALUE_COLOR_RENDER_QUALITY)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setDitheringValue(Object value) {
		keyValue = 3;
		valueValue = 0; // default
		if (value.equals(RenderingHints.VALUE_DITHER_DISABLE)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setFractionalMetricsValue(Object value) {
		keyValue = 4;
		valueValue = 0; // default
		if (value.equals(RenderingHints.VALUE_FRACTIONALMETRICS_OFF)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setInterpolationValue(Object value) {
		keyValue = 5;
		valueValue = 0; // BICUBIC
		if (value.equals(RenderingHints.VALUE_INTERPOLATION_BILINEAR)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setRenderingValue(Object value) {
		keyValue = 6;
		valueValue = 0; // DEFAULT
		if (value.equals(RenderingHints.VALUE_RENDER_QUALITY)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setStrokeValue(Object value) {
		keyValue = 7;
		valueValue = 0; // DEFAULT
		if (value.equals(RenderingHints.VALUE_STROKE_NORMALIZE)) {
			value = 1;
		} else {
			valueValue = 2;
		}
	}
	
	private void setTextAntiAliasValue(Object value) {
		keyValue = 8;
		valueValue = 0; // DEFAULT
		if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)) {
			value = 1;
		} else if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_ON)) {
			value = 2;
		} else if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)) {
			value = 3;
		} else if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR)) {
			value = 4;
		} else if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)) {
			value = 5;
		} else if (value.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR)) {
			value = 6;
		} else {
			valueValue = 7; // LCD_VRGB
		}
	}
	
	private void setTextLcdContrastValue(Object value) {
		keyValue = 9;
		valueValue = (Integer)value;
	}

	public RenderingHints.Key getKey() {
		RenderingHints.Key returnValue = RenderingHints.KEY_ALPHA_INTERPOLATION;
		if (keyValue == 1) {
			returnValue = RenderingHints.KEY_ANTIALIASING;
		} else if (keyValue == 2) {
			returnValue = RenderingHints.KEY_COLOR_RENDERING;
		} else if (keyValue == 3) {
			returnValue = RenderingHints.KEY_DITHERING;
		} else if (keyValue == 4) {
			returnValue = RenderingHints.KEY_FRACTIONALMETRICS;
		} else if (keyValue == 5) {
			returnValue = RenderingHints.KEY_INTERPOLATION;
		} else if (keyValue == 6) {
			returnValue = RenderingHints.KEY_RENDERING;
		} else if (keyValue == 7) {
			returnValue = RenderingHints.KEY_STROKE_CONTROL;
		} else if (keyValue == 8) {
			returnValue = RenderingHints.KEY_TEXT_ANTIALIASING;
		} else if (keyValue == 9) {
			returnValue = RenderingHints.KEY_TEXT_LCD_CONTRAST;
		}
		return returnValue;
	}
	
	/**
	 * Gets the representation of the key value.
	 * @return
	 */
	public int getKeyRepresentation() {
		return keyValue;
	}
	
	public Object getValue() {
		Object returnValue = null;
		if (keyValue == 0) {
			returnValue = getAlphaValue();
		} else if (keyValue == 1) {
			returnValue = getAntiAliasValue();
		} else if (keyValue == 2) {
			returnValue = getColorRenderingValue();
		} else if (keyValue == 3) {
			returnValue = getDitheringValue();
		} else if (keyValue == 4) {
			returnValue = getFractionalMetricsValue();
		} else if (keyValue == 5) {
			returnValue = getInterpolationValue();
		} else if (keyValue == 6) {
			returnValue = getRenderingValue();
		} else if (keyValue == 7) {
			returnValue = getStrokeValue();
		} else if (keyValue == 8) {
			returnValue = getTextAntiAliasValue();
		} else if (keyValue == 9) {
			returnValue = getTextLcdContrastValue();
		}
		return returnValue;
	}
	
	public int getValueRepresentation() {
		return keyValue;
	}
	
	private Object getAlphaValue() {
		Object value = RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT;
		if (valueValue == 1) { 
			value = RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY;
		} else {
			value = RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
		}
		return value;
	}
	
	private Object getAntiAliasValue() {
		Object value = RenderingHints.VALUE_ANTIALIAS_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_ANTIALIAS_OFF;
		} else {
			value = RenderingHints.VALUE_ANTIALIAS_ON;
		}
		 return value;
	}
	
	private Object getColorRenderingValue() {
		Object value = RenderingHints.VALUE_COLOR_RENDER_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_COLOR_RENDER_QUALITY;
		} else {
			value = RenderingHints.VALUE_COLOR_RENDER_SPEED;
		}
		 return value;
	}
	
	private Object getDitheringValue() {
		Object value = RenderingHints.VALUE_DITHER_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_DITHER_DISABLE;
		} else {
			value = RenderingHints.VALUE_DITHER_ENABLE;
		}
		 return value;
	}
	
	private Object getFractionalMetricsValue() {
		Object value = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
		} else {
			value = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
		}
		 return value;
	}
	
	private Object getInterpolationValue() {
		Object value = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		} else {
			value = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		}
		 return value;
	}
	
	private Object getRenderingValue() {
		Object value = RenderingHints.VALUE_RENDER_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_RENDER_QUALITY;
		} else {
			value = RenderingHints.VALUE_RENDER_SPEED;
		}
		 return value;
	}
	
	private Object getStrokeValue() {
		Object value = RenderingHints.VALUE_STROKE_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_STROKE_NORMALIZE;
		} else {
			value = RenderingHints.VALUE_STROKE_PURE;
		}
		 return value;
	}
	
	private Object getTextAntiAliasValue() {
		Object value = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
		if (valueValue == 1) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
		} else if (valueValue == 2) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
		} else if (valueValue == 3) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
		} else if (valueValue == 4) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
		} else if (valueValue == 5) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
		} else if (valueValue == 6) {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
		} else {
			value = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB; // LCD_VRGB
		}
		 return value;
	}
	
	private Object getTextLcdContrastValue() {
		return new Integer(valueValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RenderingHintWrapper [keyValue=" + keyValue + ", valueValue="
				+ valueValue + "]";
	}

}
