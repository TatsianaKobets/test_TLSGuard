package org.example.testtlsguard.dao;

import static org.example.testtlsguard.util.DatabaseConstants.CHECK_WEBSITE_BY_URL;
import static org.example.testtlsguard.util.DatabaseConstants.CREATE_WEBSITES_TABLE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.DELETE_ALL_CERTIFICATES_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.DELETE_ALL_WEBSITES_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.INSERT_WEBSITE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.JDBC_URL;
import static org.example.testtlsguard.util.DatabaseConstants.SELECT_ALL_WEBSITES_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.UPDATE_LAST_CHECKED_DATE_BY_ID;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsiteDao {

  private static final Logger logger = LoggerFactory.getLogger(WebsiteDao.class);

  public WebsiteDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      logger.info("Connected to database: {}", JDBC_URL);
      stmt.execute(CREATE_WEBSITES_TABLE_SQL);
      logger.info("Table 'websites' created successfully.");

      ResultSet rs = conn.getMetaData().getColumns(null, null, "WEBSITES", null);
      while (rs.next()) {
        String columnName = rs.getString("COLUMN_NAME");
        String columnType = rs.getString("TYPE_NAME");
        logger.debug("Column: {}, Type: {}", columnName, columnType);
      }
    } catch (SQLException e) {
      logger.error("Error connecting to database or creating table: {}", e.getMessage(), e);
    }
  }

  public void addWebsite(Website website) {
    if (website.getUrl() == null || website.getUrl().trim().isEmpty()) {
      logger.warn("URL is required. Website not added.");
      return;
    }
    logger.info("Attempting to add website: {}, Schedule: {}", website.getUrl(),
        website.getSchedule());

    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_WEBSITE_BY_URL)) {

      pstmtCheck.setString(1, website.getUrl());
      ResultSet rs = pstmtCheck.executeQuery();

      if (rs.next()) {
        int existingId = rs.getInt("id");
        logger.info("Website with URL {} already exists. ID: {}", website.getUrl(), existingId);
        return;
      }

      try (PreparedStatement pstmtInsert = conn.prepareStatement(INSERT_WEBSITE_SQL)) {
        pstmtInsert.setString(1, website.getUrl());
        pstmtInsert.setString(2, website.getSchedule());
        int rowsInserted = pstmtInsert.executeUpdate();
        if (rowsInserted > 0) {
          logger.info("Website added successfully: {}", website.getUrl());
        } else {
          logger.warn("Failed to add website: {}", website.getUrl());
        }
      }
    } catch (SQLException e) {
      logger.error("Error adding website: {}", e.getMessage(), e);
    }
  }

  public List<Website> getAllWebsites() {
    List<Website> websites = new ArrayList<>();
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(SELECT_ALL_WEBSITES_SQL)) {

      logger.debug("Executing query: {}", SELECT_ALL_WEBSITES_SQL);
      while (rs.next()) {
        Website website = new Website(
            rs.getInt("id"),
            rs.getString("url"),
            rs.getString("schedule")
        );
        website.setLastChecked(rs.getTimestamp("last_checked"));
        Timestamp validTo = rs.getTimestamp("valid_to");
        if (!rs.wasNull()) {
          website.setValidTo(validTo);
        }
        websites.add(website);
      }
      logger.info("Fetched {} websites from the database.", websites.size());
    } catch (SQLException e) {
      logger.error("Error fetching websites: {}", e.getMessage(), e);
    }
    return websites;
  }

  public void clearAllWebsites() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtDeleteCertificates = conn.prepareStatement(
            DELETE_ALL_CERTIFICATES_SQL);
        PreparedStatement pstmtDeleteWebsites = conn.prepareStatement(DELETE_ALL_WEBSITES_SQL)) {

      int certificatesDeleted = pstmtDeleteCertificates.executeUpdate();
      int websitesDeleted = pstmtDeleteWebsites.executeUpdate();
      logger.info("Deleted {} certificates and {} websites.", certificatesDeleted, websitesDeleted);
    } catch (SQLException e) {
      logger.error("Error clearing websites and certificates: {}", e.getMessage(), e);
    }
  }

  public void updateLastChecked(int websiteId, Timestamp lastChecked) {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmt = conn.prepareStatement(UPDATE_LAST_CHECKED_DATE_BY_ID)) {
      pstmt.setTimestamp(1, lastChecked);
      pstmt.setInt(2, websiteId);
      int rowsUpdated = pstmt.executeUpdate();
      if (rowsUpdated > 0) {
        logger.info("Updated last checked date for website ID: {}", websiteId);
      } else {
        logger.warn("No website found with ID: {}", websiteId);
      }
    } catch (SQLException e) {
      logger.error("Error updating last checked date: {}", e.getMessage(), e);
    }
  }

  public boolean checkIfWebsiteExists(String url) {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmtCheck = conn.prepareStatement(CHECK_WEBSITE_BY_URL)) {
      pstmtCheck.setString(1, url);
      ResultSet rs = pstmtCheck.executeQuery();
      boolean exists = rs.next();
      logger.debug("Website with URL {} exists: {}", url, exists);
      return exists;
    } catch (SQLException e) {
      logger.error("Error checking website existence: {}", e.getMessage(), e);
      return false;
    }
  }
}