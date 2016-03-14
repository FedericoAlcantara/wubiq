/**
 * 
 */
package net.sf.wubiq.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Contains the remote info and status. Each connected client is represented by an instance of this class.
 * @author Federico Alcantara
 *
 */
@Entity
@Table(name = "wubiq_remote_client")
public class RemoteClient implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length = 100)
	private String uniqueId;
	
	@Column(length = 100)
	private String computerName;
	
	
	/**
	 * @deprecated Services are handled by RemotePrintServiceLookup
	 */
	private transient List<String> services;
	
	@Column(length = 20)
	private String clientVersion;

	private Boolean refreshed;
	
	private Boolean paused;
	
	private Boolean killed;
	
	/**
	 * @deprecated Use lastAccessed
	 */
	private transient Long lastAccessedTime;

	/**
	 * Time in milliseconds where after not having notification from remote the connection is considered dead.
	 */
	private transient long inactiveTime;

	/**
	 * @deprecated Connection failures proves to be a useless statistics is no longer maintained.
	 */
	private transient long connectionFailures;
	
	public RemoteClient() {
		this(20000); // 20 seconds default idle time
	}
	
	private RemoteClient(Integer inactiveTime) {
		this.inactiveTime = inactiveTime;
		this.setKilled(false);
		this.setRefreshed(false);
		this.setPaused(false);
	}
	
	/**
	 * @return the uUID
	 */
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId the uUID to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
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
	}

	/**
	 * @return the killed
	 */
	public Boolean isKilled() {
		return killed;
	}

	/**
	 * @deprecated Services are not registered here. They are registered at RemotePrintServiceLookup.
	 * @param services the services to set
	 */
	public void setServices(List<String> services) {
		this.services = services;
	}

	/**
	 * @deprecated No use. Services are handled by RemotePrintServiceLookup
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
	 * @deprecated Use setLastAccessed instead.
	 * @param lastAccessedTime the lastAccessedTime to set
	 */
	public void setLastAccessedTime(Long lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}
	
	/**
	 * @deprecated Use getLastAccessed
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
	 * @deprecated Control of its active state is transferred to the Remote Client Manager.
	 * Determines if the remote is active and working.
	 * @return Always returns true.
	 */
	public boolean isRemoteActive() {
		return true;
	}
	
	/**
	 * Determines if the remote is dead for a long time.
	 * @deprecated No longer used for determining the state of the client. 
	 * Clients are ALWAYS considered connected, to definitively kill a client
	 * a 'kill' command must be issued either by the client or by the server.
	 * The state is handled by the RemoteClientManager.
	 * @return Always returns true, meaning that the client is not dead.
	 */
	public boolean isRemoteDead() {
		return true;
	}

	/**
	 * @return
	 */
	public Boolean isPaused() {
		return paused;
	}

	/**
	 * It is recommended to pause the remote client through the RemoteClientManager provided interface.
	 * @param paused
	 */
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
	 * @deprecated Connection failure is no longer maintained. This method will always return 0.
	 * The number of times a client connection had failed.
	 * When a client connection is dropped more than 20 seconds
	 * it is a considered a connection failure and added up.
	 * @return The number of times the connection failed.
	 */
	public long getConnectionFailures() {
		return connectionFailures;
	}
}
