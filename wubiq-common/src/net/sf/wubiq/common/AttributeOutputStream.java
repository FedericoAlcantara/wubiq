/**
 * 
 */
package net.sf.wubiq.common;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.IntegerSyntax;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.print.attribute.standard.MediaPrintableArea;

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
		write(data.toString());
	}

	/** 
	 * Writes a collection of attributes to the stream.
	 * @param attributesCollection List of attributes to be written.
	 * @throws IOException
	 */
	public void writeAttributes(Collection<Attribute> attributesCollection) throws IOException {
		StringBuffer attributes = new StringBuffer("");
		for (Attribute attribute : attributesCollection) {
			if (attribute != null) {
				if (attributes.length() > 0) {
					attributes.append(ParameterKeys.ATTRIBUTES_SEPARATOR);
				}
				try {
					StringBuffer serialized = convertAttributeToString(attribute);
					attributes.append(serialized);
				} catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		}
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
		} else {
			if (attribute instanceof EnumSyntax) {
				data.append(ParameterKeys.ATTRIBUTE_TYPE_ENUM_SYNTAX).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
				writeEnumSyntax((EnumSyntax)attribute, data);
			} else {
				if (attribute instanceof IntegerSyntax) {
					data.append(ParameterKeys.ATTRIBUTE_TYPE_INTEGER_SYNTAX).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
					writeIntegerSyntax((IntegerSyntax)attribute, data);
				} else {
					if (attribute instanceof MediaPrintableArea) {
						data.append(ParameterKeys.ATTRIBUTE_TYPE_MEDIA_PRINTABLE_AREA).append(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
						writeMediaPrintableArea((MediaPrintableArea)attribute, data);
					} else {
						LOG.info("Attribute not converted:" + attribute);
						System.out.println(attribute);
					}
				}
			}
		}
		return data;
	}
	
	/**
	 * Serialize EnumSyntax attribute types.
	 * @param attribute Attribute to serialize.
	 * @param data Buffered data to output.
	 * @throws IOException
	 */
	private void writeEnumSyntax(EnumSyntax attribute, StringBuffer data) throws IOException {
		String value = attribute.toString().toUpperCase().replaceAll("-", "_").replaceAll(" ", "_");
		data.append(value);
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
	 * Serialize MediaPrintableArea attribute types.
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

}
