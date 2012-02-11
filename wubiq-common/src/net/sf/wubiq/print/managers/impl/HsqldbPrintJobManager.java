/**
 * 
 */
package net.sf.wubiq.print.managers.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.print.DocFlavor;
import javax.print.attribute.Attribute;

import net.sf.wubiq.print.jobs.IRemotePrintJob;
import net.sf.wubiq.print.jobs.RemotePrintJobStatus;
import net.sf.wubiq.print.jobs.impl.PrintJobInputStream;
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
					"ATTRIBUTES varchar(" + Integer.MAX_VALUE + "), " +
					"STATUS integer not null, " +
					"DOC_FLAVOR varchar(255) not null, " +
					"CONVERTED integer not null, " +
					"PAGE_HEIGHT decimal(18,6)," +
					"PAGE_WIDTH decimal(18,6)," +
					"PRINT_DOCUMENT binary(" + Integer.MAX_VALUE + ")," +
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
	public long addRemotePrintJob(String queueId, IRemotePrintJob remotePrintJob) {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		long returnValue = 0l;
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int byteVal = -1;
			while ((byteVal = remotePrintJob.getPrintDocument().read()) != -1) {
				outputStream.write(byteVal);
			}
			outputStream.close();
			returnValue = getLastJobId() + 1;
			connection = getConnection();
			connection.setAutoCommit(false);
			String query = "insert into PRINT_JOB (" +
					JOB_ID_FIELD_NAME + "," +
					QUEUE_ID_FIELD_NAME + "," +
					"PRINT_SERVICE_NAME, ATTRIBUTES, STATUS, DOC_FLAVOR, CONVERTED," +
					"PAGE_HEIGHT, PAGE_WIDTH, " +
					"PRINT_DOCUMENT) values (?,?,?,?,?,?,?,?,?,?)";
			stmt = connection.prepareStatement(query);
			stmt.setLong(1, returnValue);
			stmt.setString(2, queueId);
			stmt.setString(3, remotePrintJob.getPrintServiceName());
			stmt.setString(4, PrintServiceUtils.serializeAttributes(remotePrintJob.getAttributes()));
			stmt.setInt(5, RemotePrintJobStatus.NOT_PRINTED.ordinal());
			stmt.setString(6, PrintServiceUtils.serializeDocFlavor(remotePrintJob.getDocFlavor()));
			stmt.setBoolean(7, remotePrintJob.isConverted());
			stmt.setFloat(8, remotePrintJob.getPageHeight());
			stmt.setFloat(9, remotePrintJob.getPageWidth());
			stmt.setBytes(10, outputStream.toByteArray());
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
	public IRemotePrintJob getRemotePrintJob(long jobId) {
		IRemotePrintJob returnValue = null;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			stmt = getJobStatement(connection, SELECT_JOB, jobId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String printServiceName = rs.getString("PRINT_SERVICE_NAME");
				Collection<Attribute> attributes = PrintServiceUtils.convertToAttributes(rs.getString("ATTRIBUTES"));
				ByteArrayInputStream inputStream = new ByteArrayInputStream(rs.getBytes("PRINT_DOCUMENT"));
				String serializedDocFlavor = rs.getString("DOC_FLAVOR");
				DocFlavor docFlavor = PrintServiceUtils.deSerializeDocFlavor(serializedDocFlavor);
				Boolean converted = rs.getBoolean("CONVERTED");
				float pageHeight = rs.getFloat("PAGE_HEIGHT");
				float pageWidth = rs.getFloat("PAGE_WIDTH");
				returnValue = new PrintJobInputStream(printServiceName, inputStream, attributes, docFlavor);
				returnValue.setConverted(converted);
				returnValue.setPageHeight(pageHeight);
				returnValue.setPageWidth(pageWidth);
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
