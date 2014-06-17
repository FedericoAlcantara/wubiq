/**
 * 
 */
package net.sf.wubiq.print.services;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.StreamPrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.print.attribute.standard.PrinterState;
import javax.print.event.PrintServiceAttributeListener;

import net.sf.wubiq.common.WebKeys;
import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remote Print Services implementation.
 * Its main purpose is to create a discoverable service on the server side.
 * Shouldn't be use for printing. DocPrintJob is ignored and PrintServiceAttributeListener.
 * It mimics the behavior of a InputStream capable print service.
 * @author Federico Alcantara
 *
 */
public class RemotePrintService extends StreamPrintService {
	private static Log LOG = LogFactory.getLog(RemotePrintService.class);
	private String uuid;
	private String remoteName;
	private String remoteComputerName;
	private List<Class<?>> remoteCategories;
	private Map<String, Object> remoteAttributes;
	private boolean mobile;
	private DocFlavor[] supportedDocFlavors;
	private Map<String, PrintServiceAttribute> attributesPerCategory;
	private Map<String, Object> defaultAttributes;
	private boolean directCommunicationEnabled = true;
	
	public RemotePrintService() {
		super(new ByteArrayOutputStream());
		remoteCategories = new ArrayList<Class<?>>();
		remoteAttributes = new HashMap<String, Object>();
		supportedDocFlavors = new DocFlavor[]{};
		attributesPerCategory = new HashMap<String, PrintServiceAttribute>();
		defaultAttributes = new HashMap<String, Object>();
	}
	
	public RemotePrintService(OutputStream outputStream) {
		super(outputStream);
	}
	
	@SuppressWarnings("unchecked")
	public RemotePrintService(PrintService printService) {
		this();
		remoteName = printService.getName();
		uuid = UUID.randomUUID().toString();
		supportedDocFlavors = printService.getSupportedDocFlavors();
		for (Class<? extends Attribute> category : PrintServiceUtils.getCategories(printService)) {
			remoteCategories.add(category);
		}
		for (Class<?> category : remoteCategories) {
			for (Attribute attribute : PrintServiceUtils.getCategoryAttributes(printService, (Class<? extends Attribute>) category)) {
				remoteAttributes.put(category.getName(), attribute);
			}

			if (category.isAssignableFrom(PrintServiceAttribute.class)) {
				attributesPerCategory.put(category.getName(), 
						printService.getAttribute((Class<? extends PrintServiceAttribute>)category));
			}
			
			defaultAttributes.put(category.getName(), 
					printService.getDefaultAttributeValue((Class<? extends PrintServiceAttribute>)category));
		}
		
		try {
			remoteComputerName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
			throw new UnsupportedOperationException(e);
		}
	}
	
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
		return new RemotePrintJob(this);
	}

	/**
	 * @see javax.print.PrintService#getAttribute(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
		return (T) attributesPerCategory.get(category.getName());
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
		return defaultAttributes.get(category.getName());
	}

	/**
	 * @see javax.print.PrintService#getName()
	 */
	@Override
	public String getName() {
		return getRemoteName() + WebKeys.REMOTE_SERVICE_SEPARATOR + getUuid();
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
		return getRemoteCategories().toArray(new Class<?>[getRemoteCategories().size()]);
	}

	/**
	 * @see javax.print.PrintService#getSupportedAttributeValues(java.lang.Class, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public Object getSupportedAttributeValues(
			Class<? extends Attribute> category, DocFlavor flavor,
			AttributeSet attributes) {
		Object returnValue = getRemoteAttributes().get(category.getName());
		if (returnValue instanceof Attribute[]) {
			Attribute[] attributeArray = ((Attribute[])returnValue);
			Object[] array = (Object[])Array.newInstance(category, attributeArray.length);
			for (int index = 0; index < array.length; index++) {
				Object value = attributeArray[index];
				array[index] = value;
			}
			returnValue = array;
		}
		return returnValue;
	}

	/**
	 * @see javax.print.PrintService#getSupportedDocFlavors()
	 */
	@Override
	public DocFlavor[] getSupportedDocFlavors() {
		return supportedDocFlavors;
	}

	/**
	 * @see javax.print.PrintService#getUnsupportedAttributes(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
	 */
	@Override
	public AttributeSet getUnsupportedAttributes(DocFlavor flavor,
			AttributeSet attributes) {
		return null; // All are supported.
	}

	/**
	 * @see javax.print.PrintService#isAttributeCategorySupported(java.lang.Class)
	 */
	@Override
	public boolean isAttributeCategorySupported(
			Class<? extends Attribute> category) {
		int index = getRemoteCategories().indexOf(category);
		return index > -1;
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
		boolean returnValue = false;
		for (DocFlavor readDocFlavor : supportedDocFlavors) {
			if (readDocFlavor.equals(flavor)) {
				returnValue = true;
				break;
			}
		}

		return returnValue;
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
		PrintServiceAttribute attribute = new PrinterName(getName(), Locale.getDefault());
		attributesPerCategory.put(attribute.getCategory().getName(), attribute);
		attribute = PrinterState.IDLE;
		attributesPerCategory.put(attribute.getCategory().getName(), attribute);
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

	/**
	 * @return the mobile
	 */
	public boolean isMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(boolean mobile) {
		this.mobile = mobile;
	}

	/**
	 * @param supportedDocFlavors the supportedDocFlavors to set
	 */
	public void setSupportedDocFlavors(DocFlavor[] supportedDocFlavors) {
		this.supportedDocFlavors = supportedDocFlavors;
	}

	@Override
	public String getOutputFormat() {
		return "";
	}

	/**
	 * Indicates if this remote print server supports direct communication.
	 * This is a way to determine if the client is apt to deal direct communication.
	 * @return True always as indicator of a feacture implemented in the client.
	 */
	public boolean isDirectCommunicationEnabled() {
		return isMobile() ? false : directCommunicationEnabled;
	}
	
	/**
	 * Sets the direct communication state for this print service.
	 * @param directCommunicationEnabled new direct communication state.
	 */
	public void setDirectCommunicationEnabled(boolean directCommunicationEnabled) {
		this.directCommunicationEnabled = directCommunicationEnabled;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemotePrintService [" + remoteName + WebKeys.REMOTE_SERVICE_SEPARATOR + uuid + "]";
	}

}
