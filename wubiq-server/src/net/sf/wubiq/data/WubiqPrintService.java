/**
 * 
 */
package net.sf.wubiq.data;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Print service persistence. This is just a marker to allow the automatic creation.
 * It is not use in JPA. Instead is managed by common using JDBC.
 * @author Federico Alcantara
 *
 */
@Entity
@Table(name = "wubiq_print_service")
@IdClass(WubiqPrintServiceId.class)
public class WubiqPrintService implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(length = 100)
	private String uuid;

	@Id
	@Column(length = 100)
	private String name;

	private Boolean mobile;
	
	private byte[] service;

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the mobile
	 */
	public Boolean getMobile() {
		return mobile;
	}

	/**
	 * @param mobile the mobile to set
	 */
	public void setMobile(Boolean mobile) {
		this.mobile = mobile;
	}

	/**
	 * @return the service
	 */
	public byte[] getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(byte[] service) {
		this.service = service;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((name == null) ? 0 : name.hashCode());
		result = prime * result + Arrays.hashCode(service);
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WubiqPrintService other = (WubiqPrintService) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (!Arrays.equals(service, other.service)) {
			return false;
		}
		if (uuid == null) {
			if (other.uuid != null) {
				return false;
			}
		} else if (!uuid.equals(other.uuid)) {
			return false;
		}
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WubiqPrintService [uuid=" + uuid + ", name=" + name
				+ ", service=" + Arrays.toString(service) + "]";
	}
	
	
}
