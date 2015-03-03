package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.DocAttribute;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttribute;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.Media;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaTray;
import javax.print.attribute.standard.NumberUp;
import javax.print.attribute.standard.PageRanges;
import javax.print.attribute.standard.PrinterName;

import net.sf.wubiq.common.AttributeInputStream;
import net.sf.wubiq.common.AttributeOutputStream;
import net.sf.wubiq.common.ParameterKeys;
import net.sf.wubiq.common.PropertyKeys;
import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.enums.PrinterType;
import net.sf.wubiq.print.attribute.CustomMediaSize;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.print.services.RemotePrintServiceLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility for querying print service information.
 * @author Federico Alcantara
 *
 */
public class PrintServiceUtils {
	private static final Log LOG = LogFactory.getLog(PrintServiceUtils.class);
	public static boolean OUTPUT_LOG = false;
	public static DocFlavor DEFAULT_DOC_FLAVOR = DocFlavor.INPUT_STREAM.PDF;
	private static Map<String, String> compressionMap;
	private static Map<DocFlavor, String> docFlavorConversionMap;
	private static Set<String>photoPrinters;
	private static Set<String>dotMatrixHqPrinters;
	private static Set<String>dotMatrixPrinters;
	private static final String VALID_PRINT_SERVICE_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	/**
	 * Gets the list of possible service providers.
	 * @return Service providers. Never null.
	 */
	@SuppressWarnings("unchecked")
	public static List<PrintServiceLookup> getServiceProviders() {
		List<PrintServiceLookup> lookupPrintServices = new ArrayList<PrintServiceLookup>();
		Method method;
		try {
			method = PrintServiceLookup.class.getDeclaredMethod("getAllLookupServices", new Class[]{});
			method.setAccessible(true);
			List<PrintServiceLookup> providers = (List<PrintServiceLookup>) method.invoke(null, new Object[]{});
			if (providers != null) {
				lookupPrintServices.addAll(providers);
			}
		} catch (Exception e) {
			LOG.fatal(e.getMessage(), e);
		}
		return lookupPrintServices;
	}
	
