/**
 * 
 */
package org.wubiq.print.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.event.PrintServiceAttributeListener;

/**
 * Remote Print Services implementation.
 * Its main purpose is to create a discoverable service on the server side.
 * Shouldn't be use for printing. DocPrintJob is ignored and PrintServiceAttributeListener.
 * It mimics the behavior of a PDF capable printer.
 * @author Federico Alcantara
 *
 */
public class RemotePrintService implements PrintService {
	private String uuid;
	private String remoteName;
	private String remoteComputerName;
	private List<Class<?>> remoteCategories;
	private Map<String, Object> remoteAttributes;
	
	/**
	 * @see javax.print.PrintService#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
	 */
	@Override
	public void addPrintServiceAttributeListener (
			PrintServiceAttributeListener listener) {
		// It is ignored.
	}

	/**
	 * @see javax.print.PrintService#createPrintJob()
	 */
	@Override
	public DocPrintJob createPrintJob() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.PrintService#getAttribute(java.lang.Class)
	 */
	@Override
	public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.print.PrintService#getAttributes()
	 */
	@Override
	public PrintServiceAttributeSet getAttributes() {
		PrintServiceAttributeSet returnValue = new HashPrintServiceAttributeSet();
		for (Class<?> category : getRemoteCategories()) {
			if (category.isAssignableFrom(PrintServiceAttribute.class)){
				Object attributeObject = getRemoteAttributes().get(category.getName());
				if (attributeObject instanceof Object[]) {
					for (Object object : ((Object[])attributeObject)) {
						returnValue.add((Attribute)object);
					}
				}
			}
		}
		return returnValue;
	}

	/**
	 * @see javax.print.PrintService#getDefaultAttributeValue(java.lang.Class)
	 */
	@Override
	public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
		return null;
	}

	/**
	 * @see javax.print.PrintService#getName()
	 */
	@Override
	public String getName() {
		return getRemoteName() + " (R) " + getRemoteComputerName();
	}

	/**
	 * @see javax.print.PrintService#getServiceUIFactory()
	 */
	@Override
	public ServiceUIFactory getServiceUIFactory() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see javax.print.PrintService#getSupportedAttributeCategories()
	 */
	@Override
	public Class<?>[] getSupportedAttributeCategories() {
		return getRemoteCategories().toArray(new Class<?>[0]);
	}

	/**
	 * @see javax.print.PrintService#getSupportedAttributeValues(java.lang.Class, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public Object getSupportedAttributeValues(
			Class<? extends Attribute> category, DocFlavor flavor,
			AttributeSet attributes) {
		return getRemoteAttributes().get(category.getName());
	}

	/**
	 * @see javax.print.PrintService#getSupportedDocFlavors()
	 */
	@Override
	public DocFlavor[] getSupportedDocFlavors() {
		return new DocFlavor[]{DocFlavor.INPUT_STREAM.PDF};
	}

	/**
	 * @see javax.print.PrintService#getUnsupportedAttributes(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public AttributeSet getUnsupportedAttributes(DocFlavor flavor,
			AttributeSet attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see javax.print.PrintService#isAttributeCategorySupported(java.lang.Class)
	 */
	@Override
	public boolean isAttributeCategorySupported(
			Class<? extends Attribute> category) {
		return getRemoteCategories().indexOf(category.getClass()) > -1;
	}

	/**
	 * @see javax.print.PrintService#isAttributeValueSupported(javax.print.attribute.Attribute, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public boolean isAttributeValueSupported(Attribute attrval,
			DocFlavor flavor, AttributeSet attributes) {
		return true;
	}

	/**
	 * @see javax.print.PrintService#isDocFlavorSupported(javax.print.DocFlavor)
	 */
	@Override
	public boolean isDocFlavorSupported(DocFlavor flavor) {
		return flavor.equals(DocFlavor.INPUT_STREAM.PDF);
	}

	/**
	 * @see javax.print.PrintService#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
	 */
	@Override
	public void removePrintServiceAttributeListener(
			PrintServiceAttributeListener listener) {
		// Ignored
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets a new list of remoteCategories for the print service.
	 * @param remoteCategories
	 */
	public void setRemoteCategories(List<Class<?>> remoteCategories) {
		this.remoteCategories = remoteCategories;
	}
	
	/**
	 * Return list of registered remoteCategories.
	 * @return List of remoteCategories.
	 */
	public List<Class <?>> getRemoteCategories() {
		if (remoteCategories == null) {
			remoteCategories = new ArrayList<Class<?>>();
		}
		return remoteCategories;
	}

	/**
	 * @param remoteAttributes the remoteAttributes to set
	 */
	public void setRemoteAttributes(Map<String, Object> remoteAttributes) {
		this.remoteAttributes = remoteAttributes;
	}

	/**
	 * @return the remoteAttributes
	 */
	public Map<String, Object> getRemoteAttributes() {
		if (remoteAttributes == null) {
			remoteAttributes = new HashMap<String, Object>();
		}
		return remoteAttributes;
	}

	/**
	 * @param remoteName the remoteName to set
	 */
	public void setRemoteName(String remoteName) {
		this.remoteName = remoteName;
	}

	/**
	 * @return the remoteName
	 */
	public String getRemoteName() {
		return remoteName;
	}

	/**
	 * @param remoteComputerName the remoteComputerName to set
	 */
	public void setRemoteComputerName(String remoteComputerName) {
		this.remoteComputerName = remoteComputerName;
	}

	/**
	 * @return the remoteComputerName
	 */
	public String getRemoteComputerName() {
		return remoteComputerName;
	}

}
