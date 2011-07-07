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
	private long longInactiveTime;
	private Boolean killed;
	private List<String> services;
	private String computerName;
	private Long lastAccessedTime;
	private Boolean refreshed;
	
	public RemoteClient() {
		this(20000); // 20 seconds default idle time
		lastAccessedTime = new Date().getTime();
	}
	
	public RemoteClient(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
		this.longInactiveTime = 2l * (60l * 60l * 1000l); // Two hours without activities is considered dead
		this.setKilled(false);
		this.setRefreshed(false);
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
	 * @return True or false.
	 */
	public boolean isRemoteDead() {
		long currentTime = new Date().getTime();
		if (Math.abs(currentTime - getLastAccessedTime()) > longInactiveTime) {
			return false;
		} 
		return true;
	}
}
