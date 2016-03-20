/**
 * 
 */
package net.sf.wubiq.data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.sf.wubiq.enums.PrintJobDataType;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

import org.hibernate.annotations.Type;

/**
 * @author Federico Alcantara
 *
 */
@Entity
@Table(name = "wubiq_print_job")
public class WubiqPrintJob implements Serializable {
	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long printJobId;
	
	@Column(length = 100)
	private String queueId;
	
	@Column(length = 255)
	private String printServiceName;
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String docAttributes;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String printRequestAttributes;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String printJobAttributes;
	
	@Column(length = 100)
	private String originalDocFlavor;

	@Column(length = 100)
	private String docFlavor;

	private PrintJobDataType printJobDataType;
	
	private Boolean usesDirectConnect;
	
	private Boolean supportsOnlyPageable;
	
	private byte[] printData;
	
	private RemotePrintJobStatus status;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;

	public WubiqPrintJob() {
		
	}
	
	/**
	 * Used for minimal retrieval.
	 * @param printJobId Id of the print job.
	 * @param queueId Queue id.
	 * @param printServiceName Print service name.
	 * @param docAttributes Doc attributes.
	 * @param printRequestAttributes Print request attributes.
	 * @param printJobAttributes Print job attributes
	 * @param docFlavor Doc flavor.
	 * @param usesDirectConnect Uses direct connect.
	 * @param supportsOnlyPageable Supports only pageable.
	 * @param status Status.
	 */
	public WubiqPrintJob(Long printJobId,
			String queueId,
			String printServiceName,
			String docAttributes,
			String printRequestAttributes,
			String printJobAttributes,
			String docFlavor,			
			Boolean usesDirectConnect,
			Boolean supportsOnlyPageable,
			RemotePrintJobStatus status) {
		this.printJobId = printJobId;
		this.queueId = queueId;
		this.printServiceName = printServiceName;
		this.docAttributes = docAttributes;
		this.printRequestAttributes = printRequestAttributes;
		this.printJobAttributes = printJobAttributes;
		this.docFlavor = docFlavor;
		this.usesDirectConnect = usesDirectConnect;
		this.supportsOnlyPageable = supportsOnlyPageable;
		this.status = status;
	}

	public WubiqPrintJob(Long printJobId,
			String queueId,
			byte[] printData) {
		this.printJobId = printJobId;
		this.queueId = queueId;
		this.printData = printData;
	}

	/**
	 * @return the printJobId
	 */
	public Long getPrintJobId() {
		return printJobId;
	}

	/**
	 * @param printJobId the printJobId to set
	 */
	public void setPrintJobId(Long printJobId) {
		this.printJobId = printJobId;
	}

	/**
	 * @return the queueId
	 */
	public String getQueueId() {
		return queueId;
	}

	/**
	 * @param queueId the queueId to set
	 */
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	/**
	 * @return the printServiceName
	 */
	public String getPrintServiceName() {
		return printServiceName;
	}

	/**
	 * @param printServiceName the printServiceName to set
	 */
	public void setPrintServiceName(String printServiceName) {
		this.printServiceName = printServiceName;
	}

	/**
	 * @return the docAttributes
	 */
	public String getDocAttributes() {
		return docAttributes;
	}

	/**
	 * @param docAttributes the docAttributes to set
	 */
	public void setDocAttributes(String docAttributes) {
		this.docAttributes = docAttributes;
	}

	/**
	 * @return the printRequestAttributes
	 */
	public String getPrintRequestAttributes() {
		return printRequestAttributes;
	}

	/**
	 * @param printRequestAttributes the printRequestAttributes to set
	 */
	public void setPrintRequestAttributes(String printRequestAttributes) {
		this.printRequestAttributes = printRequestAttributes;
	}

	/**
	 * @return the printJobAttributes
	 */
	public String getPrintJobAttributes() {
		return printJobAttributes;
	}

	/**
	 * @param printJobAttributes the printJobAttributes to set
	 */
	public void setPrintJobAttributes(String printJobAttributes) {
		this.printJobAttributes = printJobAttributes;
	}

	/**
	 * @return the originalDocFlavor
	 */
	public String getOriginalDocFlavor() {
		return originalDocFlavor;
	}

	/**
	 * @param originalDocFlavor the originalDocFlavor to set
	 */
	public void setOriginalDocFlavor(String originalDocFlavor) {
		this.originalDocFlavor = originalDocFlavor;
	}

	/**
	 * @return the docFlavor
	 */
	public String getDocFlavor() {
		return docFlavor;
	}

	/**
	 * @param docFlavor the docFlavor to set
	 */
	public void setDocFlavor(String docFlavor) {
		this.docFlavor = docFlavor;
	}

	/**
	 * @return the printJobDataType
	 */
	public PrintJobDataType getPrintJobDataType() {
		return printJobDataType;
	}

	/**
	 * @param printJobDataType the printJobDataType to set
	 */
	public void setPrintJobDataType(PrintJobDataType printJobDataType) {
		this.printJobDataType = printJobDataType;
	}

	/**
	 * @return the usesDirectConnect
	 */
	public Boolean getUsesDirectConnect() {
		return usesDirectConnect;
	}

	/**
	 * @param usesDirectConnect the usesDirectConnect to set
	 */
	public void setUsesDirectConnect(Boolean usesDirectConnect) {
		this.usesDirectConnect = usesDirectConnect;
	}

	/**
	 * @return the supportsOnlyPageable
	 */
	public Boolean getSupportsOnlyPageable() {
		return supportsOnlyPageable;
	}

	/**
	 * @param supportsOnlyPageable the supportsOnlyPageable to set
	 */
	public void setSupportsOnlyPageable(Boolean supportsOnlyPageable) {
		this.supportsOnlyPageable = supportsOnlyPageable;
	}

	/**
	 * @return the printData
	 */
	public byte[] getPrintData() {
		return printData;
	}

	/**
	 * @param printData the printData to set
	 */
	public void setPrintData(byte[] printData) {
		this.printData = printData;
	}

	/**
	 * @return the status
	 */
	public RemotePrintJobStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(RemotePrintJobStatus status) {
		this.status = status;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((printJobId == null) ? 0 : printJobId.hashCode());
		result = prime * result + ((queueId == null) ? 0 : queueId.hashCode());
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
		WubiqPrintJob other = (WubiqPrintJob) obj;
		if (printJobId == null) {
			if (other.printJobId != null) {
				return false;
			}
		} else if (!printJobId.equals(other.printJobId)) {
			return false;
		}
		if (queueId == null) {
			if (other.queueId != null) {
				return false;
			}
		} else if (!queueId.equals(other.queueId)) {
			return false;
		}
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WubiqPrintJob [printJobId=" + printJobId + ", queueId="
				+ queueId + ", printServiceName=" + printServiceName
				+ ", time=" + time + "]";
	}
	
}
