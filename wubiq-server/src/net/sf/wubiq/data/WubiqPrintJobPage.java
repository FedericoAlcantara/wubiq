/**
 * 
 */
package net.sf.wubiq.data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Federico Alcantara
 *
 */
@Entity
@Table(name = "wubiq_print_job_detail")
public class WubiqPrintJobPage implements Serializable {
	private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;

	private Long printJobId;
	
	private Integer pageIndex;
	
	private byte[] pageData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPrintJobId() {
		return printJobId;
	}

	public void setPrintJobId(Long printJobId) {
		this.printJobId = printJobId;
	}

	/**
	 * @return the pageNumber
	 */
	public Integer getPageIndex() {
		return pageIndex;
	}

	/**
	 * @param pageIndex the pageNumber to set
	 */
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * @return the pageData
	 */
	public byte[] getPageData() {
		return pageData;
	}

	/**
	 * 
	 * @param pageData the pageData to set
	 */
	public void setPageData(byte[] pageData) {
		this.pageData = pageData;
	}
}
