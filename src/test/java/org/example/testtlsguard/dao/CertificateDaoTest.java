package org.example.testtlsguard.dao;

import static org.example.testtlsguard.util.DatabaseConstants.JDBC_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import org.example.testtlsguard.model.CertificateInfo;
import org.example.testtlsguard.model.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificateDaoTest {

  private WebsiteDao websiteDao;
  private CertificateDao certificateDao;
  private static final String TEST_URL = "https://test.example.com";

  @BeforeEach
  void setUp() {
    websiteDao = new WebsiteDao();
    certificateDao = new CertificateDao();
    websiteDao.clearAllWebsites();
  }

  @Test
  void testSaveCertificateSuccessfully() throws SQLException {

    Website website = new Website(0, TEST_URL, "daily");
    websiteDao.addWebsite(website);
    List<Website> websites = websiteDao.getAllWebsites();
    int websiteId = websites.get(0).getId();

    CertificateInfo certInfo = new CertificateInfo();
    certInfo.setSubject("CN=test.example.com");
    certInfo.setIssuer("CN=Test CA");
    certInfo.setValidFrom(new Timestamp(System.currentTimeMillis()));
    certInfo.setValidTo(new Timestamp(System.currentTimeMillis() + 86400000));
    certInfo.setSerialNumber("12345");
    certInfo.setPem("-----BEGIN CERTIFICATE-----...");

    certificateDao.saveCertificate(websiteId, certInfo);

    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT * FROM certificates WHERE website_id = " + websiteId)) {

      assertTrue(rs.next(), "Certificate should be present in database");
      assertEquals(websiteId, rs.getInt("website_id"));
      assertEquals(certInfo.getSubject(), rs.getString("subject"));
      assertEquals(certInfo.getIssuer(), rs.getString("issuer"));
      assertEquals(certInfo.getValidFrom(), rs.getTimestamp("valid_from"));
      assertEquals(certInfo.getValidTo(), rs.getTimestamp("valid_to"));
      assertEquals(certInfo.getSerialNumber(), rs.getString("serial_number"));
      assertEquals(certInfo.getPem(), rs.getString("pem"));
    }
  }

  @Test
  void testSaveCertificateWithNullCertInfo() throws SQLException {
    certificateDao.saveCertificate(1, null);
    assertEquals(0, getCertificateCount(), "No certificate should be saved with null certInfo");
  }

  @Test
  void testSaveCertificateWithInvalidWebsiteId() throws SQLException {
    CertificateInfo certInfo = new CertificateInfo();
    certInfo.setSubject("CN=test.example.com");
    certInfo.setIssuer("CN=Test CA");
    certInfo.setValidFrom(new Timestamp(System.currentTimeMillis()));
    certInfo.setValidTo(new Timestamp(System.currentTimeMillis() + 86400000));
    certInfo.setSerialNumber("12345");
    certInfo.setPem("-----BEGIN CERTIFICATE-----...");

    certificateDao.saveCertificate(999, certInfo); // Non-existent website ID
    assertEquals(0, getCertificateCount(),
        "No certificate should be saved with invalid website ID");
  }

  private int getCertificateCount() throws SQLException {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM certificates")) {
      rs.next();
      return rs.getInt(1);
    }
  }
}