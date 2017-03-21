/**
 * 
 */
package net.sf.wubiq.dao;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.data.WubiqPrintJobPage;
import net.sf.wubiq.persistence.PersistenceManager;
import net.sf.wubiq.print.JpaPrintable;
import net.sf.wubiq.wrappers.GraphicCommand;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Federico Alcantara
 *
 */
public enum WubiqPrintJobPageDao {
	INSTANCE;
	private final Log LOG = LogFactory.getLog(WubiqPrintJobPageDao.class);
	
	/**
	 * Saves the print job page.
	 * @param job Associated job id.
	 * @param pageFormat Page format.
	 * @param pageIndex Index of the page.
	 * @param commands command.
	 * @return Instance of a print job page.
	 */
	public WubiqPrintJobPage save(WubiqPrintJob job, PageFormat pageFormat, int pageIndex, Set<GraphicCommand> commands) {
		WubiqPrintJobPage detail = new WubiqPrintJobPage();
		detail.setPrintJobId(job.getPrintJobId());
		detail.setPageIndex(pageIndex);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream stream;
		try {
			stream = new ObjectOutputStream(out);
			stream.writeObject(pageFormat);
			stream.writeObject(commands);
			stream.close();
			out.close();
			detail.setPageData(out.toByteArray());
		} catch (IOException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
		}
		try {
			PersistenceManager.em().persist(detail);
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return detail;
	}
	
	/**
	 * Finds a print job page.
	 * @param id Id of the print job.
	 * @return Print job page.
	 */
	private WubiqPrintJobPage find(Long id) {
		WubiqPrintJobPage returnValue = null;
		try {
			returnValue = PersistenceManager.em().find(WubiqPrintJobPage.class, id);
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * Finds a pageable page.
	 * @param id Id of the page.
	 * @return Page found.
	 */
	@SuppressWarnings("unchecked")
	public Printable findPrintable(Long id) {
		JpaPrintable returnValue = null;
		try {
			WubiqPrintJobPage page = find(id);
			if (page != null) {
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(page.getPageData()));
				PageFormat pageFormat = (PageFormat)in.readObject();
				Set<GraphicCommand> commands = (Set<GraphicCommand>)in.readObject();
				returnValue = new JpaPrintable(pageFormat, commands);
				in.close();
			}
			PersistenceManager.commit();
		} catch (Exception e) {
			PersistenceManager.rollback();
			throw new RuntimeException(e);
		}
		return returnValue;
	}
}
