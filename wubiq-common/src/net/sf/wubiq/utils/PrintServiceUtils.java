package net.sf.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.DocFlavor;
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
import net.sf.wubiq.print.pdf.PdfImagePage;
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
	public static boolean OUTPUT_LOG = false;
	public static DocFlavor DEFAULT_DOC_FLAVOR = DocFlavor.INPUT_STREAM.AUTOSENSE;
	private static String DEFAULT_DOC_FLAVOR_NAME = "INPUT_STREAM.AUTOSENSE";
	private static int DEFAULT_RESOLUTION = 192;
	private static Map<String, String> compressionMap;
	private static Map<DocFlavor, String> docFlavorConversionMap;
	private static List<DocFlavor> implementedDocFlavors;
	/**
	 * Tries to refresh print services.
	 */
	@SuppressWarnings("rawtypes")
	public static void refreshServices(){
		Method method;
		try {
			method = PrintServiceLookup.class.getDeclaredMethod("getAllLookupServices", new Class[]{});
			method.setAccessible(true);
			List lookupPrintServices = (List) method.invoke(null, new Object[]{});
			for (Object object : lookupPrintServices) {
				LOG.info("Trying to refresh:" + object.getClass());
				Method refreshServices;
				try {
					refreshServices = object.getClass().getDeclaredMethod("refreshServices", new Class[]{});
					refreshServices.setAccessible(true);
					refreshServices.invoke(object, new Object[]{});
					LOG.info("  refreshServices executed on:" + object.getClass());
				} catch (Exception e) {
					LOG.info("  Error trying refreshServices on:" + object.getClass() + " -> " + e.getMessage());
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
				addAttribute(returnValue, Chromaticity.MONOCHROME);
				addAttribute(returnValue, Chromaticity.COLOR);
			} else if (category.equals(MediaTray.class) || category.equals(MediaSizeName.class)) {
				Attribute[] attributes = (Attribute[]) printService.getSupportedAttributeValues(Media.class, null, null);
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
		if (!attributes.contains(attribute)) {
			attributes.add(attribute);
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
		return compressAttributes(categories.toString());
	}
	
	/**
	 * Serialize print service's document flavors.
	 * @param printService Print service to look into.
	 * @param debugMode If true errors are debugged.
	 * @return String representing the document flavors. Never null.
	 */
	public static String serializeDocumentFlavors(PrintService printService, boolean debugMode) {
		StringBuffer docFlavors = new StringBuffer("");
		for (DocFlavor docFlavor : printService.getSupportedDocFlavors()) {
			if (getImplementedDocFlavors().contains(docFlavor)) {
				if (docFlavors.length() > 0) {
					docFlavors.append(ParameterKeys.CATEGORIES_SEPARATOR);
				}
				docFlavors.append(PrintServiceUtils.serializeDocFlavor(docFlavor));
			}
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
		.append(printService.getName().replaceAll("\\\\", "/"));
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
	
	public static String compressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getKey(), entry.getValue());
		}
		return returnValue;
	}
	
	public static String deCompressAttributes(String attributeList) {
		String returnValue = attributeList;
		for (Entry<String, String> entry : getCompressionMap().entrySet()) {
			returnValue = returnValue.replaceAll(entry.getValue(), entry.getKey());
		}
		return returnValue;
	}

	
	/**
	 * If necessary converts a pdf to another document flavor.
	 * @param pdf Pdf to convert.
 	 * @param docFlavor Expected document flavor.
	 * @return InputStream in the proper format.
	 * @throws IOException
	 */
	public static List<PdfImagePage> convertToProperStream(InputStream pdf, DocFlavor docFlavor) throws IOException {
		List<PdfImagePage> returnValue = new ArrayList<PdfImagePage>();
		if (!docFlavor.equals(DocFlavor.INPUT_STREAM.PDF) 
				&& !docFlavor.equals(DocFlavor.INPUT_STREAM.AUTOSENSE)) {
			returnValue.clear();
			if (docFlavor.equals(DocFlavor.INPUT_STREAM.PNG)) {
				returnValue = PdfUtils.INSTANCE.convertPdfToPng(pdf, DEFAULT_RESOLUTION);
			}
			if (docFlavor.equals(DocFlavor.INPUT_STREAM.JPEG)) {
				returnValue = PdfUtils.INSTANCE.convertPdfToJpg(pdf, DEFAULT_RESOLUTION);
			}
		} else {
			returnValue = null;
		}
		return returnValue;
	}
	
	/**
	 * Determines which is the best doc flavor for the given print service.
	 * @param printService Print service to check.
	 * @return Best DocFlavor for the Print Service.
	 */
	public static DocFlavor determineDocFlavor(PrintService printService) {
		DocFlavor returnValue = DocFlavor.INPUT_STREAM.PDF;
		if (!isDocFlavorSupported(printService, returnValue)) {
			returnValue = DocFlavor.INPUT_STREAM.PNG;
		}
		if (!isDocFlavorSupported(printService, returnValue)) {
			returnValue = DocFlavor.INPUT_STREAM.JPEG;
		}
		if (!isDocFlavorSupported(printService, returnValue)) {
			returnValue = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
		}
		if (!isDocFlavorSupported(printService, returnValue)) {
			returnValue = DocFlavor.INPUT_STREAM.AUTOSENSE;
		}
		return returnValue;
	}

	/**
	 * Returns true if the doc flavor is supported.
	 * @param printService Print service to check.
	 * @param docFlavor Document flavor to test.
	 * @return True if supported, false otherwise.
	 */
	private static boolean isDocFlavorSupported(PrintService printService, DocFlavor docFlavor) {
		boolean returnValue = false;
		for (DocFlavor readDocFlavor : printService.getSupportedDocFlavors()) {
			if (docFlavor.equals(readDocFlavor)) {
				returnValue = true;
				break;
			}
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
			returnValue = DEFAULT_DOC_FLAVOR_NAME;
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
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.AUTOSENSE, "I_S.AS");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.PDF, "I_S.PDF");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.PNG, "I_S.PNG");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.JPEG, "I_S.JPEG");
			docFlavorConversionMap.put(DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST, "I_S.TXT");
		}
		return docFlavorConversionMap;
	}
	
	private static List<DocFlavor> getImplementedDocFlavors() {
		if (implementedDocFlavors == null) {
			implementedDocFlavors = new ArrayList<DocFlavor>();
			implementedDocFlavors.add(DocFlavor.INPUT_STREAM.AUTOSENSE);
			implementedDocFlavors.add(DocFlavor.INPUT_STREAM.PDF);
			implementedDocFlavors.add(DocFlavor.INPUT_STREAM.PNG);
			implementedDocFlavors.add(DocFlavor.INPUT_STREAM.JPEG);
		}
		return implementedDocFlavors;
	}
}
