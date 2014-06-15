/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.interfaces.IRemoteClientMaster;
import net.sf.wubiq.utils.DirectConnectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This proxy is located on the remote side, and the server connects 
 * to read the information from the client.
 * @author Federico Alcantara
 *
 */
public class ProxyRemoteMaster implements MethodInterceptor {
	private static final Log LOG = LogFactory.getLog(ProxyRemoteMaster.class);
	private DirectPrintManager manager;
	private UUID objectUUID;
	private List<String> filtered;

	public ProxyRemoteMaster(DirectPrintManager manager,
			String[] filtered) {
		this.manager = manager;
		this.objectUUID = UUID.randomUUID();
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
		} else if (method.getName().endsWith("remote") ||
				filtered.contains(method.getName())) {
			return methodProxy.invokeSuper(object, args);
		}
		Object decoratedObject = ((IRemoteClientMaster)object).decoratedObject();
		Object returnValue = methodProxy.invokeSuper(decoratedObject, args);
		if (!(returnValue instanceof Serializable)) {
			LOG.info("MUST fix method " + method.getName() + " on " 
						+ object.getClass().getName());
			return null;
		}
		return returnValue;
	}
	
	
}
