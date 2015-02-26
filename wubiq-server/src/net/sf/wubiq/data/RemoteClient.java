/**
 * 
 */
package net.sf.wubiq.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains the remote info and status. Each connected client is represented by an instance of this class.
 * @author Federico Alcantara
 *
 */
public class RemoteClient {
	/**
	 * Time in milliseconds where after not having notification from remote the connection is considered dead.
	 */
	private long inactiveTime;
	private Boolean killed;
	private List<String> services;
	private String computerName;
	private Long lastAccessedTime;
	private Boolean refreshed;
	private Boolean paused;
	private String clientVersion;
	private long connectionFailures;
	
	public RemoteClient() {
		this(20000); // 20 seconds default idle time
		lastAccessedTime = new Date().getTime();
	}
	
	public RemoteClient(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
		this.setKilled(false);
		this.setRefreshed(false);
		this.setPaused(false);
		this.connectionFailures = 1;
	}
	
	/**
	 * @return the inactiveTime
	 */
	public Long getInactiveTime() {
		return inactiveTime;
	}

	/**
	 * @param inactiveTime the inactiveTime to set
	 */
	public void setInactiveTime(Long inactiveTime) {
		this.inactiveTime = inactiveTime;
	}

	/**
	 * @param killed the killed to set
	 */
	public void setKilled(Boolean killed) {
		this.killed = killed;
		if (killed) {
			if (lastAccessedTime > inactiveTime) {
				lastAccessedTime = lastAccessedTime - inactiveTime;
			}
			connectionFailures = 1l;
		}
	}

	/**
	 * @return the killed
	 */
	public Boolean isKilled() {
		return killed;
	}

	/**
	 * @param services the services to set
	 */
	public void setServices(List<String> services) {
		this.services = services;
	}

	/**
	 * @return the services
	 */
	public List<String> getServices() {
		if (services == null) {
			services = new ArrayList<String>();
		}
		return services;
	}

	/**
	 * @param computerName the computerName to set
	 */
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	/**
	 * @return the computerName
	 */
	public String getComputerName() {
		if (computerName == null) {
			computerName = "";
		}
		return computerName;
	}

	/**
	 * @param lastAccessedTime the lastAccessedTime to set
	 */
	public void setLastAccessedTime(Long lastAccessedTime) {
		long currentTime = new Date().getTime();
		if ((currentTime - lastAccessedTime) > inactiveTime) {
			connectionFailures++;
		}
		this.lastAccessedTime = lastAccessedTime;
	}

	/**
	 * @return the lastAccessedTime
	 */
	public Long getLastAccessedTime() {
		return lastAccessedTime;
	}

	/**
	 * @param refreshed the refreshed to set
	 */
	public void setRefreshed(Boolean refreshed) {
		this.refreshed = refreshed;
	}

	/**
	 * @return the refreshed
	 */
	public Boolean isRefreshed() {
		return refreshed;
	}

	/**
	 * Determines if the remote is active and working.
	 * @return True or false.
	 */
	public boolean isRemoteActive() {
		long currentTime = new Date().getTime();
		if (Math.abs(currentTime - getLastAccessedTime()) > inactiveTime) {
			return false;
		} 
		return true;
	}
	
	/**
	 * Determines if the remote is dead for a long time.
	 * @deprecated No longer used for determining the state of the client. 
	 * Clients are ALWAYS considered connected, to definitively kill a client
	 * a 'kill' command must be issued either by the client or by the server.
	 * @return Always returns true, meaning that the client is not dead.
	 */
	public boolean isRemoteDead() {
		return true;
	}

	public Boolean isPaused() {
		return paused;
	}

	public void setPaused(Boolean paused) {
		this.paused = paused;
	}

	/**
	 * @return the clientVersion
	 */
	public String getClientVersion() {
		return clientVersion;
	}

	/**
	 * @param clientVersion the clientVersion to set
	 */
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}
	
	/**
	 * The number of times a client connection had failed.
	 * When a client connection is dropped more than 20 seconds
	 * it is a considered a connection failure and added up.
	 * @return The number of times the connection failed.
	 */
	public long getConnectionFailures() {
		return connectionFailures;
	}
}
