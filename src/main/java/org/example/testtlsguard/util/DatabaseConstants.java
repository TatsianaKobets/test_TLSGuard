package org.example.testtlsguard.util;

/**
 * Provides database-related constants and SQL queries.
 * <p>
 * This class provides constants for database connection properties, table names, and SQL queries
 * for creating and manipulating tables.
 */
public class DatabaseConstants {

  public static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";
  public static final String USER = "";
  public static final String PASSWORD = "";

  public static final String TABLE_WEBSITES = "websites";
  public static final String TABLE_CERTIFICATES = "certificates";
  public static final String DROP_ALL_OBJECTS_DELETE_FILES = "DROP ALL OBJECTS DELETE FILES";
  public static final String CREATE_WEBSITES_TABLE_SQL = "CREATE TABLE IF NOT EXISTS websites ("
      + "id INT AUTO_INCREMENT PRIMARY KEY, "
      + "url VARCHAR(255) NOT NULL UNIQUE, "
      + "schedule VARCHAR(50) NOT NULL, "
      + "last_checked TIMESTAMP)";

  public static final String CREATE_CERTIFICATES_TABLE_SQL =
      "CREATE TABLE IF NOT EXISTS certificates ("
          + "id INT AUTO_INCREMENT PRIMARY KEY, "
          + "website_id INT NOT NULL, "
          + "subject VARCHAR(255), "
          + "issuer VARCHAR(255), "
          + "valid_from TIMESTAMP, "
          + "valid_to TIMESTAMP, "
          + "serial_number VARCHAR(100), "
          + "pem TEXT, "
          + "checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
          + "FOREIGN KEY (website_id) REFERENCES websites(id))";
  public static final String INSERT_WEBSITE_SQL = "INSERT INTO websites(url, schedule) VALUES(?, ?)";
  public static final String SELECT_ALL_WEBSITES_SQL =
      "SELECT w.id, w.url, w.schedule, c.checked_at AS last_checked, c.valid_to "
          + "FROM websites w  "
          + "LEFT JOIN ( SELECT website_id, MAX(checked_at) "
          + "AS last_checked "
          + "FROM certificates "
          + "GROUP BY website_id) latest ON w.id = latest.website_id "
          + "LEFT JOIN certificates c ON w.id = c.website_id "
          + "AND c.checked_at = latest.last_checked ORDER BY w.id";
  public static final String CHECK_WEBSITE_BY_URL = "SELECT id FROM websites WHERE url = ?";
  public static final String UPDATE_LAST_CHECKED_DATE_BY_ID = "UPDATE websites SET last_checked = ? WHERE id = ?";

  public static final String INSERT_CERTIFICATE_SQL = "INSERT INTO certificates("
      + "website_id, subject, issuer, valid_from, valid_to, serial_number, pem)"
      + " VALUES(?, ?, ?, ?, ?, ?, ?)";
  public static final String DELETE_ALL_WEBSITES_SQL = "DELETE FROM websites";
  public static final String DELETE_ALL_CERTIFICATES_SQL = "DELETE FROM certificates";
}
