/**
 * 
 */
package net.sf.wubiq.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages persistence without Hibernate
 * @author Federico Alcantara
 *
 */
public final class CommonPersistenceManager {
	private static final Log LOG = LogFactory.getLog(CommonPersistenceManager.class);

	private static DataSource dataSource = null;

	private CommonPersistenceManager() {
	}
	
	/**
	 * Gets a connection from datasource.
	 * @return Connection.
	 * @throws SQLException Thrown if connection could not be produced.
	 */
	public static Connection getConnection() throws SQLException, NullPointerException {
		return getDataSource().getConnection();
	}
	
	
	/**
	 * Finds current datasource
	 * @return
	 */
	private static DataSource getDataSource() {
		if (dataSource == null) {
			Context ctx;
			try {
				ctx = new InitialContext();
				dataSource = (DataSource)ctx.lookup("java:comp/env/jdbc/wubiqDS");
			} catch (NamingException e) {
				LOG.debug(e.getMessage());
			}
		}
		return dataSource;
	}
	
	/**
	 * Turns off auto commit.
	 * @param connection Current connection.
	 * @return Previous value of auto commit.
	 * @throws SQLException
	 */
	public static boolean turnOffAutoCommit(Connection connection) throws SQLException {
		boolean returnValue = connection.getAutoCommit();
		connection.setAutoCommit(false);
		return returnValue;
	}
	
	/**
	 * Resets auto commit to the given state.
	 * @param connection Connection to commit.
	 * @param autoCommitState new auto commit state.
	 */
	public static void resetAutoCommit(Connection connection, boolean autoCommitState) {
		if (connection != null) {
			try {
				connection.setAutoCommit(autoCommitState);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Commits the current transaction. 
	 * @param connection Connection.
	 * @throws SQLException
	 */
	public static void commit(Connection connection) throws SQLException {
		if (connection != null && !connection.isClosed()) {
			try {
				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
			}
		}
	}
	
	/**
	 * Closes any of the resources.
	 * @param rs Resultset to be closed. Might be null.
	 * @param stmt Statement to be closed. Might be null.
	 * @param connection Connection to be closed. Might be null.
	 */
	public static void close(ResultSet rs, Statement stmt, Connection connection) {
		if (rs != null) {
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
				LOG.debug(ExceptionUtils.getMessage(e), e);
			} catch (Exception e) {
				LOG.info(ExceptionUtils.getMessage(e), e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
				stmt = null;
			} catch (SQLException e) {
				LOG.debug(ExceptionUtils.getMessage(e), e);
			} catch (Exception e) {
				LOG.info(ExceptionUtils.getMessage(e), e);
			}
		}
		if (connection != null) { // Pooled connections MUST always be closed.
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				LOG.debug(ExceptionUtils.getMessage(e), e);
			} catch (Exception e) {
				LOG.info(ExceptionUtils.getMessage(e), e);
			}
		}
	}


}
