/**
 * 
 */
package net.sf.wubiq.common;

/**
 * Connection constants. YOU MUST be sure that
 * the constants values used here does not duplicate
 * any of the constants values of net.sf.wubiq.common.ParameterKeys.
 * @author Federico Alcantara
 *
 */
public final class DirectConnectKeys {
	private DirectConnectKeys(){};
	
	public static final String DIRECT_CONNECT_PARAMETER = "dp";
	public static final String DIRECT_CONNECT_DATA = "dd";
	public static final String DIRECT_CONNECT_DATA_UUID = "du";
	public static final String DIRECT_CONNECT_NULL = "/**NU**/";
	public static final String DIRECT_CONNECT_EXCEPTION = "/**EX**/";
	public static final String DIRECT_CONNECT_NOT_READY = "/**NR**/";
	public static final String DIRECT_CONNECT_ENABLED_PARAMETER = "dcen";
	
	public static final String DIRECT_CONNECT_FORCE_SERIALIZATION_PROPERTY = "wubiq.client.force.serialized.communication";
	

}
