/**
 * 
 */
package net.sf.wubiq.clients.remotes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.wubiq.clients.DirectPrintManager;
import net.sf.wubiq.enums.RemoteCommand;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This proxy is located on the remote side, and connects to the server
 * to read the information from it.
 * @author Federico Alcantara
 *
 */
public class ProxyRemoteSlave implements MethodInterceptor {
	private static final Log LOG = LogFactory.getLog(ProxyRemoteSlave.class);
	private DirectPrintManager manager;
	private UUID objectUUID;
	
	public ProxyRemoteSlave(DirectPrintManager manager,
			UUID objectUUID,
			String[] filtered) {
		this.manager = manager;
		this.objectUUID = objectUUID;
	}
	
	/**
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		if ("initialize".equals(method.getName())) {
			manager.registerObject(objectUUID, object);
			setField(object, "manager", manager);
			setField(object, "objectUUID", objectUUID);
			return methodProxy.invoke(object, args);
		}
		return manager.readFromRemote(
				new RemoteCommand(objectUUID, method.getName(), args));
	}

	/**
	 * Sets the field value.
	 * @param object Object containing the field.
	 * @param fieldName Name of the field to set.
	 * @param value Value to set in the field.
	 */
	private void setField(Object object, String fieldName, Object value) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(object, value);
		} catch (Exception e) {
			LOG.info(object.getClass().getName() + " must define a field 'private " +
					value.getClass().getSimpleName() + " " + fieldName);
		}

	}
}
