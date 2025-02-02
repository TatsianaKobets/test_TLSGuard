package org.example.testtlsguard.dao;

import static org.example.testtlsguard.util.DatabaseConstants.CREATE_CERTIFICATES_TABLE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.INSERT_CERTIFICATE_SQL;
import static org.example.testtlsguard.util.DatabaseConstants.JDBC_URL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import org.example.testtlsguard.model.CertificateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateDao {

  private static final Logger logger = LoggerFactory.getLogger(CertificateDao.class);

  public CertificateDao() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {
      logger.info("Connected to database: {}", JDBC_URL);
      stmt.execute(CREATE_CERTIFICATES_TABLE_SQL);
      logger.info("Table 'certificates' created successfully.");
    } catch (SQLException e) {
      logger.error("Error connecting to database or creating table: {}", e.getMessage(), e);
    }
  }

  public void saveCertificate(int websiteId, CertificateInfo certInfo) {
    if (certInfo == null) {
      logger.warn("CertificateInfo is null. Certificate not saved.");
      return;
    }

    logger.info("Attempting to save certificate for website ID: {}", websiteId);

    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        PreparedStatement pstmt = conn.prepareStatement(INSERT_CERTIFICATE_SQL)) {
      pstmt.setInt(1, websiteId);
      pstmt.setString(2, certInfo.getSubject());
      pstmt.setString(3, certInfo.getIssuer());
      pstmt.setTimestamp(4, new Timestamp(certInfo.getValidFrom().getTime()));
      pstmt.setTimestamp(5, new Timestamp(certInfo.getValidTo().getTime()));
      pstmt.setString(6, certInfo.getSerialNumber());
      pstmt.setString(7, certInfo.getPem());

      int rowsInserted = pstmt.executeUpdate();
      if (rowsInserted > 0) {
        logger.info("Certificate saved successfully for website ID: {}", websiteId);
      } else {
        logger.warn("Failed to save certificate for website ID: {}", websiteId);
      }
    } catch (SQLException e) {
      logger.error("Error saving certificate for website ID {}: {}", websiteId, e.getMessage(), e);
    }
  }
}