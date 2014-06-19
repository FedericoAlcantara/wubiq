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
		"composite"
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
								jobId(),
								manager(),
								srcColorModel,
								ColorModelRemote.FILTERED_METHODS));
		ColorModelRemote dstColorModelRemote = (ColorModelRemote)
				Enhancer.create(ColorModelRemote.class,
						new ProxyClientSlave(
								jobId(),
								manager(),
								dstColorModel,
								ColorModelRemote.FILTERED_METHODS));
		RenderingHintsRemote hintsRemote = (RenderingHintsRemote)
				Enhancer.create(RenderingHintsRemote.class,
						new ProxyClientSlave(
								jobId(),
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
								jobId(),
								manager(),
								compositeContext,
								CompositeContextRemote.FILTERED_METHODS));
		return remote.objectUUID();
	}
	
	public Composite composite() {
		return (Composite)decoratedObject();
	}
	
	/* *****************************************
	 * IProxy interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#initialize()
	 */
	@Override
	public void initialize(){
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#jobId()
	 */
	@Override
	public Long jobId() {
		return null;
	}
	
	/**
	 * @see net.sf.wubiq.interfaces.IProxy#objectUUID()
	 */
	@Override
	public UUID objectUUID() {
		return null;
	}

	/* *****************************************
	 * IProxyClient interface implementation
	 * *****************************************
	 */
	@Override
	public DirectPrintManager manager() {
		// TODO Auto-generated method stub
		return null;
	}

	/* *****************************************
	 * IProxyMaster interface implementation
	 * *****************************************
	 */
	/**
	 * @see net.sf.wubiq.interfaces.IProxyMaster#decoratedObject()
	 */
	@Override
	public Object decoratedObject() {
		return null;
	}
	

}