	/**
	 * Tries to refresh print services.
	 */
	@SuppressWarnings("rawtypes")
	public static void refreshServices(){
		try {
			List lookupPrintServices = getServiceProviders();
			for (Object object : lookupPrintServices) {
				LOG.debug("Trying to refresh:" + object.getClass());
				Method refreshServices;
				try {
					refreshServices = object.getClass().getDeclaredMethod("refreshServices", new Class[]{});
					refreshServices.setAccessible(true);
					refreshServices.invoke(object, new Object[]{});
					LOG.debug("  refreshServices executed on:" + object.getClass());
				} catch (Exception e) {
					LOG.info("  Error trying refreshServices on:" + object.getClass() + " -> " + e.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
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
	 * Gets print services belonging to a given group and optionally the locals print services and the non grouped print services.
	 * @param group Group to look for. Null or empty group will result in return non grouped print services.
	 * @param includeLocals If true also local print services will be returned.
	 * @param includeNonGrouped If true also adds the non grouped print services. This parameter has no use if group is given empty or null.
	 * @return An array containing the selected print services.
	 */
	public static PrintService[] getPrintServices(String group, boolean includeLocals, boolean includeNonGrouped) {
		List<PrintService> returnValue = new ArrayList<PrintService>();
		for (PrintService printService : getPrintServices()) {
			if (isRemotePrintService(printService)) {
				RemotePrintService remotePrintService = (RemotePrintService)printService;
				if (Is.emptyString(group) && remotePrintService.getGroups().isEmpty()) {
					returnValue.add(printService);
				} else if (remotePrintService.getGroups().contains(group.toLowerCase())) {
					returnValue.add(printService);
				} else if (remotePrintService.getGroups().isEmpty() &&
						includeNonGrouped) {
					returnValue.add(printService);
				}
			} else {
				if (includeLocals) {
					returnValue.add(printService);
				}
			}
		}
		
		return returnValue.toArray(new PrintService[0]);
	}
	
	/**
	 * Replaces a print service from its corresponding lookup with a new one.
	 * @param printService Print service to replace.
	 * @param newPrintService New print service to add to the list.
	 * @return The print service just added.
	 */
	@SuppressWarnings("static-access")
	public static PrintService replacePrintService(PrintService printService, PrintService newPrintService) {
		PrintService returnValue = newPrintService;
		for (PrintServiceLookup lookup : getServiceProviders()) {
			// Just try a simple registration
			if (!lookup.registerService(newPrintService)) {
				Field field = null;
				try {
					field = lookup.getClass().getDeclaredField("printServices");
				} catch (Exception e) {
					LOG.debug(e.getMessage());
				}
				if (field == null) {
					try {
						field = lookup.getClass().getField("printServices");
					} catch (Exception e) {
						LOG.debug(e.getMessage());
					}
				}
				if (field != null) {
					try {
						field.setAccessible(true);
						PrintService[] printServices = (PrintService[])field.get(lookup);
						for (int index = 0; index < printServices.length; index++) {
							if (newPrintService.equals(printServices[index])) {
								printServices[index] = newPrintService;
							}
						}
					} catch (Exception e) {
						LOG.debug(e.getMessage());
					}
				} else if (lookup instanceof RemotePrintServiceLookup) {
					RemotePrintServiceLookup remote = (RemotePrintServiceLookup) lookup;
					remote.registerRemoteService(newPrintService);
				}
			}
		}
		return returnValue;
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
	 * Based on its name and uuid finds a printer.
	 * @param name Name of the printer.
	 * @param uuid Unique remote id.
	 * @return A PrinterService or null;
	 */
	public static PrintService findPrinter(String name, String uuid) {
		if (!Is.emptyString(uuid)) {
			return findPrinter(name + WebKeys.REMOTE_SERVICE_SEPARATOR + uuid);
		}
		return findPrinter(name);
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
		boolean supported = printService.isAttributeCategorySupported(Copies.class);
		if (printService != null && copiesCount != null &&
				supported) {
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
				addAttribute(returnValue, Chromaticity.MONOCHROME);
				addAttribute(returnValue, Chromaticity.COLOR);
			} else if (category.equals(MediaTray.class) || category.equals(MediaSizeName.class)) {
				Object attributeValue = printService.getSupportedAttributeValues(Media.class, null, null);
				Attribute[] attributes = null;
				if (attributeValue instanceof Attribute[]){
					attributes = (Attribute[]) printService.getSupportedAttributeValues(Media.class, null, null);
				} else {
					attributes = new Attribute[]{(Attribute)attributeValue};
				}
				if (attributes != null) {
					for (Attribute attribute : attributes) {
						if ((attribute instanceof MediaTray && category.equals(MediaTray.class)) ||
								attribute instanceof MediaSizeName && category.equals(MediaSizeName.class)) {
							if (!attribute.toString().equalsIgnoreCase("Custom")) {
								addAttribute(returnValue, attribute);
							}
						}
					}
				}				
			} else if (category.equals(Copies.class)) {
				CopiesSupported copiesSupported = (CopiesSupported) printService.getSupportedAttributeValues(Copies.class, null, null);
				addAttribute(returnValue, copiesSupported);
			} else  {
				Object attributeObject = printService.getSupportedAttributeValues(category, null, null);
				if (attributeObject instanceof Attribute[]) {
					Attribute[] attributes = (Attribute[]) attributeObject ;
					if (attributes != null) {
						for (Attribute attribute : attributes) {
							addAttribute(returnValue, attribute);
						}
					}
				} else if (attributeObject instanceof Attribute){
					addAttribute(returnValue, (Attribute)attributeObject);
				}
			}
		}
		return returnValue;
	}

	/**
	 * Ensures no duplicates of attributes in the collection.
	 * @param attributes Collection of attributes
	 * @param attribute attribute to add
	 */
	private static void addAttribute(Collection<Attribute> attributes, Attribute attribute) {
		if (attribute != null) {
			if (!attributes.contains(attribute)) {
				attributes.add(attribute);
			}
		}
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
			returnValue = stream.toString();
		} catch (IOException e) {
			try {
				encoder.close();
			} catch (IOException e1) {
				LOG.debug(e1.getMessage());
			}
			returnValue = "";
			LOG.error(e.getMessage(), e);
		}
		return returnValue;
	}
	/**
	 * Returns a serialization representation of the attributes.
	 * @param attributeSet Attribute set to serialize.
	 * @return Serialize attributes or blank. Never null.
	 */
	public static String serializeAttributes(AttributeSet attributeSet) {
		Collection<Attribute>attributes = new HashSet<Attribute>();
		if (attributeSet != null) {
			for (Attribute attribute: attributeSet.toArray()) {
				attributes.add(attribute);
			}
		}
		return serializeAttributes(attributes);
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
				} catch (IOException e1) {
					LOG.debug(e1.getMessage());
				}
				try {
					decoder.close();
				} catch(IOException e1) {
					LOG.debug(e1.getMessage());
				}
				stream = null;
				decoder = null;
			}
		}
		return returnValue;
	}

	/**
	 * Deserializes string into an attribute set.
	 * @param attributesData Serialized data.
	 * @return AttributeSet object. Never null.
	 */
	public static DocAttributeSet convertToDocAttributeSet(String attributesData) {
		DocAttributeSet returnValue = new HashDocAttributeSet();
		convertAttributeSet(attributesData, returnValue);
		return returnValue;
	}

	/**
	 * Deserializes string into an attribute set.
	 * @param attributesData Serialized data.
	 * @return AttributeSet object. Never null.
	 */
	public static PrintRequestAttributeSet convertToPrintRequestAttributeSet(String attributesData) {
		PrintRequestAttributeSet returnValue = new HashPrintRequestAttributeSet();
		convertAttributeSet(attributesData, returnValue);
		return returnValue;
	}
	
	/**
	 * Deserializes string into an attribute set.
	 * @param attributesData Serialized data.
	 * @return AttributeSet object. Never null.
	 */
	public static PrintJobAttributeSet convertToPrintJobAttributeSet(String attributesData) {
		PrintJobAttributeSet returnValue = new HashPrintJobAttributeSet();
		convertAttributeSet(attributesData, returnValue);
		return returnValue;
	}

	private static void convertAttributeSet(String attributesData, AttributeSet attributeSet) {
		Collection<Attribute>attributes = convertToAttributes(attributesData);
		for (Attribute attribute : attributes) {
			if (attribute != null) {
				attributeSet.add(attribute);
			}
		}
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
			Method getUuid = getRemotePrintServiceUuidMethod(printService.getClass());
			if (getUuid != null) {
				String uuid = ((String) getUuid.invoke(printService, new Object[]{}));
				returnValue = !Is.emptyString(uuid);
			}
		} catch (InvocationTargetException e) {
			LOG.debug(e.getMessage());
		} catch (IllegalAccessException e) {
			LOG.debug(e.getMessage());
		} catch (IllegalArgumentException e) {
			LOG.debug(e.getMessage());
		}
		
		return returnValue;
	}
	
