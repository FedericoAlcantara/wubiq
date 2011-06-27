package org.wubiq.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wubiq.common.AttributeInputStream;
import org.wubiq.common.AttributeOutputStream;

/**
 * Utilities for gathering print service information.
 * @author Federico Alcantara
 *
 */
public class PrintServiceUtils {
	private static final Log LOG = LogFactory.getLog(PrintServiceUtils.class);
	
	/**
	 * Returns an array of printers supporting PDF flavor
	 * @return
	 */
	public static PrintService[] getPrintServices() {
		DocFlavor pdfFlavor = DocFlavor.INPUT_STREAM.PDF;
		return PrintServiceLookup.lookupPrintServices(pdfFlavor, null);
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
				Attribute[] attributes = (Attribute[]) printService.getSupportedAttributeValues(Media.class, DocFlavor.INPUT_STREAM.PDF, null);
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
				Object attributeObject = printService.getSupportedAttributeValues(category, DocFlavor.INPUT_STREAM.PDF, null);
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
		for (Attribute attribute: attributes) {
			if (attribute instanceof DocAttribute) {
				returnValue.add(attribute);
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
		for (Attribute attribute: attributes) {
			if (attribute instanceof PrintRequestAttribute) {
				returnValue.add(attribute);
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
		return returnValue;
	}


}
