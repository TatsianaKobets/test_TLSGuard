package org.example.testtlsguard.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.example.testtlsguard.model.Website;

public class WebsiteDao {

  private static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";
  private static final String CREATE_TABLE_SQL = " CREATE TABLE IF NOT EXISTS websites ( id INT AUTO_INCREMENT PRIMARY KEY, url VARCHAR(255) NOT NULL UNIQUE, schedule VARCHAR(50) NOT NULL )";

  private static final String INSERT_SQL = "INSERT INTO websites(url, schedule) VALUES(?, ?)";
  private static final String SELECT_ALL_SQL =
      "SELECT w.id, w.url, w.schedule, c.checked_at AS last_checked, c.valid_to " +
          "FROM websites w " +
          "LEFT JOIN certificates c ON w.id = c.website_id " +
          "ORDER BY w.id";

  public WebsiteDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(CREATE_TABLE_SQL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addWebsite(Website website) {
    // Проверяем, существует ли сайт с таким URL
    String checkSql = "SELECT id FROM websites WHERE url = ?";
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(checkSql)) {

      pstmtCheck.setString(1, website.getUrl());
      ResultSet rs = pstmtCheck.executeQuery();

      if (rs.next()) {
        // Сайт уже существует, можно обновить его данные или просто выйти
        System.out.println("Website with URL " + website.getUrl() + " already exists.");
        return;
      }

      // Если сайта нет, вставляем новую запись
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
}