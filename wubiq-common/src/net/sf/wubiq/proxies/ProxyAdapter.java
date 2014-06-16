/**
 * 
 */
package net.sf.wubiq.proxies;

import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.interfaces.IRemoteListener;
import net.sf.wubiq.print.managers.IDirectConnectorQueue;

/**
 * Proxy handler for server side classes.
 * @author Federico Alcantara
 *
 */
public abstract class ProxyAdapter extends ProxyBase {
	private IDirectConnectorQueue queue;
	
	public ProxyAdapter(IDirectConnectorQueue queue,
			UUID objectUUID, String[] filtered) {
		super(objectUUID, filtered);
		this.queue = queue;
	}

	/**
	 * @see net.sf.wubiq.proxies.ProxyBase#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("queue".equals(method.getName())) {
			return queue();
		} else if ("addListener".equals(method.getName())) {
			queue().addListener((IRemoteListener)args[0]);
			return null;
		} else if ("removeListener".equals(method.getName())) {
			return queue().removeListener((IRemoteListener)args[0]);
		} else if ("listeners".equals(method.getName())) {
			return queue().listeners();
		}
		return super.intercept(object, method, args, methodProxy);
	}

	/**
	 * The queue instance saved when creating the proxy handler.
	 * @return Queue instance.
	 */
	public IDirectConnectorQueue queue() {
		return queue;
	}
}
