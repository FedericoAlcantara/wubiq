/**
 * 
 */
package net.sf.wubiq.proxies;

import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodProxy;

/**
 * The minimal implementation of a master communication proxy
 * for clients and servers in their master role.
 * @author Federico Alcantara
 *
 */
public abstract class ProxyMasterBase extends ProxyBase {
	Object decoratedObject;
	
	public ProxyMasterBase(Object decoratedObject,
			String[] filtered) {
		super(UUID.randomUUID(), filtered);
		this.decoratedObject = decoratedObject;
	}
	
	/**
	 * @see net.sf.wubiq.proxies.ProxyBase#interception(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		
		if ("decoratedObject".equals(method.getName())) {
			return decoratedObject;
		} else if (method.getName().endsWith("Remote")) {
			return method.invoke(object, args);
		} else {
			return super.intercept(object, method, args, methodProxy);
		}
	}

}
