/**
 * 
 */
package net.sf.wubiq.proxies;

import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.enums.RemoteCommand;
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
		queue().sendCommand(new RemoteCommand(objectUUID(), method.getName(), args));
		Object returnValue = queue().returnData();
		return returnValue;
	}

}
