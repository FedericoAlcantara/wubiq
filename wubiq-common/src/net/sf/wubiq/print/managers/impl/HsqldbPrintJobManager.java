/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import net.sf.wubiq.print.jobs.RemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.managers.IRemotePrintJobManager;
import net.sf.wubiq.utils.Is;
import net.sf.wubiq.utils.PrintServiceUtils;
import net.sf.wubiq.utils.ServerProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hsqldb based print job manager. This is the default.
 * @author Federico Alcantara
 *
 */
public class HsqldbPrintJobManager implements IRemotePrintJobManager {
	private static Log LOG = LogFactory.getLog(HsqldbPrintJobManager.class);
	private final String TABLE_NAME = "PRINT_JOB";
	private final String JOB_ID_FIELD_NAME = "ID";
	private final String QUEUE_ID_FIELD_NAME = "QUEUE_ID";
	private final String SELECT_JOB = "select * from " + TABLE_NAME + " where " + JOB_ID_FIELD_NAME + " = ?";
	private final String DELETE_JOB = "delete from " + TABLE_NAME + " where " + JOB_ID_FIELD_NAME + " = ?";
	private final String LOOK_UP_PENDING = "select " + JOB_ID_FIELD_NAME + " from " + TABLE_NAME + " where " + QUEUE_ID_FIELD_NAME + " = ? order by "
			 + JOB_ID_FIELD_NAME;
	
	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.prepareStatement("drop table PRINT_JOB").executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			LOG.debug(e.getMessage());
		} finally {
			close(connection);
		}
		try {
			connection = getConnection();
			connection.prepareStatement("create table PRINT_JOB (" +
					JOB_ID_FIELD_NAME + " integer not null, " +
					QUEUE_ID_FIELD_NAME + " varchar(32) default ' ' not null, " +
					"PRINT_SERVICE_NAME varchar(255) not null," +
					"DOC_ATTRIBUTES varchar(" + Integer.MAX_VALUE + "), " +
					"PRINT_REQUEST_ATTRIBUTES varchar(" + Integer.MAX_VALUE + "), " +
					"DOC_FLAVOR varchar(20), " +
					"STATUS integer not null, " +
					"PRINT_DATA binary(" + Integer.MAX_VALUE + ")," +
					"primary key (ID))").executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			LOG.debug(e.getMessage());
		} finally {
			close(connection);
		}
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#addRemotePrintJob(java.lang.String, net.sf.wubiq.print.jobs.IRemotePrintJob)
	 */
	@Override
	public long addRemotePrintJob(String queueId, RemotePrintJob remotePrintJob) {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long returnValue = 0l;
		try {
			InputStream inputStream = remotePrintJob.getStreamForBytes();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int byteVal = -1;
			if (inputStream != null) {
				while((byteVal = inputStream.read()) != -1) {
					outputStream.write(byteVal);
				}
			}
			outputStream.close();
			returnValue = getLastJobId() + 1;
			connection = getConnection();
			connection.setAutoCommit(false);
			String query = "insert into PRINT_JOB (" +
					JOB_ID_FIELD_NAME + "," +
					QUEUE_ID_FIELD_NAME + "," +
					"PRINT_SERVICE_NAME, " +
					"DOC_ATTRIBUTES, " +
					"PRINT_REQUEST_ATTRIBUTES, " +
					"DOC_FLAVOR, " +
					"STATUS, " +
					"PRINT_DATA) values (?,?,?,?,?,?,?,?)";
			stmt = connection.prepareStatement(query);
			stmt.setLong(1, returnValue);
			stmt.setString(2, queueId);
			stmt.setString(3, remotePrintJob.getPrintService().getName());
			stmt.setString(4, PrintServiceUtils.serializeAttributes(remotePrintJob.getDocAttributeSet()));
			stmt.setString(5, PrintServiceUtils.serializeAttributes(remotePrintJob.getPrintRequestAttributeSet()));
			stmt.setString(6, PrintServiceUtils.serializeDocFlavor(remotePrintJob.getDocFlavor()));
			stmt.setInt(7, RemotePrintJobStatus.NOT_PRINTED.ordinal());
			stmt.setBytes(8, outputStream.toByteArray());
			stmt.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			close(rs, stmt, connection);
		}
		return returnValue;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#removeRemotePrintJob(long)
	 */
	@Override
	public boolean removeRemotePrintJob(long jobId) {
		Connection connection = null;
		PreparedStatement stmt = null;
		try {
			connection = getConnection();
			stmt = getJobStatement(connection, DELETE_JOB, jobId);
			stmt.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			close(stmt, connection);
		}
		return true;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getRemotePrintJob(long)
	 */
	@Override
	public RemotePrintJob getRemotePrintJob(long jobId) {
		RemotePrintJob returnValue = null;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			stmt = getJobStatement(connection, SELECT_JOB, jobId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String printServiceName = rs.getString("PRINT_SERVICE_NAME");
				DocAttributeSet docAttributeSet = (DocAttributeSet) PrintServiceUtils.convertToDocAttributeSet(rs.getString("DOC_ATTRIBUTES"));
				PrintRequestAttributeSet printRequestAttributeSet = (PrintRequestAttributeSet) PrintServiceUtils.convertToPrintRequestAttributeSet(rs.getString("PRINT_REQUEST_ATTRIBUTES"));
				DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(rs.getString("DOC_FLAVOR"));
				ByteArrayInputStream inputStream = new ByteArrayInputStream(rs.getBytes("PRINT_DATA"));
				PrintService printService = PrintServiceUtils.findPrinter(printServiceName);
				if (printService != null) {
					Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, docAttributeSet);
					returnValue = (RemotePrintJob) printService.createPrintJob();
					try {
						returnValue.update(doc, printRequestAttributeSet);
						returnValue.setDocFlavor(docFlavor);
					} catch (PrintException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			close(stmt, connection);
		}
		
		return returnValue;
	}

	/**
	 * @see net.sf.wubiq.print.managers.IRemotePrintJobManager#getPrintJobs(java.lang.String, net.sf.wubiq.print.jobs.RemotePrintJobStatus)
	 */
	@Override
	public Collection<Long> getPrintJobs(String queueId,
			RemotePrintJobStatus status) {
		Collection<Long> returnValue = new ArrayList<Long>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			stmt = getJobStatement(connection, LOOK_UP_PENDING);
			stmt.setString(1, queueId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				long jobId = rs.getLong(JOB_ID_FIELD_NAME);
				returnValue.add(jobId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		} finally {
			close(stmt, connection);
		}
		
		return returnValue;
	}

	/**
	 * Returns a connection to 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		Connection returnValue = null;
        try {
			Class.forName("org.hsqldb.jdbcDriver" );
		} catch (ClassNotFoundException e) {
			throw new SQLException (e.getMessage());
		}
		StringBuffer buffer = new StringBuffer("jdbc:hsqldb:")
			.append(ServerProperties.getHsqldbHost());
		if (!Is.emptyString(ServerProperties.getHsqldbPort())) {
			buffer.append(':')
			.append(ServerProperties.getHsqldbPort());
		}
		buffer.append('/')
			.append(ServerProperties.getHsqldbDbName());
        returnValue = DriverManager.getConnection(buffer.toString(), "SA", "");
	    return returnValue;
	}
	
	private long getLastJobId() throws SQLException {
		long returnValue = 0l;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			stmt = connection.createStatement();
			rs = stmt.executeQuery("select ID from PRINT_JOB order by ID desc limit 1");
			if (rs.next()) {
				returnValue = rs.getLong("ID");
			}
		} finally {
			close(rs, stmt, connection);
		}
		return returnValue;
	}
	
	/**
	 * Gracefully closes resources.
	 * @param rs ResultSet resource.
	 * @param stmt Statement resource.
	 * @param connection Connection resource.
	 */
	private void close(ResultSet rs, Statement stmt, Connection connection) {
		try {
			if (rs != null) rs.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
		try {
			if (stmt != null) stmt.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
		try {
			if (connection != null) connection.close();
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
	}
	
	/**
	 * Gracefully closes resources.
	 * @param connection Connection resource.
	 */
	private void close(Connection connection) {
		close(null, null, connection);
	}
	
	/**
	 * Gracefully closes resources.
	 * @param stmt Statement resource.
	 * @param connection Connection resource.
	 */
	private void close(Statement stmt, Connection connection) {
		close(null, stmt, connection);
	}
	
	/**
	 * Creates a prepared statement.
	 * @param connection Connection to get the statement.
	 * @param jobId Id of the job to found.
	 * @return A prepared statement.
	 * @throws SQLException
	 */
	private PreparedStatement getJobStatement(Connection connection, String query, Long jobId) throws SQLException {
		PreparedStatement returnValue = getJobStatement(connection, query);
		returnValue.setLong(1, jobId);
		return returnValue;
	}

	/**
	 * Creates a prepared statement.
	 * @param connection Connection to get the statement.
	 * @param jobId Id of the job to found.
	 * @return A prepared statement.
	 * @throws SQLException
	 */
	private PreparedStatement getJobStatement(Connection connection, String query) throws SQLException {
		PreparedStatement returnValue = null;
		returnValue = connection.prepareStatement(query,
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		return returnValue;
	}

}
