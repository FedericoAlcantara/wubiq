/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.util.UUID;

import net.sf.cglib.proxy.Enhancer;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;
import net.sf.wubiq.proxies.ProxyClientMaster;
import net.sf.wubiq.proxies.ProxyClientSlave;

/**
 * @author Federico Alcantara
 *
 */
public class CompositeRemote implements Composite, IProxyClient, IProxyMaster {

	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	public CompositeRemote() {
		initialize();
	}
	
	@Override
	public CompositeContext createContext(ColorModel srcColorModel,
			ColorModel dstColorModel, RenderingHints hints) {
		return null;
	}

	public UUID createContextRemote(UUID srcColorModel, UUID dstColorModel, UUID hints) {
		ColorModelRemote srcColorModelRemote = (ColorModelRemote)
				Enhancer.create(ColorModelRemote.class,
						new ProxyClientSlave(
								manager(),
								srcColorModel,
								ColorModelRemote.FILTERED_METHODS));
		ColorModelRemote dstColorModelRemote = (ColorModelRemote)
				Enhancer.create(ColorModelRemote.class,
						new ProxyClientSlave(
								manager(),
								dstColorModel,
								ColorModelRemote.FILTERED_METHODS));
		RenderingHintsRemote hintsRemote = (RenderingHintsRemote)
				Enhancer.create(RenderingHintsRemote.class,
						new ProxyClientSlave(
								manager(),
								hints,
								RenderingHintsRemote.FILTERED_METHODS));
		CompositeContext compositeContext = composite().createContext(
				srcColorModelRemote,
				dstColorModelRemote,
				hintsRemote);
		
		CompositeContextRemote remote = (CompositeContextRemote)
				Enhancer.create(CompositeContextRemote.class,
						new ProxyClientMaster(
								manager(),
								compositeContext,
								CompositeContextRemote.FILTERED_METHODS));
		return remote.objectUUID();
	}
	
	public Composite composite() {
		return (Composite)decoratedObject();
	}
	
	/* ***************************
	 * Proxied methods
	 * ***************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyClient#manager()
	 */
	public DirectPrintManager manager() {
		return null;
	}
		
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	public void initialize(){
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	public Object decoratedObject() {
		return null;
	}

	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	public UUID objectUUID() {
		return null;
	}

}
