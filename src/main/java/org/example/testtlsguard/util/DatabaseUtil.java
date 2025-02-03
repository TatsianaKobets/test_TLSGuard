package org.example.testtlsguard.util;

import static org.example.testtlsguard.util.DatabaseConstants.CREATE_CERTIFICATES_TABLE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.CREATE_WEBSITES_TABLE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.DELETE_ALL_CERTIFICATES_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.DELETE_ALL_WEBSITES_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.DROP_ALL_OBJECTS_DELETE_FILES;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for database operations.
 *
 * This class provides methods for establishing a connection to the database,
 * initializing the database, clearing the database, and dropping the database.
 */
public class DatabaseUtil {

  private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);

  public static Connection getConnection() throws SQLException {
    logger.debug("Attempting to establish a connection to the database.");
    return DriverManager.getConnection(
        DatabaseConstants.JDBC_URL,
        DatabaseConstants.USER,
        DatabaseConstants.PASSWORD
    );
  }

  public static void initializeDatabase() {
    logger.info("Initializing database...");
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(CREATE_WEBSITES_TABLE_SQL);
      stmt.execute(CREATE_CERTIFICATES_TABLE_SQL);
      logger.info("Database initialized successfully.");
    } catch (SQLException e) {
      logger.error("Error initializing database: {}", e.getMessage(), e);
    }
  }

  public static void clearDatabase() {
    logger.info("Clearing database...");
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(DELETE_ALL_CERTIFICATES_SQL);
      stmt.execute(DELETE_ALL_WEBSITES_SQL);
      logger.info("Database cleared successfully.");
    } catch (SQLException e) {
      logger.error("Error clearing database: {}", e.getMessage(), e);
    }
  }

  public static void dropDatabase() {
    logger.info("Dropping database...");
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(DROP_ALL_OBJECTS_DELETE_FILES);
      logger.info("Database dropped successfully.");
    } catch (SQLException e) {
      logger.error("Error dropping database: {}", e.getMessage(), e);
    }
  }
}
