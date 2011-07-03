package net.sf.wubiq.wrappers;

import java.net.ConnectException;

import net.sf.wubiq.clients.LocalPrintManager;

public class ClientManagerTestWrapper extends LocalPrintManager {

	@Override
	public boolean isKilled() {
		return super.isKilled();
	}
	
	@Override
	public void bringAlive() {
		super.bringAlive();
	}
	
	@Override
	public void registerPrintServices() throws ConnectException {
		super.registerPrintServices();
	}	
	
	@Override
	public String[] getPendingJobs() throws ConnectException {
		return super.getPendingJobs();
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
	public String getEncodedUrl(String command, String... parameters) {
		return super.getEncodedUrl(command, parameters);
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
