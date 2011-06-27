package org.wubiq.fortests;

import java.net.ConnectException;

import org.wubiq.clients.LocalPrintManager;

import com.gargoylesoftware.htmlunit.WebClient;

public class ClientManagerTestWrapper extends LocalPrintManager {

	@Override
	public boolean canConnect() {
		return super.canConnect();
	}
	
	@Override
	public void registerPrintServices() throws ConnectException {
		super.registerPrintServices();
	}	
	
	@Override
	public void killManager() {
		super.killManager();
	}
	
	@Override
	public String askServer(String command, String... parameters)
			throws ConnectException {
		return super.askServer(command, parameters);
	}

	@Override
	public Object pollServer(String command, String... parameters)
			throws ConnectException {
		return super.pollServer(command, parameters);
	}
	
	@Override
	public void setClient(WebClient client) {
		super.setClient(client);
	}
	
	@Override
	public WebClient getClient() {
		// TODO Auto-generated method stub
		return super.getClient();
	}
	
	@Override
	public Object getPage() {
		// TODO Auto-generated method stub
		return super.getPage();
	}

	@Override
	public void setHost(String host) {
		super.setHost(host);
	}
	
	@Override
	public void setPort(String port) {
		super.setPort(port);
	}
	
	@Override
	public void setApplicationName(String applicationName) {
		super.setApplicationName(applicationName);
	}

	@Override
	public void setServletName(String servletName) {
		super.setServletName(servletName);
	}
	
	@Override
	public void setUuid(String uuid) {
		super.setUuid(uuid);
	}
}
