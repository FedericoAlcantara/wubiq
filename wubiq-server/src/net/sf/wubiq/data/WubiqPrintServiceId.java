/**
 * 
 */
package net.sf.wubiq.data;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * @author Federico Alcantara
 *
 */
public class WubiqPrintServiceId implements Serializable{
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String uuid;

	@Column(length = 100)
	private String name;

	public WubiqPrintServiceId() {
	}
	
	public WubiqPrintServiceId(String uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
		WubiqPrintServiceId other = (WubiqPrintServiceId) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
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
		return "WubiqPrintServiceId [uuid=" + uuid + ", name="
				+ name + "]";
	}
}
