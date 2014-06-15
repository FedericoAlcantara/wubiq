/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;
import net.sf.wubiq.utils.DirectConnectUtils;

/**
 * This proxy is located on the remote side, and connects to the server
 * to read the information from it.
 * @author Federico Alcantara
 *
 */
public class ProxyRemoteSlave implements MethodInterceptor {
	private DirectPrintManager manager;
	private UUID objectUUID;
	private List<String> filtered;
	
	public ProxyRemoteSlave(DirectPrintManager manager,
			UUID objectUUID,
			String[] filtered) {
		this.manager = manager;
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
			manager.registerObject(objectUUID, object);
			DirectConnectUtils.INSTANCE.setField(object, "manager", manager);
			DirectConnectUtils.INSTANCE.setField(object, "objectUUID", objectUUID);
			return methodProxy.invokeSuper(object, args);
		} else if (filtered.contains(method.getName())) {
			return methodProxy.invokeSuper(object, args);
		}
		return manager.readFromRemote(
				new RemoteCommand(objectUUID, method.getName(), args));
	}
}
