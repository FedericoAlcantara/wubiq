/**
 * 
 */
package net.sf.wubiq.android;

/**
 * Defines the steps available for conversion to mobile printing.
 * These are server side steps (Java)
 * @author Federico Alcantara
 *
 */
public enum MobileServerConversionStep {
	PDF_TO_IMAGE, RESIZE, IMAGE_TO_ESCAPED, IMAGE_TO_BIT_LINE, IMAGE_TO_BITMAP, IMAGE_TO_HEX, IMAGE_TO_PCX;
}
