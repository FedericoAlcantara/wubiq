/**
 * 
 */
package net.sf.wubiq.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collection;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.URISyntax;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;

import net.sf.wubiq.print.attribute.CustomMediaSizeName;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.WebUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Serializes print attributes.
 * @author Federico Alcantara
 *
 */
public class AttributeOutputStream extends OutputStreamWriter {
	private static final Log LOG = LogFactory.getLog(AttributeOutputStream.class);
	public AttributeOutputStream(OutputStream outputStream) {
		super(outputStream);
	}
	
	/**
	 * Writes a single attribute to the stream.
	 * @param attribute Attribute to write.
	 * @throws IOException
	 */
	public void writeAttribute(Attribute attribute) throws IOException {
		StringBuffer data = convertAttributeToString(attribute); 
		super.write(data.toString());
		super.flush();
	}

	/** 
	 * Writes a collection of attributes to the stream.
	 * @param attributesCollection List of attributes to be written.
	 * @throws IOException
	 */
	public void writeAttributes(Collection<Attribute> attributesCollection) throws IOException {
		StringBuffer attributes = new StringBuffer("");
		if (attributesCollection != null) {
			for (Attribute attribute : attributesCollection) {
				if (attribute != null) {
					if (attributes.length() > 0) {
						attributes.append(ParameterKeys.ATTRIBUTES_SEPARATOR);
					}
					try {
						StringBuffer serialized = convertAttributeToString(attribute);
						attributes.append(serialized);
					} catch (Exception e) {
						if (PrintServiceUtils.OUTPUT_LOG) {
							LOG.error(e.getMessage());
						}
					}
				}
			}
		}
		super.write(attributes.toString());
		super.flush();
	}
	
