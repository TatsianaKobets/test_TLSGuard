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
  private static final String CREATE_TABLE_SQL = " CREATE TABLE IF NOT EXISTS websites ( id INT AUTO_INCREMENT PRIMARY KEY, url VARCHAR(255) NOT NULL UNIQUE, schedule VARCHAR(50) NOT NULL, last_checked TIMESTAMP)";

  private static final String INSERT_SQL = "INSERT INTO websites(url, schedule) VALUES(?, ?)";
  private static final String SELECT_ALL_SQL =
      "SELECT w.id, w.url, w.schedule, c.checked_at AS last_checked, c.valid_to " +
          "FROM websites w " +
          "LEFT JOIN certificates c ON w.id = c.website_id " +
          "ORDER BY w.id";
  private static final String CHECK_Sql = "SELECT id FROM websites WHERE url = ?";

  public WebsiteDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(CREATE_TABLE_SQL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addWebsite(Website website) {
    if (website.getUrl() == null || website.getUrl().trim().isEmpty()) {
      System.out.println("URL is required.");
      return;
    }

    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_Sql)) {

      pstmtCheck.setString(1, website.getUrl());
      ResultSet rs = pstmtCheck.executeQuery();

      if (rs.next()) {
        System.out.println("Website with URL " + website.getUrl() + " already exists.");
        return;
      }

      try (PreparedStatement pstmtInsert = conn.prepareStatement(INSERT_SQL)) {
        pstmtInsert.setString(1, website.getUrl());
        pstmtInsert.setString(2, website.getSchedule());
        pstmtInsert.executeUpdate();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public List<Website> getAllWebsites() {
    List<Website> websites = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

      while (rs.next()) {
        Website website = new Website(
            rs.getInt("id"),
            rs.getString("url"),
            rs.getString("schedule")
        );
        website.setLastChecked(rs.getTimestamp("last_checked"));
        website.setValidTo(rs.getTimestamp("valid_to"));
        websites.add(website);
      }
    } catch (SQLException e) {
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
}