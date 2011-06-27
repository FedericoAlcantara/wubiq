/**
 * 
 */
package org.wubiq.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * Contains the remote info and status.
 * @author Federico Alcantara
 *
 */
public class RemoteInfo {
	/**
	 * Time in milliseconds where after not having notification from remote the connection is considered dead.
	 */
	private HttpSession session;
	private Integer inactiveTime;
	private Boolean killed;
	private List<String> services;
	private String computerName;
	
	public RemoteInfo() {
		this(20000); // 5 minutes default idle time
	}
	
	public RemoteInfo(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
		this.setKilled(false);
	}
	/**
	 * @param inactiveTime the inactiveTime to set
	 */

	/**
	 * @param session the session to set
	 */
	public void setSession(HttpSession session) {
		session.setMaxInactiveInterval(getInactiveTime());
		this.session = session;
		
	}

	/**
	 * @return the session
	 */
	public HttpSession getSession() {
		return session;
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
	 * Determines if the remote is active and working.
	 * @return True or false.
	 */
	public boolean isRemoteActive() {
		long currentTime = new Date().getTime();
		if (Math.abs(currentTime - getSession().getLastAccessedTime()) > inactiveTime) {
			return false;
		} 
		return true;
	}
}
