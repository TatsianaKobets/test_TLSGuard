package org.example.testtlsguard.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import org.example.testtlsguard.model.CertificateInfo;

public class CertificateDao {

  private static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";
  private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS certificates ( id INT AUTO_INCREMENT PRIMARY KEY, website_id INT NOT NULL, subject VARCHAR(255), issuer VARCHAR(255), valid_from TIMESTAMP, valid_to TIMESTAMP, serial_number VARCHAR(100), pem TEXT, checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (website_id) REFERENCES websites(id))";

  private static final String INSERT_SQL = "INSERT INTO certificates(website_id, subject, issuer, valid_from, valid_to, serial_number, pem) VALUES(?, ?, ?, ?, ?, ?, ?)";

  public CertificateDao() {
    try (Connection conn = DriverManager.getConnection(
        JDBC_URL); Statement stmt = conn.createStatement()) {
      stmt.execute(CREATE_TABLE_SQL);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveCertificate(int websiteId, CertificateInfo certInfo) {
    try (Connection conn = DriverManager.getConnection(
        JDBC_URL); PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {
      pstmt.setInt(1, websiteId);
      pstmt.setString(2, certInfo.getSubject());
      pstmt.setString(3, certInfo.getIssuer());
      pstmt.setTimestamp(4, new Timestamp(certInfo.getValidFrom().getTime()));
      pstmt.setTimestamp(5, new Timestamp(certInfo.getValidTo().getTime()));
      pstmt.setString(6, certInfo.getSerialNumber());
      pstmt.setString(7, certInfo.getPem()); // Сохраняем PEM
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
