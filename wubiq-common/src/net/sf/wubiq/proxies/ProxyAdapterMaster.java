/**
 * 
 */
package net.sf.wubiq.proxies;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Proxy for handling the communications of a server class in its master role.
 * 
 * @author Federico Alcantara
 *
 */
public class ProxyAdapterMaster extends ProxyAdapter {
	private Object decoratedObject;
	
	public ProxyAdapterMaster(
			IDirectConnectorQueue queue,
			Object decoratedObject,
			String filtered[]) {
		super(queue, UUID.randomUUID(), filtered);
		this.decoratedObject = decoratedObject;
	}
	
	/**
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("decoratedObject".equals(method.getName())) {
			return decoratedObject;
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
