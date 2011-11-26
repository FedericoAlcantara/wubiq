/**
 * 
 */
package net.sf.wubiq.android;


/**
 * Defines the conversion politics.
 * @author Federico Alcantara
 *
 */
public interface IMobileConversion<T, V> {
	T conversion (MobileDeviceInfo deviceInfo, V object);
}
