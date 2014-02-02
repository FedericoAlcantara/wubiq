/**
 * 
 */
package net.sf.wubiq.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.print.attribute.Attribute;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;

import net.sf.wubiq.print.attribute.GenericMediaSizeName;
import net.sf.wubiq.print.attribute.GenericMediaTray;
import net.sf.wubiq.utils.Is;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reads from a stream deserializing it into object.
 * @author Federico Alcantara
 *
 */
public class AttributeInputStream extends InputStreamReader {
	private static final Log LOG = LogFactory.getLog(AttributeInputStream.class);
	public AttributeInputStream(InputStream inputStream) {
		super(inputStream);
	}
	
	/**
	 * Deserializes a single attribute from the stream.
	 * @return Object or null.
	 * @throws IOException
	 */
	public Attribute readAttribute() throws IOException {
		Attribute returnValue = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		while (ready()) {
			stream.write(read());
		}
		stream.close();
		String attributeData = stream.toString();
		try {
			returnValue = convertToAttribute(attributeData);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}

	/**
	 * Deserialize a collection of attributes from the stream.
	 * @return A collection of attribute. Never null.
	 * @throws IOException
	 */
	public Collection<Attribute> readAttributes() throws IOException {
		Collection<Attribute>returnValue = new ArrayList<Attribute>();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		while (ready()) {
			stream.write(read());
		}
		stream.close();
		String attributeDatas = stream.toString();
		if (attributeDatas != null) {
			for (String attributeData : attributeDatas.split(ParameterKeys.ATTRIBUTES_SEPARATOR)) {
				Attribute attribute = null;
				try {
					attribute = convertToAttribute(attributeData);
					returnValue.add(attribute);
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			
			}
		}
		return returnValue;
	}
	
	/**
	 * Deserialize a single attribute from a String.
	 * @return Object or null.
	 * @throws IOException
	 */

	private Attribute convertToAttribute(String attributeData) throws IOException {
		Attribute returnValue = null;
		String deserialized = attributeData;
		if (!Is.emptyString(deserialized)) {
			String[] objectDetails = deserialized.split(ParameterKeys.ATTRIBUTE_VALUE_SEPARATOR);
			if (objectDetails.length > 2) {
				if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_SET_INTEGER_SYNTAX)) {
					returnValue = readSetOfIntegerSyntax(objectDetails[0], objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_MEDIA)) {
					returnValue = readMediaSyntax(objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_MEDIA_TRAY)) {
					returnValue = readMediaTraySyntax(objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_ENUM_SYNTAX)) {
					returnValue = readEnumSyntax(objectDetails[0], objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_INTEGER_SYNTAX)) {
					returnValue = readIntegerSyntax(objectDetails[0], objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_MEDIA_PRINTABLE_AREA)) {
					returnValue = readMediaPrintableArea(objectDetails[0], objectDetails[2]);
				} else if (objectDetails[1].equals(ParameterKeys.ATTRIBUTE_TYPE_JOB_NAME)) {
					returnValue = readJobName(objectDetails[0], objectDetails[2]);
				} else {
					throw new IOException("Attribute not recognized:" + deserialized);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Deserialize attribute of type SetOfIntegerSyntax.
	 * @param className Class name of the attribute.
	 * @param values Values in the stream to deserialize.
	 * @return Object or null.
	 * @throws IOException If occurs any instantiation exception.
	 */
	@SuppressWarnings({ "rawtypes" })
	private Attribute readSetOfIntegerSyntax(String className, String values) throws IOException {
		Attribute returnValue = null;
		Constructor constructor;
		try {
			String[] memberValues = values.split(ParameterKeys.ATTRIBUTE_SET_SEPARATOR);
			if (memberValues.length > 0) {
				String[] intValues = memberValues[0].split(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR);
				if (intValues.length > 1) {
					constructor = (Constructor) Class.forName(className).getConstructor(new Class[]{int.class, int.class});
					returnValue = (Attribute) constructor.newInstance(new Object[]{Integer.parseInt(intValues[0]), Integer.parseInt(intValues[1])});
				} else {
					constructor = (Constructor) Class.forName(className).getConstructor(new Class[]{int.class});
					returnValue = (Attribute) constructor.newInstance(new Object[]{Integer.parseInt(intValues[0])});
				}
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				LOG.error(e.getCause().getMessage(), e.getCause());
				throw new IOException(e.getCause());
			} else {
				LOG.error(e.getMessage(), e);
				throw new IOException(e);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}
		return returnValue;
	}
	
	/**
	 * Reads and convert a attribute with enum syntax into a standard attribute.
	 * @param className Name of the class.
	 * @param values Current enum value.
	 * @return Converted attribute if no platform compatible attribute is found.
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private Attribute readEnumSyntax(String className, String values) throws IOException {
		Attribute returnValue = null;
		try {
			Class clazz = Class.forName(className);
			returnValue = findEnumSyntax(clazz, values);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return returnValue;
	}
	
	/**
	 * Reads and convert a attribute with enum syntax into a standard attribute.
	 * @param values Dimension value.
	 * @return Attribute found or null if no suitable attribute is found.
	 * @throws IOException
	 */
	private Attribute readMediaSyntax(String values) throws IOException {
		Attribute returnValue = null;
		if (!Is.emptyString(values)) {
			String[] xyName = values.split(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR);
			if (xyName.length >= 3) {
				try {
					float x = Float.parseFloat(xyName[0]);
					float y = Float.parseFloat(xyName[1]);
					returnValue = MediaSize.findMedia(x, y, MediaSize.MM);
					if (returnValue == null) {
						new MediaSize(x, y, MediaSize.MM, new GenericMediaSizeName(xyName[2]));
						returnValue = MediaSize.findMedia(x, y, MediaSize.MM);
					}
				} catch (NumberFormatException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Converts a value to a tray representation.
	 * @param values Value to be converted.
	 * @return Tray representation or null if no suitable found.
	 * @throws IOException
	 */
	private Attribute readMediaTraySyntax(String values) throws IOException {
		GenericMediaTray mediaTray = new GenericMediaTray(0);
		Attribute returnValue = null;
		try {
			int index = Integer.parseInt(values);
			if (index < mediaTray.getEnumValueTable().length) {
				returnValue = (Attribute) mediaTray.getEnumValueTable()[index];
			}
		} catch (NumberFormatException e) {
			LOG.error(e.getMessage());
		}
		return returnValue;
	}
	
	
	/**
	 * Reads and convert a attribute with enum syntax into a standard attribute.
	 * @param clazz Name Class to be examined.
	 * @param values Enum value.
	 * @return Attribute found or null if no suitable attribute is found.
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private Attribute findEnumSyntax(Class clazz, String values) throws IOException {
		Attribute returnValue = null;
		if (!Is.emptyString(values)) {
			try {
				Field foundField = null;
				for (Field field : clazz.getDeclaredFields()) {
					if (field.getName().equals(values)) {
						foundField = field;
						break;
					}
				}
				if (foundField != null) {
						returnValue = (Attribute) foundField.get(null);
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
				returnValue = null;
			}
		}
		return returnValue;
	}

	
	/**
	 * Deserialize attribute of type IntegerSyntax.
	 * @param className Class name of the attribute.
	 * @param value Values in the stream to deserialize.
	 * @return Object or null.
	 * @throws IOException If occurs any instantiation exception.
	 */
	@SuppressWarnings({ "rawtypes" })
	private Attribute readIntegerSyntax(String className, String value) throws IOException {
		Attribute returnValue = null;
		Constructor constructor;
		try {
			if (!Is.emptyString(value)) {
				constructor = (Constructor) Class.forName(className).getConstructor(new Class[]{int.class});
				returnValue = (Attribute) constructor.newInstance(new Object[]{Integer.parseInt(value)});
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				LOG.error(e.getCause().getMessage(), e.getCause());
				throw new IOException(e.getCause());
			} else {
				LOG.error(e.getMessage(), e);
				throw new IOException(e);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}
		return returnValue;
	}
	
	/**
	 * Deserialize attribute of type MediaPrintableArea.
	 * @param className Class name of the attribute.
	 * @param values Values in the stream to deserialize.
	 * @return Object or null.
	 * @throws IOException If occurs any instantiation exception.
	 */
	@SuppressWarnings({ "rawtypes" })
	private Attribute readMediaPrintableArea(String className, String values) throws IOException {
		Attribute returnValue = null;
		Constructor constructor;
		try {
			if (!Is.emptyString(values)) {
				String[] floatValues = values.split(ParameterKeys.ATTRIBUTE_SET_MEMBER_SEPARATOR);
				if (floatValues.length >= 4) {
					constructor = (Constructor) Class.forName(className).getConstructor(new Class[]{float.class, float.class, float.class, float.class, int.class});
					returnValue = (Attribute) constructor.newInstance(new Object[]{
							Float.parseFloat(floatValues[0]),
							Float.parseFloat(floatValues[1]),
							Float.parseFloat(floatValues[2]),
							Float.parseFloat(floatValues[3]),
							MediaPrintableArea.MM});
				}
			}
		} catch (InvocationTargetException e) {
			if (e.getCause() != null) {
				LOG.error(e.getCause().getMessage(), e.getCause());
				throw new IOException(e.getCause());
			} else {
				LOG.error(e.getMessage(), e);
				throw new IOException(e);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IOException(e);
		}
		return returnValue;
	}
	
	private Attribute readJobName(String className, String values) throws IOException {
		return new JobName(values, Locale.getDefault());
	}
}
