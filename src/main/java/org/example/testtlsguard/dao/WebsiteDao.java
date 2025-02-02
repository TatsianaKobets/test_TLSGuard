package org.example.testtlsguard.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.example.testtlsguard.model.Website;

public class WebsiteDao {

  private static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";
  private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS websites ("
      + "id INT AUTO_INCREMENT PRIMARY KEY, "
      + "url VARCHAR(255) NOT NULL UNIQUE, "
      + "schedule VARCHAR(50) NOT NULL, "
      + "last_checked TIMESTAMP)";

  private static final String INSERT_SQL = "INSERT INTO websites(url, schedule) VALUES(?, ?)";
  private static final String SELECT_ALL_SQL =
      "SELECT w.id, w.url, w.schedule, c.checked_at AS last_checked, c.valid_to\n" +
          "FROM websites w\n" +
          "LEFT JOIN (\n" +
          "    SELECT website_id, MAX(checked_at) AS last_checked\n" +
          "    FROM certificates\n" +
          "    GROUP BY website_id\n" +
          ") latest ON w.id = latest.website_id\n" +
          "LEFT JOIN certificates c ON w.id = c.website_id AND c.checked_at = latest.last_checked\n"
          +
          "ORDER BY w.id";
  private static final String CHECK_Sql = "SELECT id FROM websites WHERE url = ?";

  public WebsiteDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      System.out.println("Connected to database: " + JDBC_URL); // Логирование
      stmt.execute(CREATE_TABLE_SQL);
      System.out.println("Table 'websites' created or already exists.");

      // Проверка структуры таблицы
      ResultSet rs = conn.getMetaData().getColumns(null, null, "WEBSITES", null);
      while (rs.next()) {
        String columnName = rs.getString("COLUMN_NAME");
        String columnType = rs.getString("TYPE_NAME");
        System.out.println("Column: " + columnName + ", Type: " + columnType);
      }
    } catch (SQLException e) {
      System.err.println("Error connecting to database: " + e.getMessage()); // Логирование ошибки
      e.printStackTrace();
    }
  }

  public void addWebsite(Website website) {
    if (website.getUrl() == null || website.getUrl().trim().isEmpty()) {
      System.out.println("URL is required.");
      return;
    }
    System.out.println(
        "Adding website: " + website.getUrl() + ", Schedule: " + website.getSchedule());

    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_Sql)) {

      pstmtCheck.setString(1, website.getUrl());
      ResultSet rs = pstmtCheck.executeQuery();

      if (rs.next()) {
        int existingId = rs.getInt("id");
        System.out.println(
            "Website with URL " + website.getUrl() + " already exists. ID: " + existingId);
        return;
      }
      try (PreparedStatement pstmtInsert = conn.prepareStatement(INSERT_SQL)) {
        pstmtInsert.setString(1, website.getUrl());
        pstmtInsert.setString(2, website.getSchedule());
        int rowsInserted = pstmtInsert.executeUpdate();
        System.out.println("Rows inserted: " + rowsInserted); // Логирование
        if (rowsInserted > 0) {
          System.out.println("Website added: " + website.getUrl());
        } else {
          System.out.println("Failed to add website: " + website.getUrl());
        }
      }
    } catch (SQLException e) {
      System.err.println("Error adding website: " + e.getMessage()); // Логирование ошибки
      e.printStackTrace();
    }
  }

  public List<Website> getAllWebsites() {
    List<Website> websites = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

      System.out.println("Executing query: " + SELECT_ALL_SQL);
      while (rs.next()) {
        Website website = new Website(
            rs.getInt("id"),
            rs.getString("url"),
            rs.getString("schedule")
        );
        website.setLastChecked(rs.getTimestamp("last_checked"));
        Timestamp validTo = rs.getTimestamp("valid_to");
        System.out.println("Valid to: " + validTo);
        if (!rs.wasNull()) { // Проверка на NULL
          website.setValidTo(validTo);
        }
        websites.add(website);
      }
      System.out.println("Websites fetched from DB: " + websites.size());
    } catch (SQLException e) {
      System.err.println("Error fetching websites: " + e.getMessage());
      e.printStackTrace();
    }
    return websites;
  }

  public void clearAllWebsites() {
    String deleteCertificatesSql = "DELETE FROM certificates"; // Удаляем все сертификаты
    String deleteWebsitesSql = "DELETE FROM websites"; // Затем удаляем все сайты
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtDeleteCertificates = conn.prepareStatement(deleteCertificatesSql);
        PreparedStatement pstmtDeleteWebsites = conn.prepareStatement(deleteWebsitesSql)) {

      pstmtDeleteCertificates.executeUpdate();
      pstmtDeleteWebsites.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void updateLastChecked(int websiteId, Timestamp lastChecked) {
    String updateSql = "UPDATE websites SET last_checked = ? WHERE id = ?";
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
      pstmt.setTimestamp(1, lastChecked);
      pstmt.setInt(2, websiteId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean checkIfWebsiteExists(String url) {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_Sql)) {
      pstmtCheck.setString(1, url);
      ResultSet rs = pstmtCheck.executeQuery();
      return rs.next();
    } catch (SQLException e) {
      System.err.println("Error checking website existence: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}