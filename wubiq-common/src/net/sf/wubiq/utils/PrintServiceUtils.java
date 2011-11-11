package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.NumberUp;
import javax.print.attribute.standard.PageRanges;

import net.sf.wubiq.common.AttributeInputStream;
import net.sf.wubiq.common.AttributeOutputStream;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.print.services.RemotePrintService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for querying print service information.
 * @author Federico Alcantara
 *
 */
public class PrintServiceUtils {
	private static final Log LOG = LogFactory.getLog(PrintServiceUtils.class);
	public static boolean OUTPUT_LOG = true;
	public static void main(String[] args) {
		getPrintServices();
	}
	
	/**
	 * Tries to refresh print services.
	 */
	public static void refreshServices(){
		Method method;
		try {
			method = PrintServiceLookup.class.getDeclaredMethod("getAllLookupServices", new Class[]{});
			method.setAccessible(true);
			List lookupPrintServices = (List) method.invoke(null, new Object[]{});
			for (Object object : lookupPrintServices) {
				Method refreshServices;
				try {
					refreshServices = object.getClass().getDeclaredMethod("refreshServices", new Class[]{});
					refreshServices.invoke(object, new Object[]{});
				} catch (Exception e) {
					LOG.debug(e.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}

	}
	
	/**
	 * Returns an array of printers supporting PDF flavor.
	 * @return Array of print services. If no services is found the return value is JVM implementation dependent.
	 */
	public static PrintService[] getPrintServices() {
		refreshServices();
		return PrintServiceLookup.lookupPrintServices(null, null);
	}
	
	/**
	 * Based on its name find corresponding printer.
	 * @param name Name of the printer.
	 * @return A PrinterService or null.
	 */
	public static PrintService findPrinter(String name) {
		PrintService printer = null;
		if (!Is.emptyString(name)) {
			for (PrintService printService : getPrintServices()) {
				if (printService.getName().equals(name)) {
					printer = printService;
					break;
				}
			}
		}
		return printer;
	}
	
	/**
	 * Based on its name find corresponding printer. If not found
	 * returns the default printservice.
	 * @param name Name of the printer.
	 * @return A PrinterService or null.
	 */
	public static PrintService findPrinterOrDefault(String name) {
		PrintService printer = findPrinter(name);
		if (printer == null) {
			printer = PrintServiceLookup.lookupDefaultPrintService();
		}
		return printer;
	}
	
	/**
	 * Return an instance of Copies attribute. If copies are not supported a null value is returned.
	 * @param printService Print service to be asked.
	 * @param copiesCount copies count.
	 * @return a Copies instance or null.
	 */
	public static Copies findCopies(PrintService printService, Integer copiesCount) {
		if (printService != null && copiesCount != null &&
				printService.isAttributeCategorySupported(Copies.class)) {
			return new Copies(copiesCount);
		} else {
			return null;
		}
	}

	/**
	 * Return an instance of NumberUp attribute. If numberUp are not supported a null value is returned.
	 * @param printService Print service to be asked.
	 * @param numberUpCount numberUp count.
	 * @return a NumberUp instance or null.
	 */
	public static NumberUp findNumberUp(PrintService printService, Integer numberUpCount) {
		if (printService != null && numberUpCount != null &&
				printService.isAttributeCategorySupported(NumberUp.class)) {
			return new NumberUp(numberUpCount);
		} else {
			return null;
		}
	}

	/**
	 * Return an instance of PageRanges attribute.
	 * @param printService Print service to be asked.
	 * @param pageRanges Page ranges string representation
	 * @return PageRanges instance or null if not supported or blank
	 */
	public static PageRanges findPageRanges(PrintService printService, String pageRanges) {
		if (printService != null && !Is.emptyString(pageRanges)) {
			return new PageRanges(pageRanges);
		} else {
			return null;
		}
	}
	/**
	 * Given the attribute name returns its equivalent Attribute instance.
	 * @param printService Print service to be sought.
	 * @param attributeName Attribute name.
	 * @return Attribute name or null if not found or not supported for the print service.
	 */
	public static Attribute findAttribute(PrintService printService, Class<? extends Attribute> category, String attributeName) {
		Attribute returnValue = null;
		if (printService != null && !Is.emptyString(attributeName)) {
			Collection<Attribute> attributes = getCategoryAttributes(printService, category);
			for (Attribute attribute : attributes) {
				if (attribute.toString().equals(attributeName)) {
					returnValue = attribute;
					break;
				}
			}
		} 
		return returnValue;
	}
		
		/**
		 * Given the attribute name returns its equivalent Attribute instance for any service.
		 * @param attributeName Attribute name.
		 * @return Attribute name or null if not found or not supported for the print service.
		 */
		public static Attribute findAttribute(Class<? extends Attribute> category, String attributeName) {
			Attribute returnValue = null;
			for (PrintService printService : PrintServiceUtils.getPrintServices()) {
				returnValue = findAttribute(printService, category, attributeName);
				if (returnValue != null) {
					break;
				}
			}
			return returnValue;
		}
		
	
	/**
	 * Given a PrintService (printer) returns its supported categories.
	 * @param printService Printer to be sought.
	 * @return a Collection of supported categories, could be empty, never null.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Collection<Class<? extends Attribute>> getCategories(PrintService printService) {
		Collection<Class<? extends Attribute>> returnValue = new ArrayList<Class<? extends Attribute>>();
		for (Class category : printService.getSupportedAttributeCategories()) {
			returnValue.add(category);
		}
		return returnValue;
	}
	
	/**
	 * Determines if the category is supported by the printer.
	 * @param printService Print service, if null the category is assumed to be unsupported.
	 * @param category category to be tested.
	 * @return True if the category is supported.
	 */
	public static boolean isAttributeCategorySupported(PrintService printService, Class<? extends Attribute> category ) {
		if (printService != null) {
			if (category.equals(Chromaticity.class)) {
				return printService.getAttribute(ColorSupported.class).equals(ColorSupported.SUPPORTED);
			} else if (category.equals(MediaSizeName.class) || category.equals(MediaTray.class)) {
				return printService.isAttributeCategorySupported(Media.class);
			}
			return printService.isAttributeCategorySupported(category);
		}
		return false;
	}

	/**
	 * Finds the supported attributes for pdfFlavor for the combination printService, category.
	 * @param printService printService to be sought.
	 * @param category category as filter.
	 * @return a Collection of PresentationDirection attributes. Can be empty, never null.
	 */
	public static Collection<Attribute> getCategoryAttributes(PrintService printService, Class<? extends Attribute> category) {
		Collection<Attribute> returnValue = new ArrayList<Attribute>();
		if (printService != null) {
			if (category.equals(Chromaticity.class)) {
				returnValue.add(Chromaticity.MONOCHROME);
				returnValue.add(Chromaticity.COLOR);
			} else if (category.equals(MediaTray.class) || category.equals(MediaSizeName.class)) {
				Attribute[] attributes = (Attribute[]) printService.getSupportedAttributeValues(Media.class, null, null);
				if (attributes != null) {
					for (Attribute attribute : attributes) {
						if ((attribute instanceof MediaTray && category.equals(MediaTray.class)) ||
								attribute instanceof MediaSizeName && category.equals(MediaSizeName.class)) {
							if (!attribute.toString().equalsIgnoreCase("Custom")) {
								returnValue.add(attribute);
							}
						}
					}
				}				
			} else  {
				Object attributeObject = printService.getSupportedAttributeValues(category, null, null);
				if (attributeObject instanceof Attribute[]) {
					Attribute[] attributes = (Attribute[]) attributeObject ;
					if (attributes != null) {
						for (Attribute attribute : attributes) {
							returnValue.add(attribute);
						}
					}
				} else if (attributeObject instanceof Attribute){
					returnValue.add((Attribute)attributeObject);
				}
			}
		}
		return returnValue;
	}

	
	/**
	 * Returns a DocAttributeSet from a collection of attributes.
	 * @param attributes Collection of attributes to be examined.
	 * @return DocAttributeSet object. Never null.
	 */
	public static DocAttributeSet createDocAttributes(Collection<Attribute> attributes) {
		DocAttributeSet returnValue = new HashDocAttributeSet();
		if (attributes != null) {
			for (Attribute attribute: attributes) {
				if (attribute instanceof DocAttribute) {
					returnValue.add(attribute);
				}
			}
		}
		return returnValue;
	}

	/**
	 * Returns a PrintRequestAttributeSet from a collection of attributes.
	 * @param attributes Collection of attributes to be examined.
	 * @return PrintRequestAttributeSet object. Never null.
	 */
	public static PrintRequestAttributeSet createPrintRequestAttributes(Collection<Attribute> attributes) {
		PrintRequestAttributeSet returnValue = new HashPrintRequestAttributeSet();
		if (attributes != null) {
			for (Attribute attribute: attributes) {
				if (attribute instanceof PrintRequestAttribute) {
					returnValue.add(attribute);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns a serialization representation of the attributes.
	 * @param attributes List of the attributes to serialize.
	 * @return Serialize attributes or blank. Never null.
	 */
	public static String serializeAttributes(Collection<Attribute> attributes) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		AttributeOutputStream encoder = new AttributeOutputStream(stream);
		String returnValue = "";
		try {
			encoder.writeAttributes(attributes);
		} catch (IOException e) {
			returnValue = "";
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}
	
	/**
	 * Deserializes string into a collection of attributes.
	 * @param attributesData Serialized data.
	 * @return Collection of Attribute objects. Never null.
	 */
	public static Collection<Attribute> convertToAttributes(String attributesData) {
		Collection<Attribute> returnValue = new ArrayList<Attribute>();
		if (!Is.emptyString(attributesData)) {
			ByteArrayInputStream stream = new ByteArrayInputStream(attributesData.getBytes());
			AttributeInputStream decoder = new AttributeInputStream(stream);
			try {
				returnValue = decoder.readAttributes();
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
				stream = null;
				decoder = null;
			}
		}
		return returnValue;
	}

	/**
	 * Returns true if print service is an instance of RemotePrintService.
	 * However keep in mind that PrintService and RemotePrintService might be 
	 * loaded by different class loader thus not being registered as the
	 * same instance.
	 * @param printService PrintService to test.
	 * @return True if the service is an instance of RemotePrintService. 
	 */
	public static boolean isRemotePrintService(PrintService printService) {
		boolean returnValue = false;
		try {
			Method getUuid = printService.getClass().getDeclaredMethod("getUuid", new Class[]{});
			String uuid = ((String) getUuid.invoke(printService, new Object[]{}));
			returnValue = !Is.emptyString(uuid);
		} catch (SecurityException e) {
			LOG.debug(e.getMessage());
		} catch (NoSuchMethodException e) {
			LOG.debug(e.getMessage());
		} catch (IllegalArgumentException e) {
			LOG.debug(e.getMessage());
		} catch (IllegalAccessException e) {
			LOG.debug(e.getMessage());
		} catch (InvocationTargetException e) {
			LOG.debug(e.getMessage());
		}
		
		return returnValue;
	}
	
	/**
	 * Serialize print service categories.
	 * @param printService Print service to serialize
 	 * @param debugMode if true all errors are logged out.
	 * @return Serialize print service categories.
	 */
	public static String serializeServiceCategories(PrintService printService, boolean debugMode) {
		StringBuffer categories = new StringBuffer("");
		for (Class<? extends Attribute> category : PrintServiceUtils.getCategories(printService)) {
			if (categories.length() > 0) {
				categories.append(ParameterKeys.CATEGORIES_SEPARATOR);
			}
			categories.append(category.getName())
				.append(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER);
			String attributes = "";
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				AttributeOutputStream encoder = new AttributeOutputStream(stream);
				encoder.writeAttributes(getCategoryAttributes(printService, category));
				encoder.close();
				attributes = stream.toString();
			} catch (Exception e) {
				if (OUTPUT_LOG) {
					if (debugMode) {
						LOG.info(e.getMessage());
					} else {
						LOG.debug(e.getMessage());
					}
				}
			}
		
			categories.append(attributes);
		}
		return categories.toString();
	}
	
	/**
	 * Serialize service name
	 * @param service Service to be serialized
	 * @param debugMode If true all errors are logged out
	 * @return Serialized service name
	 */
	public static String serializeServiceName(PrintService printService, boolean debugMode) {
		StringBuffer printServiceRegister = new StringBuffer(ParameterKeys.PRINT_SERVICE_NAME)
		.append(ParameterKeys.PARAMETER_SEPARATOR)
		.append(printService.getName());
		return printServiceRegister.toString();
	}
	
	/**
	 * Deserialize printService and its categories
	 * @param printServiceName Print service to deserialize
	 * @param categoriesString List of categories.
	 * @return RemotePrintService
	 */
	public static RemotePrintService deSerializeService(String printServiceName, String categoriesString) {
		String serviceName = printServiceName.contains(ParameterKeys.PARAMETER_SEPARATOR) 
				? printServiceName.substring(printServiceName.lastIndexOf(ParameterKeys.PARAMETER_SEPARATOR) + 1) 
				: printServiceName;
		RemotePrintService remotePrintService = new RemotePrintService();
		remotePrintService.setUuid("");
		remotePrintService.setRemoteName(serviceName);
		remotePrintService.setRemoteComputerName("");
		if (!Is.emptyString(categoriesString)) {
			for (String categoryLine : categoriesString.split(ParameterKeys.CATEGORIES_SEPARATOR)) {
				String categoryName = categoryLine.substring(0, categoryLine.indexOf(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER));
				String attributes = categoryLine.substring(categoryLine.indexOf(ParameterKeys.CATEGORIES_ATTRIBUTES_STARTER) + 1);
				try {
					remotePrintService.getRemoteCategories().add(Class.forName(categoryName));
					if (!Is.emptyString(attributes)) {
						String[] attributeValues = attributes.split(ParameterKeys.ATTRIBUTES_SEPARATOR);
						if (attributeValues.length > 0) {
							List<Attribute> values = new ArrayList<Attribute>(); 
							for (String attributeValue : attributeValues) {
								try {
									ByteArrayInputStream stream = new ByteArrayInputStream(attributeValue.getBytes());
									AttributeInputStream input = new AttributeInputStream(stream);
									Attribute attribute = input.readAttribute();
									if (attribute != null) {
										values.add(attribute);
									}
								} catch (Exception e) {
									LOG.debug(e.getMessage());
								}
							}
							remotePrintService.getRemoteAttributes().put(categoryName, values);
						} else {
							try {
								remotePrintService.getRemoteAttributes().put(categoryName, 
										(Attribute)Class.forName(attributes).newInstance());
							} catch (InstantiationException e) {
								LOG.debug(e.getMessage());
							} catch (IllegalAccessException e) {
								LOG.debug(e.getMessage());
							}
						}
					}
				} catch (ClassNotFoundException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return remotePrintService;
	}
	
}
