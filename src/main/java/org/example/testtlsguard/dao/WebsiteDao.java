package org.example.testtlsguard.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.testtlsguard.model.Website;

public class WebsiteDao {
  private static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";
  private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS websites (
            id INT AUTO_INCREMENT PRIMARY KEY,
            url VARCHAR(255) NOT NULL UNIQUE,
            schedule VARCHAR(50) NOT NULL
        )""";

  private static final String INSERT_SQL = "INSERT INTO websites(url, schedule) VALUES(?, ?)";
  private static final String SELECT_ALL_SQL = "SELECT * FROM websites";

  public WebsiteDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      stmt.execute(CREATE_TABLE_SQL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void addWebsite(Website website) {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
      pstmt.setString(1, website.getUrl());
      pstmt.setString(2, website.getSchedule());
      pstmt.executeUpdate();
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
        websites.add(new Website(
            rs.getInt("id"),
            rs.getString("url"),
            rs.getString("schedule")
        ));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return websites;
  }
}
