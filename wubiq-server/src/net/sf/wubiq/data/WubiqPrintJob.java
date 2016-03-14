/**
 * 
 */
package net.sf.wubiq.data;

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

import net.sf.wubiq.print.jobs.RemotePrintJobStatus;

import org.hibernate.annotations.Type;

/**
 * @author Federico Alcantara
 *
 */
@Entity
@Table(name = "wubiq_print_job")
public class WubiqPrintJob {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private long printJobId;
	
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
	private String docFlavor;

	private byte[] printData;
	
	private RemotePrintJobStatus status;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;

	/**
	 * @return the printJobId
	 */
	public long getPrintJobId() {
		return printJobId;
	}

	/**
	 * @return the queueId
	 */
	public String getQueueId() {
		return queueId;
	}

	/**
	 * @return the printServiceName
	 */
	public String getPrintServiceName() {
		return printServiceName;
	}

	/**
	 * @return the docAttributes
	 */
	public String getDocAttributes() {
		return docAttributes;
	}

	/**
	 * @return the printRequestAttributes
	 */
	public String getPrintRequestAttributes() {
		return printRequestAttributes;
	}

	/**
	 * @return the printJobAttributes
	 */
	public String getPrintJobAttributes() {
		return printJobAttributes;
	}

	/**
	 * @return the docFlavor
	 */
	public String getDocFlavor() {
		return docFlavor;
	}

	/**
	 * @param printServiceName the printServiceName to set
	 */
	public void setPrintServiceName(String printServiceName) {
		this.printServiceName = printServiceName;
	}

	/**
	 * @param docAttributes the docAttributes to set
	 */
	public void setDocAttributes(String docAttributes) {
		this.docAttributes = docAttributes;
	}

	/**
	 * @param printRequestAttributes the printRequestAttributes to set
	 */
	public void setPrintRequestAttributes(String printRequestAttributes) {
		this.printRequestAttributes = printRequestAttributes;
	}

	/**
	 * @param printJobAttributes the printJobAttributes to set
	 */
	public void setPrintJobAttributes(String printJobAttributes) {
		this.printJobAttributes = printJobAttributes;
	}

	/**
	 * @param docFlavor the docFlavor to set
	 */
	public void setDocFlavor(String docFlavor) {
		this.docFlavor = docFlavor;
	}

	/**
	 * @return the printData
	 */
	public byte[] getPrintData() {
		return printData;
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param printJobId the printJobId to set
	 */
	public void setPrintJobId(long printJob) {
		this.printJobId = printJob;
	}

	/**
	 * @param queueId the queueId to set
	 */
	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	/**
	 * @param printData the printData to set
	 */
	public void setPrintData(byte[] data) {
		this.printData = data;
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
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
	}
}
