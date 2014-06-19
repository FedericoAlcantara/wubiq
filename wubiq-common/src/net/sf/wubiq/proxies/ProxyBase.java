/**
 * 
 */
package net.sf.wubiq.proxies;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * Defines the minimal implementation for proxied communication for
 * clients, server in their master and slave roles.
 * @author Federico Alcantara
 *
 */
public abstract class ProxyBase implements MethodInterceptor {
	private Long jobId;
	private UUID objectUUID;
	private Set<String> filtered;
	
	public ProxyBase(Long jobId, 
			UUID objectUUID,
			String[] filtered) {
		this.jobId = jobId;
		this.objectUUID = objectUUID;
		this.filtered = new HashSet<String>();
		for (String filter : filtered) {
			this.filtered.add(filter);
		}
		this.filtered.add("toString");
		this.filtered.add("finalize");
		this.filtered.add("dispose");
		this.filtered.add("hashCode");
		this.filtered.add("notify");
		this.filtered.add("notifyAll");
	}
	
	/**
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("jobId".equals(method.getName())) {
			return jobId;
		} else if ("objectUUID".equals(method.getName())) {
			return objectUUID;
		} else if (filtered.contains(method.getName())) {
			return methodProxy.invokeSuper(object, args);
		} else {
			StringBuffer argBuffer = new StringBuffer("");
			for (Object arg : args) {
				if (argBuffer.length() > 0) {
					argBuffer.append(", ");
				}
				argBuffer.append(arg.toString());
			}
			return interception(object, method, args, methodProxy);
		}
	}
	
	/**
	 * This is the actual implementation at proxy level. It has
	 * the same meaning as the MethodInterceptor#intercept
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	public abstract Object interception(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable;
	
	/**
	 * The unique identification of the print job.
	 * @return Job Id.
	 */
	public Long jobId() {
		return jobId;
	}
	
	/**
	 * The unique object identification.
	 * @return The object unique identification.
	 */
	public UUID objectUUID() {
		return objectUUID;
	}
}
