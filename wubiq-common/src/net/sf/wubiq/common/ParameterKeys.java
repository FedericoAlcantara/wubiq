/**
 * 
 */
package net.sf.wubiq.common;

/**
 * Contains keys used for forming parameters.
 * @author Federico Alcantara
 *
 */
public final class ParameterKeys {
	private ParameterKeys () {
	}
	public static final String UUID = "uuid";
	public static final String GROUPS = "grps";
	public static final String COMMAND = "command";
	public static final String PARAMETER_SEPARATOR = "=";
	public static final String COMPUTER_NAME = "computerName";
	public static final String PRINT_SERVICE_NAME = "printServiceName";
	public static final String PRINT_SERVICE_CATEGORIES = "printServiceCategories";
	public static final String PRINT_SERVICE_DOC_FLAVORS = "printServiceDocFlavors";
	public static final String CATEGORIES_SEPARATOR = ";";
	public static final String CATEGORIES_ATTRIBUTES_STARTER = "=";
	public static final String ATTRIBUTES_SEPARATOR = "/";
	public static final String ATTRIBUTE_CHANGE_SEPARATOR = "~";
	public static final String ATTRIBUTE_VALUE_SEPARATOR = ":";
	public static final String ATTRIBUTE_SET_SEPARATOR = ";";
	public static final String ATTRIBUTE_SET_MEMBER_SEPARATOR = ",";
	public static final String ATTRIBUTE_TYPE_SET_INTEGER_SYNTAX = "S";
	public static final String ATTRIBUTE_TYPE_ENUM_SYNTAX = "E";
	public static final String ATTRIBUTE_TYPE_INTEGER_SYNTAX = "I";
	public static final String ATTRIBUTE_TYPE_MEDIA_PRINTABLE_AREA = "A";
	public static final String ATTRIBUTE_TYPE_JOB_NAME = "J";
	public static final String ATTRIBUTE_TYPE_CUSTOM_MEDIA = "C";
	public static final String ATTRIBUTE_TYPE_MEDIA = "M";
	public static final String ATTRIBUTE_TYPE_MEDIA_TRAY = "T";
	public static final String ATTRIBUTE_TYPE_URI_SYNTAX = "U";
	public static final String PENDING_JOB_SIGNATURE = "pending_jobs";
	public static final String PRINT_JOB_ID = "printJob";
	public static final String CONNECTION_TEST_STRING = "WUBIQ_CONNECTED";
	public static final String PRINT_TEST_DIRECT_PAGEABLE = "TP";
	public static final String PRINT_TEST_STREAM_URL = "TPT";
	public static final String CLIENT_VERSION = "cv";
	public static final String CLIENT_SUPPORTS_COMPRESSION = "csp";
	
	/* Developers parameters not used or ignored in production */
	public static final String DEVELOPMENT_PROPERTY_NAME="zp01";
}
