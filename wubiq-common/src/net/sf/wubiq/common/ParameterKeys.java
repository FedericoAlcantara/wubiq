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
	public static final String COMMAND = "command";
	public static final String PARAMETER_SEPARATOR = "=";
	public static final String COMPUTER_NAME = "computerName";
	public static final String PRINT_SERVICE_NAME = "printServiceName";
	public static final String PRINT_SERVICE_CATEGORIES = "printServiceCategories";
	public static final String CATEGORIES_SEPARATOR = ";";
	public static final String CATEGORIES_ATTRIBUTES_STARTER = "=";
	public static final String ATTRIBUTES_SEPARATOR = "/";
	public static final String ATTRIBUTE_VALUE_SEPARATOR = ":";
	public static final String ATTRIBUTE_SET_SEPARATOR = ";";
	public static final String ATTRIBUTE_SET_MEMBER_SEPARATOR = ",";
	public static final String ATTRIBUTE_TYPE_SET_INTEGER_SYNTAX = "S";
	public static final String ATTRIBUTE_TYPE_ENUM_SYNTAX = "E";
	public static final String ATTRIBUTE_TYPE_INTEGER_SYNTAX = "I";
	public static final String ATTRIBUTE_TYPE_MEDIA_PRINTABLE_AREA = "A";
	public static final String PENDING_JOB_SIGNATURE = "pending_jobs";
	public static final String PRINT_JOB_ID = "printJob";
}
