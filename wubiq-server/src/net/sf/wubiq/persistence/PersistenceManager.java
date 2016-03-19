/**
 * 
 */
package net.sf.wubiq.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.sf.wubiq.data.RemoteClient;
import net.sf.wubiq.data.WubiqPrintJob;
import net.sf.wubiq.data.WubiqPrintService;
import net.sf.wubiq.data.WubiqServer;
import net.sf.wubiq.utils.ServerLabels;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * Handles all persistence related cases.
 * @author Federico Alcantara
 *
 */
public final class PersistenceManager {
	
	private static String dialect = null;
	
	private static final Log LOG = LogFactory.getLog(PersistenceManager.class);
	
	private static DataSource dataSource = null;
	
	private final static ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();
	
	private PersistenceManager() {
	}
	
	public static void setDialect(String dialect) {
		PersistenceManager.dialect = dialect;
	}
	
	/**
	 * Evaluates if the persistence is enabled.
	 * @return
	 */
	public static boolean isPersistenceEnabled() {
		boolean returnValue = false;
		if (getDataSource() != null) {
			if (dialect == null) {
				LOG.error(ServerLabels.get("server.error_must_define_dialect"));
			} else {
				returnValue = true;
			}
		}
		return returnValue;
	}
	
	/**
	 * Returns an instance of an entity manager.
	 * @return Entity manager.
	 */
	public static EntityManager em() {
		EntityManager em = entityManager.get();
		if (em == null || !em.isOpen()) {
			em = Persistence.createEntityManagerFactory("default").createEntityManager();
			em.getTransaction().begin();
			entityManager.set(em);
		}
		return em;
	}
	
	/**
	 * Commits all changes.
	 */
	public static void commit() {
		EntityManager m = entityManager.get();		
		if (m == null) {
			return;
		}
		if (m.isOpen()) {			
			EntityTransaction t = (EntityTransaction) m.getTransaction();
			try {
				if (t.isActive()) {
					t.commit();
				} else {
					m.flush();
				}
			}
			finally {
				entityManager.set(null);
				m.close();				
			}
		}
		else {					
			entityManager.set(null);
		}
	}
	
	/**
	 * Rolls back any pending transaction.
	 */
	public static void rollback() {
		EntityManager m = entityManager.get();
		if (m == null) {
			return;
		}

		if (m.isOpen()) {
			EntityTransaction t = (EntityTransaction) m.getTransaction();
			try {
				t.rollback();
			}
			finally {
				entityManager.set(null);
				m.close();
			}
		}					
		else {
			entityManager.set(null);
		}
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
				LOG.debug(ExceptionUtils.getMessage(e));
			}
		}
		return dataSource;
	}
	
	/**
	 * Creates the schema.
	 */
	public static void createSchemas() {
		try {
			Properties properties = new Properties();
			properties.put("hibernate.default_schema", "");
			properties.put("hibernate.dialect", dialect);
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("default", properties);
			emf.createEntityManager();
			File tempFile = File.createTempFile("wubiq_server", ".sql");
			Configuration cfg = new Configuration();
			cfg.addAnnotatedClass(WubiqPrintJob.class);
			cfg.addAnnotatedClass(RemoteClient.class);
			cfg.addAnnotatedClass(WubiqPrintService.class);
			cfg.addAnnotatedClass(WubiqServer.class);
			cfg.setProperty("hibernate.dialect", dialect);
			
			SchemaExport export = new SchemaExport(cfg);
			export.setOutputFile(tempFile.getAbsolutePath());
			export.setFormat(false);
			export.execute(true, false, false, true);

			if (tempFile.exists()) {
				String line = null;
				BufferedReader reader = new BufferedReader(new FileReader(tempFile));
				while ((line = reader.readLine()) != null) {
					try {
						PersistenceManager.em().createNativeQuery(line).executeUpdate();
						PersistenceManager.commit();
					} catch (Exception e) {
						PersistenceManager.rollback();
					}
				}
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates the schema.
	 */
	public static void createSchemasHibernate5() {
		/*
		try {
			File tempFile = File.createTempFile("wubiq_server", ".sql");
			MetadataSources metadataSources = new MetadataSources(
				    new StandardServiceRegistryBuilder()
				    	.applySetting(AvailableSettings.DIALECT, dialect)
				        .build());
			final MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
			// adds entities
			metadataSources.addAnnotatedClass(WubiqPrintJob.class);
			metadataSources.addAnnotatedClass(RemoteClient.class);
			metadataSources.addAnnotatedClass(WubiqPrintService.class);
			//
			EnumSet<TargetType> types = EnumSet.of(TargetType.SCRIPT);
			new SchemaExport()
				.setOutputFile(tempFile.getAbsolutePath())
				.execute(types, Action.CREATE, metadataBuilder.build());
			if (tempFile.exists()) {
				String line = null;
				BufferedReader reader = new BufferedReader(new FileReader(tempFile));
				while ((line = reader.readLine()) != null) {
					try {
						PersistenceManager.em().createNativeQuery(line).executeUpdate();
						PersistenceManager.commit();
					} catch (Exception e) {
						PersistenceManager.rollback();
					}
				}
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		*/
	}

}
