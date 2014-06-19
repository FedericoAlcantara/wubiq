/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.UUID;

import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IProxyClient;
import net.sf.wubiq.interfaces.IProxyMaster;

/**
 * Graphics Adapter for communicating with remote.
 * @author Federico Alcantara
 *
 */
public class CompositeContextRemote implements CompositeContext, 
		IProxyClient, IProxyMaster {

	public static final String[] FILTERED_METHODS = new String[]{
	};
	
	public CompositeContextRemote() {
		initialize();
	}
	
	@Override
	public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
	}
	
	public void composeRemote(UUID src, UUID dstIn, UUID dstOut) {
		
	}

	@Override
	public void dispose() {
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