	/**
	 * Lookup for getUuid method within the given class or its parent.
	 * @param clazz Class to be searched.
	 * @return A method or null if not found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Method getRemotePrintServiceUuidMethod(Class clazz) {
		Method uuid = null;
		if (clazz !=null) {
			try {
				uuid = clazz.getDeclaredMethod("getUuid", new Class[]{});
			} catch (Exception e) {
				LOG.debug(e.getMessage());
			}
			if (uuid == null) {
				uuid = getRemotePrintServiceUuidMethod(clazz.getSuperclass());
			}
		}
		return uuid;
	}
	
	/**
	 * Returns true if print service is a Mobile Remote print service.
	 * However keep in mind that PrintService and RemotePrintService might be 
	 * loaded by different class loader thus not being registered as the
	 * same instance.
	 * @param printService PrintService to test.
	 * @return True if the service is an instance of RemotePrintService. 
	 */
	public static boolean isMobilePrintService(PrintService printService) {
		boolean returnValue = false;
		try {
			Method isMobileMethod = printService.getClass().getDeclaredMethod("isMobile", new Class[]{});
			Boolean isMobile = ((Boolean) isMobileMethod.invoke(printService, new Object[]{}));
			returnValue = isMobile;
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
		} catch (NullPointerException e) {
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
		return compressAttributes(categories.toString());
	}
	
	/**
	 * Serialize print service's document flavors.
	 * @param printService Print service to look into.
	 * @return String representing the document flavors. Never null.
	 */
	public static String serializeDocumentFlavors(PrintService printService) {
		StringBuffer docFlavors = new StringBuffer("");
		Set<DocFlavor> flavors = new HashSet<DocFlavor>();
		for (DocFlavor docFlavor : printService.getSupportedDocFlavors()) {
			System.out.println(docFlavor);
			flavors.add(docFlavor);
		}
		for (DocFlavor docFlavor : flavors) {
			if (docFlavors.length() > 0) {
				docFlavors.append(ParameterKeys.CATEGORIES_SEPARATOR);
			}
			docFlavors.append(PrintServiceUtils.serializeDocFlavor(docFlavor));
		}
		docFlavors.insert(0, ParameterKeys.PARAMETER_SEPARATOR);
		docFlavors.insert(0, ParameterKeys.PRINT_SERVICE_DOC_FLAVORS);
		return docFlavors.toString();
	}
	
	public static DocFlavor[] deserializeDocumentFlavors(String docFlavors) {
		String docs[] = docFlavors.split(ParameterKeys.CATEGORIES_SEPARATOR);
		DocFlavor[] returnValue = new DocFlavor[]{DEFAULT_DOC_FLAVOR};
		if (docs.length > 0) {
			returnValue = new DocFlavor[docs.length];
			for (int index = 0; index < docs.length; index++) {
				DocFlavor docFlavor = deSerializeDocFlavor(docs[index]);
				if (docFlavor != null) {
					returnValue[index] = docFlavor;
				} else {
					returnValue[index] = DEFAULT_DOC_FLAVOR;
				}
			}
		}
		return returnValue;
	}
	/**
	 * Serialize service name
	 * @param debugMode If true all errors are logged out
	 * @return Serialized service name
	 */
	public static String serializeServiceName(PrintService printService, boolean debugMode) {
		StringBuffer printServiceRegister = new StringBuffer(ParameterKeys.PRINT_SERVICE_NAME)
		.append(ParameterKeys.PARAMETER_SEPARATOR)
		.append(cleanPrintServiceName(printService));
		return printServiceRegister.toString();
	}


	/**
	 * Deserialize printService and its categories
	 * @param printServiceName Print service to deserialize
	 * @return RemotePrintService
	 */
	public static RemotePrintService deSerializeService(String printServiceName, String compressedCategoriesString) {
		String categoriesString = deCompressAttributes(compressedCategoriesString);
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
								AttributeInputStream input = null;
								try {
									ByteArrayInputStream stream = new ByteArrayInputStream(attributeValue.getBytes());
									input = new AttributeInputStream(stream);
									Attribute attribute = input.readAttribute();
									if (attribute != null) {
										values.add(attribute);
									}
								} catch (Exception e) {
									LOG.debug(e.getMessage());
								} finally {
									try {
										if (input != null) {
											input.close();
										}
									} catch (IOException e1) {
										LOG.debug(e1.getMessage());
									}
								}
								
							}
							remotePrintService.getRemoteAttributes().put(categoryName, values.toArray(new Attribute[]{}));
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
	
	/**
	 * Compresses the attribute list of a print service.
	 * @param attributeList String representing the list of valid attributes.
	 * @return Compressed representation of the attribute list.
	 */
	public static String compressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getKey(), entry.getValue());
		}
		return returnValue;
	}
	
	/**
	 * Uncompress a previously compressed list of attributes.
	 * @param attributeList Attribute list is compressed format.
	 * @return String representing the attribute list.
	 */
	public static String deCompressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getValue(), entry.getKey());
		}
		return returnValue;
	}

	/**
	 * Serializes a Doc flavor object.
	 * @param docFlavor Doc flavor object to serialize.
	 * @return A String representing a DocFlavor object.
	 */
	public static String serializeDocFlavor(DocFlavor docFlavor) {
		String returnValue = getDocFlavorConversionMap().get(docFlavor);
		if (returnValue == null) {
			returnValue = getDocFlavorConversionMap().get(DEFAULT_DOC_FLAVOR);
		}
		return returnValue;
	}

	/**
	 * Converts a doc flavor class name into a DocFlavor object.
	 * @param serializedDocFlavor Class name for the DocFlavor.
	 * @return Instance of DocFlavor.
	 */
	public static DocFlavor deSerializeDocFlavor(String serializedDocFlavor) {
		DocFlavor returnValue = null;
		if (serializedDocFlavor != null && !serializedDocFlavor.equals("")) {
			for (Entry<DocFlavor, String> entry : getDocFlavorConversionMap().entrySet()) {
				if (entry.getValue().equals(serializedDocFlavor)) {
					returnValue = entry.getKey();
					break;
				}
			}
		}
		if (returnValue == null) {
			returnValue = DEFAULT_DOC_FLAVOR;
		}
		return returnValue;
	}
	
	/**
	 * @param printService Returns a cleaned print service name.
	 * @return A cleaned (no strange characters) print service name.
	 */
	public static String cleanPrintServiceName(PrintService printService) {
		return cleanPrintServiceName(printService.getName());
	}

	/**
	 * @param printServiceName Returns a cleaned print service name.
	 * @return A cleaned (no strange characters) print service name.
	 */
	public static String cleanPrintServiceName(String printServiceName) {
		StringBuffer returnValue = new StringBuffer("");
		for (int index = 0; index < printServiceName.length(); index++) {
			char charAt = printServiceName.charAt(index);
			if (VALID_PRINT_SERVICE_CHARACTERS.indexOf(charAt) >= 0) {
				returnValue.append(charAt);
			} else {
				returnValue.append("_");
			}
		}
		return returnValue.toString();
	}

	/**
	 * Extract the copies count from the request attributes.
	 * @param attributes Print request attributes.
	 * @return Number of copies, at least 1.
	 */
	public static int getCopies(PrintRequestAttributeSet attributes) {
		int returnValue = 1;
		for (Attribute attribute : attributes.toArray()) {
			if (attribute instanceof Copies) {
				Copies copies = (Copies)attribute;
				returnValue = copies.getValue();
				if (returnValue < 1) {
					returnValue = 1;
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Removes the given category from the print request.
	 * @param attributes Print request attributes.
	 * @return a new Print request set of attributes without copies and related.
	 */
	public static PrintRequestAttributeSet removeCategoryAttribute(PrintRequestAttributeSet attributes, Class<? extends Attribute> category) {
		PrintRequestAttributeSet returnValue = new HashPrintRequestAttributeSet();
		if (attributes != null) {
			for (Attribute attribute : attributes.toArray()) {
				if (!attribute.getCategory().equals(category)) {
					returnValue.add(attribute);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Find a given attribute.
	 * @param attributes Set of attributes.
	 * @param category Category to get the attribute from.
	 * @return Found attribute or null.
	 */
	public static Attribute findCategoryAttribute(AttributeSet attributes, Class<? extends Attribute> category) {
		Attribute returnValue = null;
		if (attributes != null) {
			for (Attribute attribute : attributes.toArray()) {
				if (attribute.getCategory().equals(category)) {
					returnValue = attribute;
					break;
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns the type of printers.
	 * @param printService Print service associated with the printer.
	 * @return Printer type.
	 */
	public static PrinterType printerType(PrintService printService) {
		String printerName = printService.getName();
		if (Is.emptyString(printerName)) {
			// try to get it from category;
			PrinterName printerNameAttr = printService.getAttribute(PrinterName.class);
			if (printerNameAttr != null) {
				printerName = printerNameAttr.getValue();
			}
		}
		return printerType(printerName);
 	}
	
	/**
	 * Returns the type of printers.
	 * @param sentPrinterName Name of the printer.
	 * @return Printer type.
	 */
	public static PrinterType printerType(String sentPrinterName) {
		PrinterType returnValue = PrinterType.UNDEFINED;
		String printerName = sentPrinterName.trim().toUpperCase();
		if (photoPrinters == null) {
			photoPrinters = printerTypeStrings(PropertyKeys.WUBIQ_PRINTERS_PHOTO, "photo,jet,laser");
		}
		if (dotMatrixHqPrinters == null) {
			dotMatrixHqPrinters = printerTypeStrings(PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX_HQ, 
					" LQ,24-pin,24 pin,KX-P,KXP");
		}
		if (dotMatrixPrinters == null) {
			dotMatrixPrinters = printerTypeStrings(PropertyKeys.WUBIQ_PRINTERS_DOTMATRIX, 
					" lx-, lx , fx-, fx , mx-, mx , rx-, rx , ml-42,9-pin,9 pin");
		}
		if (containsString(photoPrinters, printerName)) {
			returnValue = PrinterType.LASER_INK_JET;
		} else if (containsString(dotMatrixPrinters, printerName)) {
			returnValue = PrinterType.DOT_MATRIX;
		} else if (containsString(dotMatrixHqPrinters, printerName)) {
			returnValue = PrinterType.DOT_MATRIX_HQ;
		}
		return returnValue;
	}
	
	/**
	 * Returns a list of printer names.
	 * @param propertyName Name of the property to read additional list from.
	 * @param defaultValues Default values.
	 * @return a Set of printer names. Never null.
	 */
	private static Set<String> printerTypeStrings(String propertyName, String defaultValues) {
		Set<String> returnValue = new HashSet<String>();
		returnValue.addAll(parsePrinterTypeString(defaultValues));
		String values = System.getProperty(propertyName, "");
		returnValue.addAll(parsePrinterTypeString(values));
		return returnValue;
	}
	
	/**
	 * Parse a comma separated list of printers IPP names.
	 * @param printersList comma separated list.
	 * @return A Set of printer names. Never null.
	 */
	private static Set<String> parsePrinterTypeString(String printersList) {
		Set<String> returnValue = new HashSet<String>();
		if (!Is.emptyString(printersList)) {
			String[] values = printersList.toUpperCase().split(",");
			for (String value : values) {
				if (!Is.emptyString(value.trim())) {
					returnValue.add(value);
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Finds out if the printer name has a match within the list.
	 * @param printersList List of printers.
	 * @param printerName Printer name.
	 * @return True if the printer name is within the list.
	 */
	private static boolean containsString(Set<String> printersList, String printerName) {
		boolean returnValue = false;
		for (String printer : printersList) {
			if (printerName.contains(printer)) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	private static Map<String, String> getCompressionMap() {
		if (compressionMap == null) {
			compressionMap = new LinkedHashMap<String, String>();
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Chromaticity", "xCHRx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.CopiesSupported", "xCSUx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Copies", "xCOPx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Destination", "xDSTx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Fidelity", "xFIDx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Finishings", "xFINx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.JobSheets", "xJSHx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.JobName", "xJNAx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.MediaPrintableArea", "xMPAx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.MediaSizeName", "xMSNx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.MediaTray", "xMTRx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Media", "xMEDx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.NumberUp", "xNUPx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.OrientationRequested", "xORQx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.PageRanges", "xPRAx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.PrinterResolution", "xPREx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.RequestingUserName", "xRUNx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.SheetCollate", "xSCOx");
			compressionMap.put("javax\\.print\\.attribute\\.standard\\.Sides", "xSIDx");
			compressionMap.put("net\\.sf\\.wubiq\\.print\\.attribute\\.OriginalOrientationRequested", "xOORx");
			compressionMap.put("sun\\.print\\.CustomMediaSizeName", "xCMSx");
			compressionMap.put("sun\\.print\\.SunAlternateMedia", "xSAMx");
			compressionMap.put("sun\\.print\\.Win32MediaSize", "xWMSx");
			compressionMap.put("sun\\.print\\.Win32MediaTray", "xWMTx");
		}
		return compressionMap;
	}

	private static Map<DocFlavor, String> getDocFlavorConversionMap() {
		if (docFlavorConversionMap == null) {
			docFlavorConversionMap = new HashMap<DocFlavor, String>();

			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.AUTOSENSE, "B_A.AS");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.GIF, "B_A.GIF");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.JPEG, "B_A.JPEG");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.PCL, "B_A.PCL");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.PDF, "B_A.PDF");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.PNG, "B_A.PNG");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.POSTSCRIPT, "B_A.PS");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST, "B_A.THH");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_US_ASCII, "B_A.THUA");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16, "B_A.THU16");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16BE, "B_A.THU16BE");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_16LE, "B_A.THU16LE");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_HTML_UTF_8, "B_A.THU8");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST, "B_A.TPH");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII, "B_A.TPUA");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16, "B_A.TPU16");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16BE, "B_A.TPU16BE");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_16LE, "B_A.TPU16LE");
			docFlavorConversionMap.put(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8, "B_A.TPU8");
			
			docFlavorConversionMap.put(DocFlavor.CHAR_ARRAY.TEXT_HTML, "C_A.TH");
			docFlavorConversionMap.put(DocFlavor.CHAR_ARRAY.TEXT_PLAIN, "C_A.TP");
			

			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.AUTOSENSE, "I_S.AS");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.GIF, "I_S.GIF");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.JPEG, "I_S.JPEG");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.PCL, "I_S.PCL");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.PDF, "I_S.PDF");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.PNG, "I_S.PNG");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.POSTSCRIPT, "I_S.PS");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_HOST, "I_S.THH");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_US_ASCII, "I_S.THUA");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16, "I_S.THU16");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16BE, "I_S.THU16BE");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_16LE, "I_S.THU16LE");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_HTML_UTF_8, "I_S.THU8");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST, "I_S.TPH");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII, "I_S.TPUA");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16, "I_S.TPU16");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16BE, "I_S.TPU16BE");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_16LE, "I_S.TPU16LE");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_UTF_8, "I_S.TPU8");

			docFlavorConversionMap.put(DocFlavor.READER.TEXT_HTML, "R.TH");
			docFlavorConversionMap.put(DocFlavor.READER.TEXT_PLAIN, "R.TP");

			docFlavorConversionMap.put(DocFlavor.SERVICE_FORMATTED.PAGEABLE, "S_F.PA");
			docFlavorConversionMap.put(DocFlavor.SERVICE_FORMATTED.PRINTABLE, "S_F.PR");
			docFlavorConversionMap.put(DocFlavor.SERVICE_FORMATTED.RENDERABLE_IMAGE, "S_F.RI");

			docFlavorConversionMap.put(DocFlavor.STRING.TEXT_HTML, "S.TH");
			docFlavorConversionMap.put(DocFlavor.STRING.TEXT_PLAIN, "S.TP");
			
			docFlavorConversionMap.put(DocFlavor.URL.AUTOSENSE, "U.AS");
			docFlavorConversionMap.put(DocFlavor.URL.GIF, "U.GIF");
			docFlavorConversionMap.put(DocFlavor.URL.JPEG, "U.JPEG");
			docFlavorConversionMap.put(DocFlavor.URL.PCL, "U.PCL");
			docFlavorConversionMap.put(DocFlavor.URL.PDF, "U.PDF");
			docFlavorConversionMap.put(DocFlavor.URL.PNG, "U.PNG");
			docFlavorConversionMap.put(DocFlavor.URL.POSTSCRIPT, "U.PS");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_HOST, "U.THH");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_US_ASCII, "U.THUA");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_UTF_16, "U.THU16");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_UTF_16BE, "U.THU16BE");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_UTF_16LE, "U.THU16LE");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_HTML_UTF_8, "U.THU8");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_HOST, "U.TPH");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_US_ASCII, "U.TPUA");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_UTF_16, "U.TPU16");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_UTF_16BE, "U.TPU16BE");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_UTF_16LE, "U.TPU16LE");
			docFlavorConversionMap.put(DocFlavor.URL.TEXT_PLAIN_UTF_8, "U.TPU8");

		}
		return docFlavorConversionMap;
	}
	
	/**
	 * Determines if a print service supports the given doc flavor.
	 * @param printService Print Service to test about.
	 * @param docFlavor Doc flavor to test.
	 * @return True if the print service support the flavor. False otherwise.
	 */
	public static boolean supportDocFlavor(PrintService printService, DocFlavor docFlavor) {
		boolean returnValue = false;
		for (DocFlavor psDocFlavor : printService.getSupportedDocFlavors()) {
			if (psDocFlavor.toString().equals(docFlavor.toString())) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns true if the print service has the same version as the
	 * server.
	 * @param printService
	 * @return True if the print service represents a local print service or if the
	 * remote print service is the same as the server version.
	 */
	public static boolean isSameVersion(PrintService printService) {
		boolean returnValue = true;
		if (printService instanceof RemotePrintService) {
			returnValue = Labels.VERSION
					.equals(((RemotePrintService) printService).getClientVersion());
		}
		return returnValue;
	}
	/**
	 * Gets the client version that is connecting to.
	 * @param printService Print service to poll.
	 * @return Client version or <2.0.
	 */
	public static String getClientVersion(PrintService printService) {
		String returnValue = "< 2.0";
		if (isRemotePrintService(printService)) {
			RemotePrintService remote = (RemotePrintService) printService;
			if (!Is.emptyString(remote.getClientVersion())) {
				returnValue = remote.getClientVersion();
			}
		}
		return returnValue;
	}

	/**
	 * Indicates if the client supports compression.
	 * @param printService Print service to test.
	 * @return True if the client supports version, false otherwise.
	 */
	public static boolean clientSupportsCompression(PrintService printService) {
		boolean returnValue = false;
		double currentClientVersion = convertedVersion(getClientVersion(printService));
		double expectedVersion = convertedVersion("2.1");
		if (currentClientVersion >= expectedVersion) {
			return true;
		}
		return returnValue;
	}
	
	/**
	 * Converts a version to a numeric value for comparison purposes.
	 * @param version Version string to compare. Format version.sub-version[.build]<br/>
	 * for example: 2.0.10 = 2000010, 2.1 = 2001000, 2.1.1 = 2001001
	 * @return A representation of the convertedVersion.
	 */
	public static int convertedVersion(String version) {
		int returnValue = 1;
		if (!Is.emptyString(version) && !version.startsWith("<")) {
			StringBuffer buffer = new StringBuffer("");
			int value = 0;
			int multiplier = 1000000;
			for (int index = 0; index < version.length(); index ++) {
				char charAt = version.charAt(index);
				if (charAt == '.') {
					value = value + (Integer.parseInt(buffer.toString()) * multiplier);
					multiplier = multiplier / 1000;
					buffer = new StringBuffer("");
				} else {
					buffer.append(charAt);
				}
			}
			returnValue = value + (buffer.length() > 0 ? Integer.parseInt(buffer.toString()) * multiplier : 0);
		}
		return returnValue;
	}
	
	/**
	 * Finds a suitable media size name. If it can't find an exact match then it CREATES and register a new set
	 * of MediaSizeName and MediaSize. It allows landscape dimensions.
	 * @param width Width of the paper.
	 * @param height Height of the paper.
	 * @param units Units used. It can be MM or INCH.
	 * @return Compatible MediaSizeName, never null.
	 */
	public static MediaSizeName findMedia(float width, float height, int units) {
		MediaSizeName returnValue = MediaSize.findMedia(width, height, units);
		boolean createNew = false;
		if (returnValue == null) {
			createNew = true;
		} else {
			MediaSize mediaSize = MediaSize.getMediaSizeForName(returnValue);
			if (mediaSize.getX(units) != width ||
					mediaSize.getY(units) != height) {
				createNew = true;
			}
		}
		if (createNew) {
			CustomMediaSize.createCustomMediaSize(width, height, units);
			returnValue = MediaSize.findMedia(width, height, units);
		}
		return returnValue;
	}
	
	/**
	 * Tries to produce a compatible printable area.
	 * @param printService Print service to check the printable area from.
	 * @param flavor Doc flavor to use. It can be null.
	 * @param media MediaSizeName to check the contraints. If null returns the default MediaPrintableArea for the printer.
	 * @return MediaPrintableArea or null.
	 */
	public static MediaPrintableArea getMediaPrintableArea(PrintService printService, DocFlavor flavor, MediaSizeName media) {
		MediaPrintableArea returnValue = null;
		if (media != null) {
			PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
			attributes.add(media);
			MediaPrintableArea defaultArea = (MediaPrintableArea) printService.getDefaultAttributeValue(MediaPrintableArea.class);
			if (defaultArea != null) {
				MediaSize mediaSize = MediaSize.getMediaSizeForName(media);
				if (mediaSize != null) {
					returnValue = new MediaPrintableArea(defaultArea.getX(MediaSize.MM), 
							defaultArea.getY(MediaSize.MM),
							mediaSize.getX(MediaSize.MM) - defaultArea.getX(MediaSize.MM),
							mediaSize.getY(MediaSize.MM) - defaultArea.getY(MediaSize.MM),
							MediaSize.MM);
				}
			}
		}
		return returnValue;
	}
}
