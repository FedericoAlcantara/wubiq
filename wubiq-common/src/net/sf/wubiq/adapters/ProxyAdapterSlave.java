/**
 * 
 */
package net.sf.wubiq.adapters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;
import net.sf.wubiq.utils.DirectConnectUtils;

/**
 * Intercepts the objects' methods to ensure proper communication with
 * remote parties.
 * 
 * @author Federico Alcantara
 *
 */
public class ProxyAdapterSlave implements MethodInterceptor {
	private IDirectConnectorQueue queue;
	private UUID objectUUID;
	private List<String> filtered;

	public ProxyAdapterSlave(IDirectConnectorQueue queue,
			UUID objectUUID,
			String filtered[]) {
		this.queue = queue;
		this.objectUUID = objectUUID;
		this.filtered = Arrays.asList(filtered);
		
	}
	
	/**
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("initialize".equals(method.getName())) {
			queue.registerObject(objectUUID, object);
			DirectConnectUtils.INSTANCE.setField(object, "manager", queue);
			DirectConnectUtils.INSTANCE.setField(object, "objectUUID", objectUUID);
			return methodProxy.invokeSuper(object, args);
		} else if (filtered.contains(method.getName())) {
			return methodProxy.invokeSuper(object, args);
		}
		queue.sendCommand(new RemoteCommand(objectUUID, method.getName(), args));
		Object returnValue = queue.returnData();
		return returnValue;
	}

}
