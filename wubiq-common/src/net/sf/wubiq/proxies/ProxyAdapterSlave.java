/**
 * 
 */
package net.sf.wubiq.proxies;

import java.lang.reflect.Method;
import java.util.UUID;

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
public class ProxyAdapterSlave extends ProxyAdapter {

	public ProxyAdapterSlave(IDirectConnectorQueue queue,
			UUID objectUUID,
			String filtered[]) {
		super(queue, objectUUID, filtered);
	}
	
	@Override
	public Object interception(Object object, Method method, Object[] args,
			MethodProxy methodProxy) throws Throwable {
		queue().sendCommand(new RemoteCommand(objectUUID(), method.getName(), 
				DirectConnectUtils.INSTANCE.convertToGraphicParameters(method, args)));
		Object returnValue = queue().returnData();
		return returnValue;
	}
}
