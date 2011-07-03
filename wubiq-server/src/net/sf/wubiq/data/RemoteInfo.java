/**
 * 
 */
package net.sf.wubiq.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains the remote info and status.
 * @author Federico Alcantara
 *
 */
public class RemoteInfo {
	/**
	 * Time in milliseconds where after not having notification from remote the connection is considered dead.
	 */
	private Integer inactiveTime;
	private Boolean killed;
	private List<String> services;
	private String computerName;
	private Long lastAccessedTime;
	
	public RemoteInfo() {
		this(20000); // 20 seconds default idle time
		lastAccessedTime = new Date().getTime();
	}
	
	public RemoteInfo(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
		this.setKilled(false);
	}
	
	/**
	 * @return the inactiveTime
	 */
	public Integer getInactiveTime() {
		return inactiveTime;
	}

	/**
	 * @param inactiveTime the inactiveTime to set
	 */
	public void setInactiveTime(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
	}

	/**
	 * @param killed the killed to set
	 */
	public void setKilled(Boolean killed) {
		this.killed = killed;
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
}
