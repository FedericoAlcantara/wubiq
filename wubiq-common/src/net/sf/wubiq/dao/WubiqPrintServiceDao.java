/**
 * 
 */
package net.sf.wubiq.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.print.DocFlavor;

import net.sf.wubiq.persistence.CommonPersistenceManager;
import net.sf.wubiq.print.services.RemotePrintService;
import net.sf.wubiq.utils.PrintServiceUtils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handles the print service data access.
 * @author Federico Alcantara
 *
 */
public enum WubiqPrintServiceDao {
	INSTANCE;
	private final Log LOG = LogFactory.getLog(WubiqPrintServiceDao.class);
	
	/**
	 * Saves the print service.
	 * @param printService Remote print service to be saved.
	 */
	public void registerRemotePrintService(RemotePrintService printService) {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			stmt = connection.prepareStatement("SELECT * FROM wubiq_print_service"
					+ " WHERE "
					+ "uuid=?"
					+ " AND "
					+ "name=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, printService.getUuid());
			stmt.setString(2, printService.getName());
			rs = stmt.executeQuery();
			
			if (rs.first()) {
				rs.updateBytes("service", serializePrintService(printService));
				rs.updateRow();
			} else {
				rs.moveToInsertRow();
				rs.updateString("uuid", printService.getUuid());
				rs.updateString("name", printService.getName());
				rs.updateBoolean("mobile", printService.isMobile());
				rs.updateBytes("service", serializePrintService(printService));
				rs.insertRow();
			}
			CommonPersistenceManager.commit(connection);
		} catch (SQLException e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(rs, stmt, connection);
		}
	}
	
	/**
	 * Reads all remote print services();
	 * @return List of remote print services ordered by uuid and name.
	 */
	public Collection<RemotePrintService> remoteAllPrintServices() {
		Collection<RemotePrintService> returnValue = new ArrayList<RemotePrintService>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			try {
				stmt = connection.prepareStatement("SELECT * FROM wubiq_print_service"
						+ " ORDER BY "
						+ "uuid, name",
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery();
				while(rs.next()) {
					RemotePrintService remote = null;
					try {
						remote = deserialize(rs.getBytes("service"));
						if (remote != null) {
							returnValue.add(remote);
						}
					} catch (Exception e) {
						LOG.error(ExceptionUtils.getMessage(e));
					}
				}
				CommonPersistenceManager.commit(connection);
			} finally {
				CommonPersistenceManager.close(rs, stmt, null);
			}
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(rs, stmt, connection);
		}
		return returnValue;
	}
	
	/**
	 * Reads all remote print services();
	 * @param uuid Remote client unique id.
	 * @return List of remote print services ordered by uuid and name.
	 */
	public Map<String, RemotePrintService> remotePrintServices(String uuid) {
		Map<String, RemotePrintService> returnValue = new HashMap<String, RemotePrintService>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			stmt = connection.prepareStatement("SELECT name, service FROM wubiq_print_service"
					+ " WHERE "
					+ "uuid=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, uuid);
			rs = stmt.executeQuery();
			while(rs.next()) {
				RemotePrintService remote = deserialize(rs.getBytes("service"));
				if (remote != null) {
					returnValue.put(rs.getString("name"), remote);
				}
			}
			CommonPersistenceManager.commit(connection);
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(rs, stmt, connection);
		}
		return returnValue;
	}

	/**
	 * Removes remote print services associated to client uuid.
	 * @param uuid Remote client unique id.
	 * @return List of remote print services ordered by uuid and name.
	 */
	public void removePrintServices(String uuid) {
		Connection connection = null;
		PreparedStatement stmt = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			stmt = connection.prepareStatement("DELETE FROM wubiq_print_service"
					+ " WHERE "
					+ "uuid=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, uuid);
			stmt.executeUpdate();
			CommonPersistenceManager.commit(connection);
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(null, stmt, connection);
		}
	}