	/**
	 * Serialize SetOfIntegerSyntax attribute type.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeSetOfIntegerSyntax(SetOfIntegerSyntax attribute, StringBuffer data) throws IOException {
		int[][] members = attribute.getMembers();
		StringBuffer memberData = new StringBuffer("");
		for (int i = 0; i < members.length; i++) {
			if (memberData.length() > 0) {
				memberData.append(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
			}
			StringBuffer tupple = new StringBuffer("");
			for (int j = 0; j < members[i].length; j++) {
				if (tupple.length() > 0) {
					tupple.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR);
				}
				tupple.append(members[i][j]);
			}
			memberData.append(tupple);
		}
		data.append(memberData);
	}
	
	/**
	 * Analyze the attribute and perform the appropriate serialization.
	 * @param attribute Attribute to be inspected.
	 * @return StringBuffer containing the attribute serialization.
	 * @throws IOException
	 */
	private StringBuffer convertAttributeToString(Attribute attribute) throws IOException {
		StringBuffer data = new StringBuffer(attribute.getClass().getName())
				.append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
		if (attribute instanceof SetOfIntegerSyntax) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_SET_INTEGER_SYNTAX).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeSetOfIntegerSyntax((SetOfIntegerSyntax)attribute, data);
		} else if (attribute instanceof CustomMediaSizeName) {
			writeCustomMediaSizeNameSyntax((CustomMediaSizeName)attribute, data);
		} else if (attribute instanceof MediaSizeName) {
			writeMediaSizeNameSyntax((MediaSizeName)attribute, data);
		} else if (attribute instanceof MediaTray) {
			writeMediaTraySyntax((MediaTray)attribute, data);
		} else if (attribute instanceof EnumSyntax) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_ENUM_SYNTAX).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeEnumSyntax((EnumSyntax)attribute, data);
		} else if (attribute instanceof IntegerSyntax) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_INTEGER_SYNTAX).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeIntegerSyntax((IntegerSyntax)attribute, data);
		} else if (attribute instanceof MediaPrintableArea) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_MEDIA_PRINTABLE_AREA).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeMediaPrintableArea((MediaPrintableArea)attribute, data);
		} else if (attribute instanceof JobName) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_JOB_NAME).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeJobName((JobName)attribute, data);
		} else if (attribute instanceof URISyntax) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_URI_SYNTAX)
				.append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			writeURISyntax((URISyntax)attribute, data);
		} else {
			if (PrintServiceUtils.OUTPUT_LOG) {
				LOG.info("Attribute not converted:" + attribute);
				System.out.println(attribute);
			}
		}
		return data;
	}
	
	/**
	 * Writes media information.
	 * @param mediaSize Size of the media.
	 * @param data Buffer containing the output.
	 * @throws IOException
	 */
	private void writeCustomMediaSizeNameSyntax(CustomMediaSizeName mediaSizeName, StringBuffer data) throws IOException {
		data.append(ParameterKeys.ATTRIBUTE_TYPE_CUSTOM_MEDIA).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR)
				.append(mediaSizeName.getX())
				.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
				.append(mediaSizeName.getY())
				.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
				.append(mediaSizeName.getUnits())
				;
	}

	/**
	 * Writes media information.
	 * @param mediaSize Size of the media.
	 * @param data Buffer containing the output.
	 * @throws IOException
	 */
	private void writeMediaSizeNameSyntax(MediaSizeName mediaSizeName, StringBuffer data) throws IOException {
		MediaSize mediaSize = MediaSize.getMediaSizeForName(mediaSizeName);
		if (mediaSize != null) {
			data.append(ParameterKeys.ATTRIBUTE_TYPE_MEDIA).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR)
					.append(mediaSize.getX(MediaSize.MM))
					.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
					.append(mediaSize.getY(MediaSize.MM))
					.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
					.append(mediaSizeName.toString().replaceAll(";", " "))
					;
		}
	}
	
	/**
	 * Attribute tray syntax writer.
	 * @param attribute Attribute of type media tray to store.
	 * @param data Data containing the attributes description.
	 * @throws IOException
	 */
	private void writeMediaTraySyntax(MediaTray attribute, StringBuffer data) throws IOException {
		data.append(ParameterKeys.ATTRIBUTE_TYPE_MEDIA_TRAY).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR)
				.append(attribute.getValue());
		
	}
	
	/**
	 * Serialize EnumSyntax attribute types.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeEnumSyntax(EnumSyntax attribute, StringBuffer data) throws IOException {
		writeStringData(attribute.toString().toUpperCase(), data);
	}
	
	/**
	 * Serialize EnumSyntax attribute types.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeIntegerSyntax(IntegerSyntax attribute, StringBuffer data) throws IOException {
		data.append(attribute.getValue());
	}
	
	/**
	 * Serializes MediaPrintableArea attribute types.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeMediaPrintableArea(MediaPrintableArea attribute, StringBuffer data) throws IOException {
		data.append(attribute.getX(MediaPrintableArea.MM))
			.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
			.append(attribute.getY(MediaPrintableArea.MM))
			.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
			.append(attribute.getWidth(MediaPrintableArea.MM))
			.append(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR)
			.append(attribute.getHeight(MediaPrintableArea.MM));
	}

	/**
	 * Serializes JobName attribute types.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeJobName(JobName attribute, StringBuffer data) throws IOException {
		data.append(attribute.getValue());
	}
	
	/**
	 * Serialize String data making it suitable for network transfer.
	 * @param input String data to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeStringData(String input, StringBuffer data) throws IOException {
		data.append(input.replaceAll("-", "_").replaceAll(" ", "_"));
	}
	
	/**
	 * Serializes a URI syntax attribute.
	 * @param attribute Attribute to be serialized
	 * @param data Data to be used.
	 * @throws IOException
	 */
	private void writeURISyntax(URISyntax attribute, StringBuffer data) throws IOException {
		//new URI(scheme, authority, path, query, fragment);
		URI uri = attribute.getURI();
		String value = convertValue(uri.getScheme())
				+ ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR
				+ convertValue(uri.getAuthority())
				+ ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR
				+ convertValue(uri.getPath())
				+ ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR
				+ convertValue(uri.getQuery())
				+ ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR
				+ convertValue(uri.getFragment())
				;
		data.append(value);
	}
	
	/**
	 * Converts the value to appropriate web communications.
	 * @param value Value to be converted.
	 * @return String representing the value.
	 */
	private String convertValue(String value) {
		String returnValue = "-";
		if (value != null) {
			returnValue = WebUtils.INSTANCE.encode(value.replaceAll(ParameterKeys.ATTRIBUTES_SEPARATOR, ParameterKeys.ATTRIBUTE_CHANGE_SEPARATOR));
		}
		return returnValue;
	}
}