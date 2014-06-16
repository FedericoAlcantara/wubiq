/**
 * 
 */
package net.sf.wubiq.proxies;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.clients.DirectPrintManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This proxy is located on the remote side, and the server connects 
 * to read the information from the client.
 * @author Federico Alcantara
 *
 */
public class ProxyClientMaster extends ProxyMasterBase {
	private static final Log LOG = LogFactory.getLog(ProxyClientMaster.class);
	private DirectPrintManager manager;
	
	/**
	 * Define the proxy handler for a client in its master role.
	 * @param manager Direct print manager.
	 * @param decoratedObject Object to be decorated.
	 * @param filtered List of method not handled by this proxy.
	 */
	public ProxyClientMaster(DirectPrintManager manager,
			Object decoratedObject,
			String[] filtered) {
		super(UUID.randomUUID(), filtered);
		this.manager = manager;
	}

	/**
	 * @see net.sf.wubiq.proxies.ProxyMasterBase#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("manager".equals(method.getName())) {
			return manager;
		}
		return super.intercept(object, method, args, methodProxy);
	}

	
	@Override
	public Object interception(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		Object returnValue = methodProxy.invokeSuper(decoratedObject, args);
		if (!(returnValue instanceof Serializable)) {
			LOG.info("MUST fix method " + method.getName() + " on " 
						+ object.getClass().getName());
			return null;
		}
		return returnValue;

	}
	
	
}