	/**
	 * Removes a single remote print services associated to client uuid.
	 * @param uuid Remote client unique id.
	 * @param name Remote print service.
	 * @return List of remote print services ordered by uuid and name.
	 */
	public void removePrintService(String uuid, String name) {
		Connection connection = null;
		PreparedStatement stmt = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			stmt = connection.prepareStatement("DELETE FROM wubiq_print_service"
					+ " WHERE "
					+ "uuid=?"
					+ " AND "
					+ "name=?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			stmt.setString(1, uuid);
			stmt.setString(2, name);
			stmt.executeUpdate();
			CommonPersistenceManager.commit(connection);
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(null, stmt, connection);
		}
	}

	/**
	 * Returns true if at least one of the print services is mobile.
	 * @param uuid Unique id of the remote client.
	 * @return Mobile.
	 */
	public boolean isMobile(String uuid) {
		boolean returnValue = false;
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean autoCommitState = true;
		try {
			connection = CommonPersistenceManager.getConnection();
			autoCommitState = CommonPersistenceManager.turnOffAutoCommit(connection);
			stmt = connection.prepareStatement("SELECT count(name) FROM wubiq_print_service"
					+ " WHERE "
					+ "uuid=?"
					+ " AND "
					+ "mobile=true"
					+ " GROUP BY "
					+ "uuid",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setString(1, uuid);
			stmt.setMaxRows(1);
			rs = stmt.executeQuery();
			while(rs.next()) {
				if (rs.getLong(1) > 0l) {
					returnValue = true;
				}
			}
			CommonPersistenceManager.commit(connection);
		} catch (Exception e) {
			LOG.error(ExceptionUtils.getMessage(e), e);
			throw new RuntimeException(e);
		} finally {
			CommonPersistenceManager.resetAutoCommit(connection, autoCommitState);
			CommonPersistenceManager.close(rs, stmt, connection);
		}
		return returnValue;
	}
	
	/**
	 * Serializes a remote print service.
	 * @param printService Remote print service to serialize.
	 * @return Byte array representing the object.
	 */
	/**
	 * Serializes a remote print service.
	 * @param printService Remote print service to serialize.
	 * @return Byte array representing the object.
	 */
	private byte[] serializePrintService(RemotePrintService printService) {
		byte[] returnValue = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(stream);
			output.writeObject(printService.getName());
			output.writeObject(PrintServiceUtils.serializeServiceCategories(printService, false));
			output.writeObject(printService.getUuid());
			output.writeObject(printService.getRemoteComputerName());
			output.writeObject(printService.getRemoteName());
			output.writeObject(printService.getSupportedDocFlavors());
			output.writeBoolean(printService.isMobile());
			output.writeBoolean(printService.getDirectCommunicationEnabled());
			output.close();
			stream.flush();
			stream.close();
			returnValue = stream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return returnValue;
	}
	
	/**
	 * De-serializes a persisted service into a functional one.
	 * @param service Service data to be deserialized.
	 * @return Instance of Remote print service or null if could not deserialize it.
	 * @throws IOException If stream is corrupted or not valid.
	 * @throws ClassNotFoundException If can't produce an instance of RemotePrintService.
	 */
	private RemotePrintService deserialize(byte[] service) throws IOException, ClassNotFoundException {
		RemotePrintService remotePrintService = null;
		if (service != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(service);
			ObjectInputStream input = new ObjectInputStream(stream);
			String serviceName = (String) input.readObject();
			String categoriesString = (String)input.readObject();
			String uuid = (String)input.readObject();
			String remoteComputerName = (String)input.readObject();
			String remoteName = (String)input.readObject();
			DocFlavor[] docFlavors = (DocFlavor[])input.readObject();
			boolean mobile = input.readBoolean();
			boolean directCommunication = input.readBoolean();
			
			remotePrintService = (RemotePrintService) PrintServiceUtils.deSerializeService(serviceName, categoriesString);
			remotePrintService.setUuid(uuid);
			remotePrintService.setRemoteComputerName(remoteComputerName);
			remotePrintService.setSupportedDocFlavors(docFlavors);
			remotePrintService.setRemoteName(remoteName);
			remotePrintService.setMobile(mobile);
			remotePrintService.setDirectCommunicationEnabled(directCommunication);
		}
		return remotePrintService;
	}

}
