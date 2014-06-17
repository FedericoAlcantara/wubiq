/**
 * 
 */
package net.sf.wubiq.proxies;

import java.io.Serializable;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.clients.DirectPrintManager;

/**
 * This proxy is located on the remote side, and the server connects 
 * to read the information from the client.
 * @author Federico Alcantara
 *
 */
public class ProxyClientMaster extends ProxyMasterBase {
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
		super(decoratedObject, filtered);
		this.manager = manager;
	}

	/**
	 * @see net.sf.wubiq.proxies.ProxyMasterBase#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("initialize".equals(method.getName())) {
			manager.registerObject(objectUUID(), object);
			return null;
		} else if ("manager".equals(method.getName())) {
			return manager;
		} else if (method.getName().endsWith("Remote")) {
			return methodProxy.invokeSuper(object, args);
		}
		return super.intercept(object, method, args, methodProxy);
	}

	
	@Override
	public Object interception(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		Method decoratedMethod = decoratedObject.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
		Object returnValue = null;
		if (decoratedMethod != null) {
			returnValue = decoratedMethod.invoke(decoratedObject, args);
			if (!void.class.equals(decoratedMethod.getReturnType())
					&& !decoratedMethod.getReturnType().isPrimitive()
					&& !(Serializable.class.isAssignableFrom(decoratedMethod.getReturnType()))) {
				throw new RuntimeException("MUST fix method " + method.getName() + " on " 
							+ object.getClass().getName());
			}
		}
		return returnValue;

	}
	
	
}
